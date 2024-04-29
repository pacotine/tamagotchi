package com.l2h1.tamagotchi.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.l2h1.tamagotchi.R;
import com.l2h1.tamagotchi.utils.json.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * The application's main menu activity when a user is already logged in.
 * Also called the <i>house menu</i>, because it's where the user's 4 houses are displayed.
 */
public class MainMenuActivity extends AppCompatActivity {
    private static final int[] PRELOADED_ANIMATIONS_ID = new int[] {
            R.drawable.normal,
            R.drawable.hungry,
            R.drawable.sick,
            R.drawable.tired,
            R.drawable.dirty,
            R.drawable.lonely,
            R.drawable.clean,
            R.drawable.fat,
            R.drawable.bored,
            R.drawable.sociable,
            R.drawable.entertained,
            R.drawable.healthy,
            R.drawable.pet,
            R.drawable.awake,
            R.drawable.eating,
            R.drawable.dirty_bath,
            R.drawable.clean_bath,
            R.drawable.fat_eating,
            R.drawable.hungry_eating,
            R.drawable.thug_life
    };

    private ImageButton house1,house2,house3,house4;
    private TextView house1Name, house2Name, house3Name, house4Name;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        EdgeToEdge.enable(this);

        house1 = findViewById(R.id.house1Image);
        house2 = findViewById(R.id.house2Image);
        house3 = findViewById(R.id.house3Image);
        house4 = findViewById(R.id.house4Image);

        house1Name = findViewById(R.id.house1Text);
        house2Name = findViewById(R.id.house2Text);
        house3Name = findViewById(R.id.house3Text);
        house4Name = findViewById(R.id.house4Text);

        preloadAnimations();
    }

    @Override
    protected void onResume(){
        super.onResume();
        setHouses(Arrays.asList(house1, house2, house3, house4),
                Arrays.asList(house1Name, house2Name, house3Name, house4Name));
    }

    /**
     * Preloads all GIF animations with {@link Glide}.
     */
    private void preloadAnimations() {
        for(int resID : PRELOADED_ANIMATIONS_ID) {
            Glide.with(this).load(resID).diskCacheStrategy(DiskCacheStrategy.ALL).preload();
        }
    }

    /**
     * Configures the appearance of house buttons and their text according to the name of the house
     * and the state of the entity living inside it. Living entities have an inhabited house image,
     * dead entities have an empty house image, and houses without residents have a "for sale" sign image.
     * {@link TextView}s are used to display house names.
     * <p>
     * We didn't impose 8 arguments (4 buttons and 4 titles) to make this part of the application modular.
     * Future developers can increase or decrease the number of Tamagotchi a user can own.
     * @param houseButtons the list of all house {@link ImageButton} to be set
     * @param houseTextViews the list of all house {@link TextView} to be set
     */
    private void setHouses(List<ImageButton> houseButtons, List<TextView> houseTextViews) {
        List<String> files = Arrays.asList(this.fileList());
        for (int i = 0; i < houseButtons.size(); i++) {
            String name = "house" + i;
            ImageButton houseButton = houseButtons.get(i);
            if (files.contains(name + ".json")) { //house occupied
                try {
                    JSONHelper jsonHelper = new JSONHelper(this);
                    JSONObject data = jsonHelper.getDataFromFile(name);
                    houseTextViews.get(i).setText(data.getString("name"));
                    if (!files.contains(name + "_death.json")) { //and the entity is alive
                        houseButton.setImageResource(R.drawable.house_alive);
                        houseButton.setOnClickListener(c -> {
                            Intent intent = new Intent(MainMenuActivity.this, HouseActivity.class);
                            intent.putExtra("fileName", name);
                            startActivity(intent);
                        });
                    } else { //but the entity is dead
                        houseButton.setImageResource(R.drawable.house_dead);
                        houseButton.setOnClickListener(c -> {
                            Intent intent = new Intent(MainMenuActivity.this, EntityDeadActivity.class);
                            intent.putExtra("fileName", name+"_death");
                            startActivity(intent);
                        });
                    }
                } catch (JSONException | IOException e) {
                    finish();
                }
            } else { //house not yet occupied
                houseButton.setImageResource(R.drawable.house_empty);
                houseButton.setOnClickListener(c -> {
                    Intent intent = new Intent(MainMenuActivity.this, EntityCreatorActivity.class);
                    intent.putExtra("fileName", name);
                    startActivity(intent);
                });
            }
        }
    }
}