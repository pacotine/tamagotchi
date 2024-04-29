package com.l2h1.tamagotchi.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.l2h1.tamagotchi.R;

/**
 * The application main's activity. This is the first activity created when the application starts up.
 * This activity is not intended to be visible to the user, as it redirects the user directly either
 * to the {@link RegisterActivity} (if no Firebase user is registered on this device)
 * or to the {@link MainMenuActivity} (if the user has already signed in to Firebase).
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(MainActivity.this, MainMenuActivity.class));
            finish();
        } else {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            finish();
        }
    }
}