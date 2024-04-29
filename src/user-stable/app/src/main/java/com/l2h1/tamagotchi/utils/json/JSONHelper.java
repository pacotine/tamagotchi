package com.l2h1.tamagotchi.utils.json;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A utility class for writing and reading JSON objects.
 * The utility needs a {@link Context} object in order to retrieve {@link FileInputStream} and {@link FileOutputStream}
 * associated with this context's application package.
 */
public class JSONHelper {
    private final Context context;

    /**
     * Constructs a {@link JSONHelper} utility object with the specified {@link Context} object.
     * @param context an application {@link Context} such an {@link android.app.Activity}
     */
    public JSONHelper(Context context) {
        this.context = context;
    }

    /**
     * Saves the specified {@link JSONable} object as {@link JSONObject} in a file of the specified name.
     * @param object the {@link JSONable} object which will be serialized into a {@link JSONObject} in order to be saved
     * @param fileName the name of the save file
     * @throws JSONException if there is an error during JSON serialization
     */
    public void saveJSONToFile(@NonNull JSONable object, String fileName) throws JSONException {
        String dataString = object.toJSONObject().toString(2);
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName + ".json", Context.MODE_PRIVATE);
            fileOutputStream.write(dataString.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the data as {@link JSONObject} from the file of the specified name.
     * @param fileName the name of the file to be retrieved
     * @return the data from the file parsed into {@link JSONObject}
     * @throws JSONException if the parse fails or doesn't yield a JSONObject
     * @throws IOException if an I/O error occurs during reading
     */
    public JSONObject getDataFromFile(String fileName) throws JSONException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        FileInputStream fileInputStream = context.openFileInput(fileName + ".json");
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        fileInputStream.close();

        return new JSONObject(stringBuilder.toString());
    }


}
