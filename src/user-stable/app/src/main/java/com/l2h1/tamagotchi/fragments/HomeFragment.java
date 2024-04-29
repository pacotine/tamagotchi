package com.l2h1.tamagotchi.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BulletSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.l2h1.tamagotchi.R;
import com.l2h1.tamagotchi.activities.HouseActivity;
import com.l2h1.tamagotchi.model.House;
import com.l2h1.tamagotchi.model.LivingEntity;
import com.l2h1.tamagotchi.model.listeners.OnStateValueChangedListener;
import com.l2h1.tamagotchi.model.states.State;

import java.util.Calendar;
import java.util.Locale;

/**
 * Main fragment of the {@link HouseActivity}, displayed by default when the activity is created.
 * This is the fragment displaying the entity's gauges.
 * Only in this fragment are sensors activated when available (for step detection or shake detection).
 */
public class HomeFragment extends Fragment implements SensorEventListener {
    private ImageView healthImageView, hungerImageView,
            entertainmentImageView, sociabilityImageView, hygieneImageView,
            fatigueImageView, mainImageView;

    private ProgressBar healthProgressBar, hungerProgressBar, entertainmentProgressBar,
            hygieneProgressBar, sociabilityProgressBar, fatigueProgressBar;

    private TextView healthTextView, hungerTextView, entertainmentTextView, hygieneTextView,
            sociabilityTextView, fatigueTextView, nameView, houseNameView;
    private ConstraintLayout homeLayout;
    private SensorManager sensorManager;
    private Sensor sensorShake, sensorStep;
    private static final int SENSOR_ACCELERATION = 12;
    private Dialog dialog;
    private HouseActivity context;

    /**
     * Default empty constructor, required for replacement management.
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //it's temporary, either we don't set the value directly, or we use a TextView other than the ProgressBar title
        healthTextView = view.findViewById(R.id.text_view_health);
        hungerTextView = view.findViewById(R.id.text_view_hunger);
        fatigueTextView = view.findViewById(R.id.text_view_fatigue);
        entertainmentTextView = view.findViewById(R.id.text_view_entertainment);
        sociabilityTextView = view.findViewById(R.id.text_view_sociability);
        hygieneTextView = view.findViewById(R.id.text_view_hygiene);
        // ---------------------------------------------------------------------------------------//

        healthImageView = view.findViewById(R.id.image_view_health);
        hungerImageView = view.findViewById(R.id.image_view_hunger);
        fatigueImageView = view.findViewById(R.id.image_view_fatigue);
        entertainmentImageView = view.findViewById(R.id.image_view_entertainment);
        sociabilityImageView = view.findViewById(R.id.image_view_sociability);
        hygieneImageView = view.findViewById(R.id.image_view_hygiene);
        mainImageView = view.findViewById(R.id.image_view_main);

        healthProgressBar = view.findViewById(R.id.health_progress_bar);
        hungerProgressBar = view.findViewById(R.id.hunger_progress_bar);
        fatigueProgressBar = view.findViewById(R.id.fatigue_progress_bar);
        entertainmentProgressBar = view.findViewById(R.id.entertainment_progress_bar);
        sociabilityProgressBar = view.findViewById(R.id.sociability_progress_bar);
        hygieneProgressBar = view.findViewById(R.id.hygiene_progress_bar);

        nameView = view.findViewById(R.id.name);
        houseNameView = view.findViewById(R.id.houseName);

        homeLayout = view.findViewById(R.id.home_layout);

        //instead of retrieving an instance of House from the context (retrieved when onAttached was called)
        //it's better to retrieve a single final instance from the context and call, via the HouseService
        //interface, getHouse() in order to be able to retrieve the value of the House and resident
        //in question each time it's updated from the HouseActivity (context)
        this.context = (HouseActivity) requireContext();

        sensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
        sensorShake = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorStep = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR); //should show a Log.e if the user denied "android.permission.ACTIVITY_RECOGNITION"

        dialog = new Dialog(context);
        ImageButton instructionsButton = view.findViewById(R.id.instructionsButton);
        instructionsButton.setOnClickListener(v -> showInstructions());

        if(context.getSharedPreferences("entity_creation", Context.MODE_PRIVATE)
                .getBoolean("NEW_ENTITY_CREATED", true)){
            showInstructions();
        }
    }

    /**
     * Increases the entity's sociability ({@link com.l2h1.tamagotchi.model.states.State.SociabilityState})
     * by {@link State#PET_VALUE} to represent the bonus for petting its companion.
     */
    private void pet(){
        LivingEntity resident = context.getHouse().getResident();
        State sociabilityState = resident.getStates().get(State.StateType.SOCIABILITY);
        if(sociabilityState != null) {
            Glide.with(context).load(R.drawable.normal).into(mainImageView);
            sociabilityState.increase(State.PET_VALUE);
            resident.checkStates();
            updateProgressBars();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this); //deactivate all sensors

        //as the UI thread had become the priority for updating ProgressBars, changing fragment blocked the scheduler in a sort of deadlock
        //waiting for response from views that were destroyed during fragment exchange
        //so the listener must therefore be removed when the fragment is changed (at the same time as the animations are stopped)
        context.removeListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        setBackground();
        initProgressBars();
        initAnimations();
        initTouchListener();
    }

    /**
     * Sets the {@link HomeFragment} background according to the time of day.
     */
    private void setBackground() {
        //background changes according to daytime/nighttime
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if(timeOfDay >= 8 && timeOfDay <= 19)
            homeLayout.setBackgroundResource(R.drawable.day);
        else
            homeLayout.setBackgroundResource(R.drawable.night);
    }

    /**
     * Initialize the {@link android.view.View.OnTouchListener} on the central animation,
     * to enable the action of "caressing" (rubbing your finger over the animation) and reward this action after 3s.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initTouchListener() {
        //allowing the user to pet the main Tamagotchi
        //this action improves a little bit the sociability state
        mainImageView.setOnTouchListener(new View.OnTouchListener() {
            final Handler handler = new Handler();
            final Runnable longPressedRunnable = () -> pet();
            boolean run = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        if (!run) {
                            handler.postDelayed(longPressedRunnable, 3000);
                            Glide.with(context).load(R.drawable.pet).into(mainImageView);
                            run = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        run = false;
                        Glide.with(context).load(R.drawable.normal).into(mainImageView);
                        handler.removeCallbacks(longPressedRunnable);
                        v.performClick();
                        break;
                }
                return true;
            }
        });
    }

    /**
     * Initializes {@link ProgressBar}s display, according to the value of each entity state, and listeners.
     */
    @SuppressLint("SetTextI18n")
    private void initProgressBars() {
        updateProgressBars();

        House house = context.getHouse();
        LivingEntity resident = house.getResident();
        nameView.setText(resident.getName());
        houseNameView.setText("in " + house.getName());

        healthImageView.setOnClickListener(v -> {
            context.replaceFragment(new HealthFragment());
            context.getMenuView().setSelectedItemId(R.id.health);
        });

        hungerImageView.setOnClickListener(v -> {
            context.replaceFragment(new FoodFragment());
            context.getMenuView().setSelectedItemId(R.id.food);
        });

        entertainmentImageView.setOnClickListener(v -> {
            context.replaceFragment(new GamesFragment());
            context.getMenuView().setSelectedItemId(R.id.games);
        });

        hygieneImageView.setOnClickListener(v -> {
            context.replaceFragment(new HygieneFragment());
            context.getMenuView().setSelectedItemId(R.id.hygiene);
        });

        fatigueImageView.setOnClickListener(v ->  {
            double screenTime = context.getScreenTimeRetrieved();
            String screenTimeText = screenTime < 0 ? "We don't have the permission to do that :("
                    : String.format(Locale.ENGLISH, "Screen time detected : %.2f", screenTime);
            Toast.makeText(context, screenTimeText, Toast.LENGTH_SHORT).show();
        });
        //do NOT forget to run UI updates on the UI thread !
        house.setOnTimePassedListener(e -> context.runOnUiThread(this::updateProgressBars));
    }

    /**
     * Initializes animations according to the value of each entity state, and listeners.
     */
    private void initAnimations() {
        LivingEntity resident = context.getHouse().getResident();

        //-------------------//
        //init sensors
        if(resident.hasCriticalState(State.CriticalType.OVERWEIGHT))
            sensorManager.registerListener(this, sensorStep, SensorManager.SENSOR_DELAY_NORMAL); //start detecting steps

        State fatigueState = resident.getStates().get(State.StateType.FATIGUE);
        if(fatigueState != null && fatigueState.getValue() < 350.0) {
            sensorManager.registerListener(this, sensorShake, SensorManager.SENSOR_DELAY_NORMAL); //start detecting shakes
        }
        //-------------------//

        Glide.with(context).load(resident.hasCriticalState(State.CriticalType.HUNGRY)
                ? R.drawable.hungry : (resident.hasCriticalState(State.CriticalType.OVERWEIGHT)
                ? R.drawable.fat : R.drawable.normal)).into(hungerImageView);
        Glide.with(context).load(resident.hasCriticalState(State.CriticalType.SICK)
                ? R.drawable.sick : R.drawable.healthy).into(healthImageView);
        Glide.with(context).load(resident.hasCriticalState(State.CriticalType.TIRED)
                ? R.drawable.tired : R.drawable.awake).into(fatigueImageView);
        Glide.with(context).load(resident.hasCriticalState(State.CriticalType.DIRTY)
                ? R.drawable.dirty : R.drawable.clean).into(hygieneImageView);
        Glide.with(context).load(resident.hasCriticalState(State.CriticalType.LONELY)
                ? R.drawable.lonely : R.drawable.sociable).into(sociabilityImageView);
        Glide.with(context).load(resident.hasCriticalState(State.CriticalType.BORED)
                ? R.drawable.bored : R.drawable.entertained).into(entertainmentImageView);

        Glide.with(context).load(R.drawable.normal).into(mainImageView); //not 'this', only 'context'

        resident.setOnStateValueChangedListener(new OnStateValueChangedListener() {
            @Override
            public void onStateCritical(State.CriticalType type) {
                context.runOnUiThread(() -> {
                    switch(type) {
                        case SICK:
                            Glide.with(context).load(R.drawable.sick).into(healthImageView);
                            break;
                        case HUNGRY:
                            Glide.with(context).load(R.drawable.hungry).into(hungerImageView);
                            break;
                        case TIRED:
                            Glide.with(context).load(R.drawable.tired).into(fatigueImageView);
                            break;
                        case DIRTY:
                            Glide.with(context).load(R.drawable.dirty).into(hygieneImageView);
                            break;
                        case LONELY:
                            Glide.with(context).load(R.drawable.lonely).into(sociabilityImageView);
                            break;
                        case OVERWEIGHT:
                            Glide.with(context).load(R.drawable.fat).into(hygieneImageView);
                            break;
                        case BORED:
                            Glide.with(context).load(R.drawable.bored).into(entertainmentImageView);
                            break;
                        default:
                            break;
                    }
                });
            }

            @Override
            public void onStateStable(State.StateType type) {
                switch(type) {
                    case HEALTH:
                        Glide.with(context).load(R.drawable.healthy).into(healthImageView);
                        break;
                    case HUNGER:
                        Glide.with(context).load(R.drawable.normal).into(hungerImageView);
                        sensorManager.unregisterListener(HomeFragment.this, sensorStep); //stop detecting steps
                        break;
                    case FATIGUE:
                        Glide.with(context).load(R.drawable.awake).into(fatigueImageView);
                        break;
                    case HYGIENE:
                        Glide.with(context).load(R.drawable.clean).into(hygieneImageView);
                        break;
                    case SOCIABILITY:
                        Glide.with(context).load(R.drawable.sociable).into(sociabilityImageView);
                        break;
                    case ENTERTAINMENT:
                        Glide.with(context).load(R.drawable.entertained).into(entertainmentImageView);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * Updates the display of {@link ProgressBar}s according to the value of each entity state.
     * The value of the {@link ProgressBar} itself and the text ({@link TextView}) displayed below it are updated.
     */
    private void updateProgressBars() {
        //we update the ProgressBar value and the text here
        //a reminder: the decrement values for each tick are so small
        //that the visual result is not very obvious (unlike the text)
        //perhaps we should consider not updating the ProgressBar every tick?
        context.getHouse().getResident().getStates().forEach((type, state) -> {
            switch(type) {
                case HEALTH:
                    healthProgressBar.setProgress((int) state.getValue());
                    healthTextView.setText(String.format(Locale.ENGLISH, "\uD83D\uDC8A %.2f", state.getValue()));
                    break;
                case HUNGER:
                    hungerProgressBar.setProgress((int) state.getValue());
                    hungerTextView.setText(String.format(Locale.ENGLISH, "\uD83C\uDF7D️ %.2f", state.getValue()));
                    break;
                case FATIGUE:
                    fatigueProgressBar.setProgress((int) state.getValue());
                    fatigueTextView.setText(String.format(Locale.ENGLISH, "\uD83D\uDCA4 %.2f", state.getValue()));
                    break;
                case HYGIENE:
                    hygieneProgressBar.setProgress((int) state.getValue());
                    hygieneTextView.setText(String.format(Locale.ENGLISH, "\uD83E\uDDFC\uD83D\uDEC1 %.2f", state.getValue()));
                    break;
                case SOCIABILITY:
                    sociabilityProgressBar.setProgress((int) state.getValue());
                    sociabilityTextView.setText(String.format(Locale.ENGLISH, "\uD83D\uDC65 %.2f", state.getValue()));
                    break;
                case ENTERTAINMENT:
                    entertainmentProgressBar.setProgress((int) state.getValue());
                    entertainmentTextView.setText(String.format(Locale.ENGLISH, "\uD83C\uDFAE %.2f", state.getValue()));
                    break;

                default: break;
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        LivingEntity resident = context.getHouse().getResident();
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && resident != null) {
            float x_acc1 = event.values[0];
            float y_acc1 = event.values[1];

            if(Math.abs(x_acc1) > SENSOR_ACCELERATION || Math.abs(y_acc1) > SENSOR_ACCELERATION) {
                State fatigueState = resident.getStates().get(State.StateType.FATIGUE);
                if(fatigueState != null) {
                    if(fatigueState.getValue()<350.0){ //only able to increase up to 350
                        fatigueState.increase(10.0);
                        resident.checkStates();
                        updateProgressBars();
                    }
                    else{
                        sensorManager.unregisterListener(HomeFragment.this, sensorShake); //stop detecting shakes
                    }
                }
            }
        } else if(event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR && resident != null) {
            //the only return value is 1.0 :
            //float steps = event.values[0];
            State hungerState = resident.getStates().get(State.StateType.HUNGER);
            if(hungerState != null) {
                hungerState.decrease(10.0); //1 step = 10 hunger point less
                resident.checkStates();
                updateProgressBars();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //unused
    }

    /**
     * Displays a {@link Dialog} detailing all game instructions.
     */
    private void showInstructions(){
        ImageButton closePopup;
        TextView textView;
        SpannableStringBuilder instructions = new SpannableStringBuilder();
        String[] bulletPoints = {
                "There are 6 states : health, hunger, fatigue, entertainment, sociability and hygiene. Each one varies from 0 to 500.\n",
                "Your goal is to keep every state at its optimal rate. For every state, you must keep the rate the highest possible.\n",
                "Except for the hunger state. If it goes past 400, the Tamagotchi will become obese. In that case, walk, with the app open, to reduce its weight.\n",
                "Fatigue varies according to your screen time. If you use your phone excessively your virtual friend will not sleep well. Shake your phone to reduce its fatigue.\n",
                "Pet your tamagotchi to increase its sociability.\n",
                "If the rate of a given state is below 200, it will be in a critical state. Take care of it accordingly.\n",
                "And most importantly, HAVE FUN!"

        };
        for (String point : bulletPoints) {
            SpannableString bulletPoint = new SpannableString(point);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bulletPoint.setSpan(new BulletSpan(20, Color.BLACK, 6), 0, bulletPoint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            instructions.append(bulletPoint);
            }


        dialog.setContentView(R.layout.popup_instructions);
        Window window = dialog.getWindow();
        if(window != null) window.setBackgroundDrawableResource(R.color.transparent);
        closePopup = (ImageButton) dialog.findViewById(R.id.closePopup);
        textView = (TextView) dialog.findViewById(R.id.instructions);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(instructions);

        closePopup.setOnClickListener(v-> dialog.dismiss());
        dialog.show();
        SharedPreferences.Editor sharedEditor = context.getSharedPreferences("entity_creation", Context.MODE_PRIVATE).edit();
        sharedEditor.putBoolean("NEW_ENTITY_CREATED", false);
        sharedEditor.apply();

    }

}