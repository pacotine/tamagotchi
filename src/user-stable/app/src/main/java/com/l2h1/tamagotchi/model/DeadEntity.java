package com.l2h1.tamagotchi.model;

import com.l2h1.tamagotchi.model.states.State;
import com.l2h1.tamagotchi.utils.json.JSONable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class representing a <i>dead entity</i>, which extends the abstract {@link Entity} class.
 * Dead entities encapsulate a set of properties such as the date of discovery of this state or the cause of the death.
 * But this class, just as the backend logic, has been made as modular as possible,
 * so that any future developer can add, modify or remove a property at will, or modify its simulation logic.
 * @see Entity
 * @see LivingEntity
 */
public class DeadEntity extends Entity {

    private State.DeathType deathType;
    private long discovered;

    /**
     * Constructs a {@link DeadEntity} with the specified name.
     *
     * @param name the name of the living entity
     * @param dateOfBirth the date of birth of the living entity (as a timestamp)
     * @param deathType the {@link com.l2h1.tamagotchi.model.states.State.DeathType} of this entity
     * @param discovered the timestamp corresponding to the moment when the entity is found dead (the moment when the dead entity is created)
     */
    public DeadEntity(String name, long dateOfBirth, State.DeathType deathType, long discovered) {
        super(name, false, dateOfBirth);
        this.deathType = deathType;
        this.discovered = discovered;
    }

    /**
     * Constructs a {@link DeadEntity} with an empty name and a 0 value date of birth.
     */
    public DeadEntity() {
        super("", false,0);
    }

    /**
     * Retrieves the {@link com.l2h1.tamagotchi.model.states.State.DeathType} of the entity.
     * @return the {@link com.l2h1.tamagotchi.model.states.State.DeathType} of the entity
     */
    public State.DeathType getDeathType() {
        return deathType;
    }

    /**
     * Sets the {@link com.l2h1.tamagotchi.model.states.State.DeathType} of the entity.
     * @param deathType the new {@link com.l2h1.tamagotchi.model.states.State.DeathType} of the entity
     */
    public void setDeathType(State.DeathType deathType) {
        this.deathType = deathType;
    }

    /**
     * Retrieves the discovery timestamp of the entity.
     * @return the discovery timestamp of the entity
     */
    public long getDiscovered() {
        return discovered;
    }

    /**
     * Sets the discovery timestamp of the entity.
     * @param timestamp the new discovery timestamp
     */
    public void setTimestamp(long timestamp) {
        this.discovered = timestamp;
    }

    /**
     * Converts the dead entity to a {@link JSONObject} representation.
     *
     * @return a {@link JSONObject} representing the dead entity
     * @throws JSONException if there is an error during JSON serialization
     * @see JSONable#toJSONObject()
     * @see Entity
     */
    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonDeath = super.toJSONObject();

        jsonDeath.put("deathType", deathType.name());
        jsonDeath.put("deathTimestamp", discovered);

        return jsonDeath;
    }

    /**
     * Populates the dead entity's fields from the provided {@link JSONObject}.
     *
     * @param object the {@link JSONObject} containing the data to populate the dead entity with
     * @throws JSONException if there is an error during JSON deserialization
     * @see JSONable#fromJSON(JSONObject)
     * @see Entity
     */
    @Override
    public void fromJSON(JSONObject object) throws JSONException {
        super.fromJSON(object);

        this.deathType = State.DeathType.valueOf(object.getString("deathType"));
        this.discovered = object.getLong("deathTimestamp");
    }
}
