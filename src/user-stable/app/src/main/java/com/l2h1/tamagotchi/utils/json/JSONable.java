package com.l2h1.tamagotchi.utils.json;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An interface representing objects that can be serialized to and deserialized from JSON format.
 * Implementing classes should provide methods to convert the object to a JSON representation
 * and to populate the object's fields from a JSON object.
 */
public interface JSONable {
    /**
     * Converts the object to a {@link JSONObject} representation.
     *
     * @return a {@link JSONObject} representing the state of the object
     * @throws JSONException if there is an error during JSON serialization
     */
    JSONObject toJSONObject() throws JSONException;

    /**
     * Populates the object's fields from the provided {@link JSONObject}.
     *
     * @param object the JSONObject containing the data to populate the object with
     * @throws JSONException if there is an error during JSON deserialization
     */
    void fromJSON(JSONObject object) throws JSONException;
}
