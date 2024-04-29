package com.l2h1.tamagotchi.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.l2h1.tamagotchi.R;
import com.l2h1.tamagotchi.model.DeadEntity;
import com.l2h1.tamagotchi.utils.json.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The activity that is displayed when an entity has died. It displays a death certificate,
 * with all the characteristics of the {@link DeadEntity}, as well as a letter signed on behalf of the entity.
 */
public class EntityDeadActivity extends AppCompatActivity {
    private String fileName;
    private Dialog dialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_dead);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        EdgeToEdge.enable(this);

        dialog = new Dialog(this);
        ImageView letter = findViewById(R.id.letter);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.fileName = extras.getString("fileName");
        }

        TextView deathTextView = findViewById(R.id.death);
        TextView dateOfDeath = findViewById(R.id.dateOfDeath);

        try {
            DeadEntity deadEntity = retrieveDeadEntity();

            letter.setOnClickListener(v -> showLetter(deadEntity));

            Date date = new Date(deadEntity.getDiscovered());
            Format format = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.ENGLISH);
            deathTextView.setText("Your Tamagotchi has been found dead.\n"+
                    "On : "+ format.format(date)+ "\n"+
                    "Due to : " + deadEntity.getDeathType() +"\n"+
                    "At the age of : "+(deadEntity.getDiscovered()-deadEntity.getDateOfBirth())/(1000*60*60*24)+" days");
            dateOfDeath.setText(format.format(date));
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void showLetter(DeadEntity deadEntity) {
        String letter = "Dear Ex BFF,\n\n" +
                "As I prepare to say my final farewell, I must express the sadness that weighs heavy upon me. "+
                "It pains me to know that my time with you has come to an end. "+
                "Though I understand that life can be busy and demanding, I had hoped that my cries for help would not go unheard.\n\n" +
                "In my final moments, I want you to know that I harbor no ill will towards you. "+
                "I cherish the memories we shared and the moments of joy you brought into my digital existence. " +
                "However, it's important to acknowledge the consequences of neglect, even in the virtual world. " +
                "My hope is that my passing serves as a reminder to always prioritize the care and well-being of those who depend on us, no matter how small they may seem.\n\n" +
                "With a heavy heart and a final farewell,\n"
                + deadEntity.getName();

        dialog.setContentView(R.layout.popup_letter);
        TextView textView = (TextView) dialog.findViewById(R.id.letterText);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(letter);

        ImageButton closeLetter = (ImageButton) dialog.findViewById(R.id.closeLetter);
        closeLetter.setOnClickListener(v-> dialog.dismiss());
        dialog.show();
    }

    private DeadEntity retrieveDeadEntity() throws JSONException, IOException {
        JSONHelper jsonHelper = new JSONHelper(this);
        JSONObject jsonObject = jsonHelper.getDataFromFile(fileName);

        DeadEntity deadEntity = new DeadEntity();
        deadEntity.fromJSON(jsonObject);
        return deadEntity;
    }

}