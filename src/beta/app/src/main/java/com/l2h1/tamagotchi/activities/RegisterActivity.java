package com.l2h1.tamagotchi.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.l2h1.tamagotchi.R;

/**
 * The activity displayed to a user who does not have a customer account in the Firebase database.
 * The user must then register in the database with an email address and a password.
 * If successful, the user is redirected to the {@link LoginActivity}.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextUsername, editTextPassword, editPasswordConfirmation;
    private TextView message;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editPasswordConfirmation = findViewById(R.id.editTextPasswordTest);

        TextView login = findViewById(R.id.login);

        message = findViewById(R.id.message);
        message.setVisibility(View.INVISIBLE);

        Button button = findViewById(R.id.button);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();



        editPasswordConfirmation.setOnEditorActionListener((v, actionId, event) -> {
            validatePassword();
            return false;
        });

        login.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        button.setOnClickListener(v -> {
            String email, password, passwordConfirmation;

            email = String.valueOf(editTextUsername.getText());
            password = String.valueOf(editTextPassword.getText());
            passwordConfirmation = String.valueOf(editPasswordConfirmation.getText());


            if(TextUtils.isEmpty(email)){
                Toast.makeText(RegisterActivity.this,"Enter email",Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(password)){
                Toast.makeText(RegisterActivity.this,"Enter password",Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(passwordConfirmation)){
                Toast.makeText(RegisterActivity.this,"Enter password confirmation",Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (validatePassword()){
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Account created",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.d("failed", "failed : " + task.getException());
                                Toast.makeText(RegisterActivity.this, "Authentication failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


        });


    }

    /**
     * Check that the password meets our requirements: the password and the confirmation password
     * must match, and the password must be at least 8 characters long.
     * @return {@code true} if password conforms, {@code false} otherwise
     */
    private boolean validatePassword(){
        String pw = String.valueOf(editTextPassword.getText()).trim();
        String pwConf = String.valueOf(editPasswordConfirmation.getText()).trim();
        boolean validated = false;
        if(!pw.equals(pwConf)){
            message.setText(R.string.passwords_don_t_match);
            message.setVisibility(View.VISIBLE);
        }
        else {
            if(pw.length()<8){
                message.setText(R.string.password_length);
                message.setVisibility(View.VISIBLE);
            }
            else{
                message.setVisibility(View.INVISIBLE);
                validated = true;
            }
        }
        return validated;

    }


}

