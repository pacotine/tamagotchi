package com.l2h1.tamagotchi.model;

import com.l2h1.tamagotchi.utils.json.JSONable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An <i>entity</i> is the most abstract structure used to represent a Tamagotchi.
 * It provides a name and a liveliness.
 * Entities extending this class can be converted to and from JSON format
 * thanks to its implementation of {@link JSONable}.
 * <p>
 * Each entity has its own set of attributes. It is therefore preferable to use the {@link JSONable}
 * interface to add this information at serialization time:
 * <pre>{@code
 *      @Override
 *      public JSONObject toJSONObject() throws JSONException {
 *          JSONObject jsonEntity = super.toJSONObject();
 *          //your logic...
 *      }
 * }
 * </pre>
 * and at deserialization time :
 *<pre>{@code
 *      @Override
 *      public void fromJSON(@NonNull JSONObject object) throws JSONException {
 *         super.fromJSON(object);
 *         //your logic...
 *      }
 *}
 *</pre>
 *
 * @see LivingEntity
 * @see DeadEntity
 */
public abstract class Entity implements JSONable {
    protected String name;
    protected boolean isAlive;
    protected long dateOfBirth;

    /**
     * Constructs an {@link Entity} with the specified name, alive status, and date of birth.
     * @param name the name of the entity
     * @param isAlive the alive status of the entity
     * @param dateOfBirth the date of birth of the entity
     */
    public Entity(String name, boolean isAlive, long dateOfBirth) {
        this.name = name;
        this.isAlive = isAlive;
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Retrieves the name of the entity.
     *
     * @return the name of the entity
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the entity.
     *
     * @param name the new name of the entity
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Checks if the entity is alive.
     *
     * @return {@code true} if the entity is alive, {@code false} otherwise
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Sets the alive status of the entity.
     *
     * @param alive the new alive status of the entity
     */
    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Converts the entity to a {@link JSONObject} representation.
     *
     * @return a {@link JSONObject} representing the entity
     * @throws JSONException if there is an error during JSON serialization
     * @see JSONable#toJSONObject()
     * @see Entity
     */
    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonEntity = new JSONObject();
        jsonEntity.put("name", name);
        jsonEntity.put("isAlive", isAlive);
        jsonEntity.put("dateOfBirth",dateOfBirth);

        return jsonEntity;
    }

    /**
     * Populates the entity's fields from the provided {@link JSONObject}.
     *
     * @param object the {@link JSONObject} containing the data to populate the entity with
     * @throws JSONException if there is an error during JSON deserialization
     * @see JSONable#fromJSON(JSONObject)
     * @see Entity
     */
    @Override
    public void fromJSON(JSONObject object) throws JSONException {
        this.name = object.getString("name");
        this.isAlive = object.getBoolean("isAlive");
        this.dateOfBirth = object.getLong("dateOfBirth");
    }
}
