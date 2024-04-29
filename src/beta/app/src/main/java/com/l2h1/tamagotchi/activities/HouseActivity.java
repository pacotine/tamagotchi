package com.l2h1.tamagotchi.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.l2h1.tamagotchi.R;
import com.l2h1.tamagotchi.fragments.FoodFragment;
import com.l2h1.tamagotchi.fragments.GamesFragment;
import com.l2h1.tamagotchi.fragments.HealthFragment;
import com.l2h1.tamagotchi.fragments.HomeFragment;
import com.l2h1.tamagotchi.fragments.HygieneFragment;
import com.l2h1.tamagotchi.model.DeadEntity;
import com.l2h1.tamagotchi.model.LivingEntity;
import com.l2h1.tamagotchi.model.House;
import com.l2h1.tamagotchi.model.states.State;
import com.l2h1.tamagotchi.utils.json.JSONHelper;
import com.l2h1.tamagotchi.utils.json.JSONable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * The activity displayed when a user wants to take care of his companion,
 * an entity that must be alive (if not, see {@link EntityDeadActivity}).
 * This is the application's most important activity, as it manages the simulation,
 * the user's interactions with its companion and the various fragments presented.
 */
public class HouseActivity extends AppCompatActivity {
    private JSONHelper jsonHelper;
    private HomeFragment homeFragment;
    private HealthFragment healthFragment;
    private GamesFragment gamesFragment;
    private HygieneFragment hygieneFragment;
    private FoodFragment foodFragment;
    private BottomNavigationView menuView;
    private House house;
    private String fileName;
    private double screenTime;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        EdgeToEdge.enable(this);

        this.jsonHelper = new JSONHelper(this);
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            this.fileName = extras.getString("fileName");
        }

        //fragments
        homeFragment = new HomeFragment();
        healthFragment = new HealthFragment();
        gamesFragment = new GamesFragment();
        hygieneFragment = new HygieneFragment();
        foodFragment = new FoodFragment();

        replaceFragment(homeFragment);

        menuView = findViewById(R.id.bottomNavigationView);
        menuView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(homeFragment);
            } else if (itemId == R.id.health) {
                replaceFragment(healthFragment);
            } else if (itemId == R.id.games) {
                replaceFragment(gamesFragment);
            } else if (itemId == R.id.hygiene) {
                replaceFragment(hygieneFragment);
            } else if (itemId == R.id.food) {
                replaceFragment(foodFragment);
            }
            return true;

        });
    }

    /**
     * Resets listeners linked to the active simulation to {@code null}.
     * <p>
     * As the UI thread is the priority for updating {@link android.widget.ProgressBar}s, changing fragment can block
     * the scheduler in a sort of deadlock, waiting for response from {@link android.view.View}s that were destroyed during
     * fragment exchange, so the listener must therefore be removed when the fragment is changed
     * (at the same time as the animations are stopped)
     */
    public void removeListeners() {
        house.setOnTimePassedListener(null);
        house.getResident().setOnStateValueChangedListener(null);
    }

    /**
     * Retrieves JSON data for the entity in question, performs passive simulation and prepares listeners.
     * <p>
     * This method <b>must be called before starting active simulation or user interaction</b>.
     */
    private void retrieveData() {
        try {
            JSONObject data = jsonHelper.getDataFromFile(fileName);
            this.house = new House();
            house.fromJSON(data);
            LivingEntity resident = house.getResident();
            if(resident != null && resident.isAlive()) {
                State.FatigueState fatigueState = (State.FatigueState) resident.getStates().get(State.StateType.FATIGUE);
                if (fatigueState != null)
                    fatigueState.setScreenTime(Math.max(0.0, screenTime)); //even if it's 0.0 (permission not granted), it doesn't affect the simulation

                resident.setOnEntityDeadListener(deathType -> {
                    save(new DeadEntity(resident.getName(), resident.getDateOfBirth(),
                            deathType, System.currentTimeMillis()), fileName + "_death");
                    Intent intent = new Intent(HouseActivity.this, EntityDeadActivity.class);
                    intent.putExtra("fileName", fileName + "_death");
                    startActivity(intent);
                    finish();
                });
                house.simulateWithTimestamps(data.getLong("lastUpdate"), System.currentTimeMillis());
            }
        } catch (JSONException | IOException e) {
            Log.e("entity data file not found", e.toString());
            //startActivity(new Intent(MainActivity.this, ErrorActivity.class));
            finish();
        }
    }

    /**
     * Checks that all permissions (other than system permissions) are granted, and if not, asks the
     * user if he wants to grant them. Since {@link Manifest.permission#PACKAGE_USAGE_STATS} is a system permission,
     * screen time has to be retrieved in order to determine whether the permission is granted or not.
     */
    private void checkPermissions() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            //ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
            } else {
                Toast.makeText(this, "your device SDK version is too old (must be >= Q)", Toast.LENGTH_SHORT).show();
            }
        }
        boolean hasUserDenied = this.getSharedPreferences("shared_preferences", Context.MODE_PRIVATE)
                .getBoolean("USAGE_STATS_PERMISSION_DENIED", false);
        Log.v("permission", "has user already denied USAGE_STATS permission : " + hasUserDenied);
        //we need to test the retrieveScreenTime() because
        //ContextCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS) returns always -1 (permission only granted to system apps)
        if((this.screenTime = retrieveScreenTime()) == -1 && !hasUserDenied) {
            startActivity(new Intent(HouseActivity.this, PermissionsActivity.class));
        }
    }

    //N.B : please read the Google Issue Tracker 118564471 on this topic
    //https://issuetracker.google.com/issues/118564471
    //as we can read, the API seems to be inaccurate, providing wrong or not precise results
    //Google has been noticed of this issue, but even if the issue has been "passed to the dev. team" in 2019
    //there are no more update : the status was "Won't Fix (Obsolete)" in 2020 but reopened in 2023
    //as a user said in 2024 :
    //"It's not taking any-time to not fix issues. As obviously from first replies, it was not going to be fixed ever. yet another ADP"
    //so we have to deal with this API, even though it's broken, cause this is the only API that Android officially give and recommend
    //after several tests, we can conclude to a statical data shift of a few hours
    //this issue has not much impact on our application, but it's worth nothing

    /**
     * Recovers screen time over the last 24 hours. If no permission is granted, the list of statistics
     * by package must be empty. If the device is not in an unlocked state,
     * {@link UsageStatsManager#queryUsageStats(int, long, long)} returns {@code null}.
     * But if permissions are granted and the device is in an unlocked state, then this method sums up
     * the screen times for each package, filtered so as not to include system packages (e.g. Android packages).
     * The result is converted into hours.
     * @return {@code -1} if permission is not granted ;
     * {@code -2} if device is not in an unlocked state ;
     * the sum of the filtered packages screen times, in hours, if permission is granted
     */
    private double retrieveScreenTime() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        long startOfDayTime = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startOfDayTime, currentTime);
        if(stats == null) {
            Toast.makeText(this, "Device is not in an unlocked state", Toast.LENGTH_SHORT).show();
            return -2;
        } else if(stats.isEmpty()) {
            Toast.makeText(this, "Empty list, permission not granted", Toast.LENGTH_SHORT).show();
            return -1;
        }

        long totalTime = 0;
        for (UsageStats usageStats : stats) {
            String packageName = usageStats.getPackageName();
            long totalTimeForeground = usageStats.getTotalTimeInForeground();
            long lastTimeUsed = usageStats.getLastTimeUsed();
            long firstTimeStamp = usageStats.getFirstTimeStamp();
            double packageTime = (totalTimeForeground / 1000.0) / 60.0;
            if (packageTime != 0 && !packageName.matches("com\\.(sec\\.)?android.*") && lastTimeUsed > startOfDayTime) { //filtered
                totalTime += (long) packageTime;
                Log.v("onCreate", packageName + " | Time : " + packageTime +
                        " min. | Last time used : " + new Date(lastTimeUsed) + " | FTS : " + new Date(firstTimeStamp));
                Log.e("total", "total time : " + totalTime);
            }
        }
        //Toast.makeText(this, "Total screen time retrieved : " + totalTime/60.0 + " hours", Toast.LENGTH_SHORT).show();
        return totalTime/60.0;
    }

    /**
     * Saves the {@link JSONable} object in the specified file name,
     * only if the activity's {@link JSONHelper} object is not {@code null}.
     * @param object the {@link JSONable} object which will be serialized into a {@link JSONObject} in order to be saved
     * @param fileName the name of the save file
     */
    private void save(JSONable object, String fileName) {
        if(jsonHelper != null && object != null) {
            try {
                jsonHelper.saveJSONToFile(object, fileName);
            } catch (JSONException e) {
                //TODO: replace this RuntimeException with starting a new ErrorActivity
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Changes the currently displayed fragment to the specified fragment.
     * @param fragment the replacement  {@link android.app.Fragment}
     */
    public void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        // fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * Returns the screen time retrieved when checking permissions.
     * This is the time retrieved from the {@link HouseActivity#retrieveScreenTime()} call,
     * so it can have negative values, unlike the attribute of a {@link com.l2h1.tamagotchi.model.states.State.FatigueState} object.
     * @return the screen time retrieved by calling {@link HouseActivity#retrieveScreenTime()}
     */
    public double getScreenTimeRetrieved() {
        return screenTime;
    }

    /**
     * Gives the {@link BottomNavigationView} instance of this activity.
     * @return the {@link BottomNavigationView} instance of this activity
     */
    public BottomNavigationView getMenuView(){
        return menuView;
    }

    /**
     * Gives the {@link House} instance of this activity.
     * @return the {@link House} instance of this activity
     */
    public House getHouse() {
        return house;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        //activity come back to foreground (from the stopped state)
        //"Although the onPause() method is called before onStop(),
        //you should use onStop() to perform larger, more CPU intensive shut-down operations,
        //such as writing information to a database"
        //see https://stuff.mit.edu/afs/sipb/project/android/docs/training/basics/activity-lifecycle/
        super.onStart();
        Log.i("onStart()", "started");
        checkPermissions();
        retrieveData();
        house.startSimulation();
    }

    @Override
    protected void onStop() {
        //user opens the 'Recent Apps' window and switches from the app to another
        //or back button pressed
        //or activity change
        //see https://stuff.mit.edu/afs/sipb/project/android/docs/training/basics/activity-lifecycle/
        super.onStop();
        Log.i("onStop()", "stopped");
        save(house, fileName);
        house.stopSimulation();
    }
}