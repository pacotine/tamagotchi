package com.l2h1.tamagotchi.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.l2h1.tamagotchi.R;
import com.l2h1.tamagotchi.model.LivingEntity;
import com.l2h1.tamagotchi.model.House;
import com.l2h1.tamagotchi.utils.json.JSONHelper;

import org.json.JSONException;

/**
 * The activity that lets users create a Tamagotchi.
 * It displays some inputs in order to take the name of the entity and the name of its house in order to create a backup
 * JSON file and open it on a {@link HouseActivity} if no exception is raised.
 */
public class EntityCreatorActivity extends AppCompatActivity {

    private EditText name, houseName;
    private JSONHelper jsonHelper;
    private House house;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_creator);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        name = findViewById(R.id.editTextName);
        houseName = findViewById(R.id.editTextHouseName);

        Button button = findViewById(R.id.button);

        this.jsonHelper = new JSONHelper(this);
        Bundle extras = getIntent().getExtras();

        button.setOnClickListener(v->{

            if(extras != null) {
                String fileName = extras.getString("fileName");
                String entityNameStr = name.getText().toString().trim();
                String houseNameStr = houseName.getText().toString().trim();

                if(entityNameStr.isEmpty()){
                    Toast.makeText(this, "Enter Tamagotchi's name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(houseNameStr.isEmpty()){
                    Toast.makeText(this, "Enter Tamagotchi's house name", Toast.LENGTH_SHORT).show();
                    return;
                }

                this.house = new House(houseNameStr, new LivingEntity(entityNameStr,System.currentTimeMillis()));
                try {
                    jsonHelper.saveJSONToFile(house, fileName);

                    SharedPreferences.Editor sharedEditor = this.getSharedPreferences("entity_creation", Context.MODE_PRIVATE).edit();
                    sharedEditor.putBoolean("NEW_ENTITY_CREATED", true);
                    sharedEditor.apply();

                    Intent intent = new Intent(EntityCreatorActivity.this, HouseActivity.class);
                    intent.putExtra("fileName", fileName);
                    startActivity(intent);
                    finish();
                } catch (JSONException e) {
                    finishAffinity();
                }
            }

        });

    }

}