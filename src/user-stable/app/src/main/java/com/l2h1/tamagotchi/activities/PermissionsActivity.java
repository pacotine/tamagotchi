package com.l2h1.tamagotchi.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.l2h1.tamagotchi.R;

/**
 * The activity displayed to users when they create their first Tamagotchi.
 * It informs the user of the reasons for the requested permissions, and gives him the option of granting or refusing them.
 * If the user refuses, this activity will never be displayed again, in accordance with the rules defined by Android,
 * which stipulates that a user must not be forced to grant permissions of this type.
 */
public class PermissionsActivity extends AppCompatActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Button refuse = findViewById(R.id.refuse);
        refuse.setOnClickListener(e -> {
            SharedPreferences.Editor sharedEditor = this.getSharedPreferences("shared_preferences", Context.MODE_PRIVATE).edit();
            sharedEditor.putBoolean("USAGE_STATS_PERMISSION_DENIED", true);
            sharedEditor.apply();
            finish();
        });

        Button accept = findViewById(R.id.accept);
        accept.setOnClickListener(e -> {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            finish();
        });
    }
}