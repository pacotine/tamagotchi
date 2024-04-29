package com.l2h1.tamagotchi.model;

import androidx.annotation.NonNull;

import com.l2h1.tamagotchi.model.listeners.OnEntityDeadListener;
import com.l2h1.tamagotchi.model.listeners.OnStateValueChangedListener;
import com.l2h1.tamagotchi.model.states.State;
import com.l2h1.tamagotchi.utils.json.JSONable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A class representing a <i>living entity</i>, which extends the abstract {@link Entity} class.
 * Living entities encapsulate various states such as health, hunger, entertainment, hygiene, sociability, and fatigue.
 * But this class, just as the backend logic, has been made as modular as possible,
 * so that any future developer can add, modify or remove a state at will, or modify its simulation logic.
 * <p>
 * Living entities provide functionality to manage and monitor their states, as well as methods
 * for simulation, JSON serialization and states values listeners.
 * @see Entity
 * @see State
 */
public class LivingEntity extends Entity {
    private final Map<State.StateType, State> states;
    //rev. 40 : instead of having a set of critical types that the entity has,
    //we use a set of states that are not stable (this choice is justified in more detail in checkStates())
    private final Set<State> criticalStates;
    private OnStateValueChangedListener onStateValueChangedListener;
    private OnEntityDeadListener onEntityDeadListener;

    /**
     * Constructs a {@link LivingEntity} with the specified name and date of birth.
     *
     * @param name the name of the living entity
     * @param dateOfBirth the date of birth of the living entity (as a timestamp)
     */
    public LivingEntity(String name, long dateOfBirth) {
        super(name, true, dateOfBirth);
        this.criticalStates = new HashSet<>();

        this.states = new HashMap<>();
        states.put(State.StateType.HEALTH, new State.HealthState());
        states.put(State.StateType.HUNGER, new State.HungerState());
        states.put(State.StateType.ENTERTAINMENT, new State.EntertainmentState());
        states.put(State.StateType.HYGIENE, new State.HygieneState());
        states.put(State.StateType.SOCIABILITY, new State.SociabilityState());
        states.put(State.StateType.FATIGUE, new State.FatigueState());
    }

    /**
     * Constructs a {@link LivingEntity} with an empty name and a 0 value date of birth.
     */
    public LivingEntity() {
        super("", true,0);
        this.criticalStates = new HashSet<>();
        this.states = new HashMap<>();
    }

    /**
     * Starts the verification procedure for all entity states. Verification consists of the following sequence:
     * <ul>
     * <li> check whether each state has a critical value
     * <li> add each critical state to the entity's list of critical states; respectively, remove each stable state from the entity's list of critical states
     * <li> call {@link OnStateValueChangedListener#onStateCritical(State.CriticalType)} on the type that has become critical;
     * respectively, call {@link OnStateValueChangedListener#onStateStable(State.StateType)} on the type that has become stable again
     * <li> check whether one of the states is killing the entity
     * <li> call {@link OnEntityDeadListener#onEntityDead(State.DeathType)} on the state that has killed the entity
     * </ul>
     *
     * <code>checkStates()</code> must be called every time the entity needs to be updated for its states,
     * whether during a simulation or after a simple change of state value.
     * The method must also be called if you wish to check that the entity is still alive,
     * especially if an {@link OnEntityDeadListener} is waiting for the entity death to perform an action.
     * @see State
     * @see State#isValueCritical()
     * @see State#isOver()
     */
    public void checkStates() {
        for(State state : states.values()) {
            if(state.isValueCritical()) {
                if(criticalStates.add(state) && onStateValueChangedListener != null) {onStateValueChangedListener.onStateCritical(state.getCriticalType());}
            } else {
                //bug fixed here: some states, such as hunger, can have several critical types,
                //but you can't know which critical type the state was in when it becomes stable again;
                //having the list of non-stable states is more preferable, as you only need to check
                //if the value of the state is no longer critical to consider it stable:
                //the question is no longer "which type of critical state should I remove?"
                //(which was impossible to determinate for states with multiple critical types) but rather
                //"which state became stable again?" (which is verifiable regardless of the type of critical state)
                if(criticalStates.remove(state) && onStateValueChangedListener != null) {onStateValueChangedListener.onStateStable(state.getType());}
            }
            if(state.isOver()) {
                if(onEntityDeadListener != null && isAlive) onEntityDeadListener.onEntityDead(state.getDeathType()); //must be called when the first death is detected
                isAlive = false;
            }
        }
    }

    public void passTime() { // = passTime(15) for 1 tick
        passTime(15);
    }

    /**
     * Represents the passage of time for all entity states for given seconds.
     * Note once again that the variations in the values of the entity's states are a function of ticks;
     * and that <b>one tick = 15 seconds</b>.
     * Calling this method for a time of less than 15 seconds will therefore produce nothing.
     * <p>
     * During simulation, whether passive or active, the passage of time is simulated in such a way
     * as to decrease the values of each state of the entity according to each tick passed.
     * The malus generated by critical states is also taken into account,
     * so that with each passage of time, the values of the entity's states
     * drop more or less according to the entity's critical states.
     * @param secs the number of seconds representing the life simulation time passed by the entity
     */
    public void passTime(double secs) {
        int ticks = (int) secs/15;
        if(ticks > 0) {
            for (State state : states.values()) {
                state.decreaseTick(ticks); //natural decrease
            }

            State health = Objects.requireNonNull(states.get(State.StateType.HEALTH));
            State entertainment = Objects.requireNonNull(states.get(State.StateType.ENTERTAINMENT));
            State hygiene = Objects.requireNonNull(states.get(State.StateType.HYGIENE));
            State sociability = Objects.requireNonNull(states.get(State.StateType.SOCIABILITY));
            State fatigue = Objects.requireNonNull(states.get(State.StateType.FATIGUE));

            criticalStates.forEach(state -> {
                switch (state.getCriticalType()) {
                    case SICK:
                    case OVERWEIGHT:
                        health.decrease(State.HEALTH_TICK*ticks);
                        fatigue.decrease(State.FATIGUE_TICK*2*ticks);
                        break;
                    case HUNGRY:
                    case TIRED:
                        health.decrease(State.HEALTH_TICK*2*ticks);
                        entertainment.decrease(State.ENTERTAINMENT_TICK*ticks);
                        break;
                    case BORED:
                        entertainment.decrease(State.ENTERTAINMENT_TICK*ticks);
                        fatigue.decrease(State.FATIGUE_TICK*2*ticks);
                        break;
                    case LONELY:
                        hygiene.decrease(State.HYGIENE_TICK*ticks);
                        sociability.decrease(State.SOCIABILITY_TICK*2*ticks);
                        break;
                    case DIRTY:
                        health.decrease(State.HEALTH_TICK*ticks);
                        sociability.decrease(State.SOCIABILITY_TICK*2*ticks);
                        break;
                }
            });
        }
    }

    /**
     * Retrieves the entity's states {@link Map}. More precisely, returns a {@link Map}
     * that associates {@link com.l2h1.tamagotchi.model.states.State.StateType} objects as keys with {@link State} objects as values.
     *
     * @return the entity's {@link Map} of key-values of type {@link com.l2h1.tamagotchi.model.states.State.StateType} - {@link State}
     */
    public Map<State.StateType, State> getStates() {
        return states;
    }

    /**
     * Returns whether one of the entity's critical states is of the specified type.
     *
     * @param criticalType the {@link com.l2h1.tamagotchi.model.states.State.CriticalType} of the state to check whether the entity has it or not
     * @return {@code true} if one of the entity's critical states is of the type specified, otherwise {@code false}
     */
    public boolean hasCriticalState(State.CriticalType criticalType) {
        //Log.v("hasCriticalState", "entity is " + criticalType + "?" + criticalStates.stream().anyMatch(state -> state.getCriticalType() == criticalType));
        return criticalStates.stream().anyMatch(state -> state.getCriticalType() == criticalType); //the predicate here eqv. to a 'contains' method with critical type of the states
        //we only need a match with the predicate, not a filter to find any and return it
    }

    /**
     * Register a callback to be invoked when the state values of this entity change.
     * @param onStateValueChangedListener the new {@link OnStateValueChangedListener} for this entity
     */
    public void setOnStateValueChangedListener(OnStateValueChangedListener onStateValueChangedListener) {
        this.onStateValueChangedListener = onStateValueChangedListener;
    }

    /**
     * Register a callback to be invoked when this entity dies.
     * @param onEntityDeadListener the new {@link OnEntityDeadListener} for this entity
     */
    public void setOnEntityDeadListener(OnEntityDeadListener onEntityDeadListener) {
        this.onEntityDeadListener = onEntityDeadListener;
    }

    /**
     * Converts the living entity to a {@link JSONObject} representation.
     *
     * @return a {@link JSONObject} representing the living entity
     * @throws JSONException if there is an error during JSON serialization
     * @see JSONable#toJSONObject()
     * @see Entity
     */
    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonEntity = super.toJSONObject();

        JSONObject jsonStates = new JSONObject();
        for(State state : states.values()) {
            jsonStates.put(state.getStringName(), state.getValue());
        }
        jsonEntity.put("states", jsonStates);

        return jsonEntity;
    }

    /**
     * Populates the living entity's fields from the provided {@link JSONObject}.
     *
     * @param object the {@link JSONObject} containing the data to populate the living entity with
     * @throws JSONException if there is an error during JSON deserialization
     * @see JSONable#fromJSON(JSONObject)
     * @see Entity
     */
    @Override
    public void fromJSON(@NonNull JSONObject object) throws JSONException {
        super.fromJSON(object);

        JSONObject jsonStates = object.getJSONObject("states");

        State health = new State.HealthState();
        health.setValue(jsonStates.getDouble("health"));
        states.put(State.StateType.HEALTH, health);

        State hunger = new State.HungerState();
        hunger.setValue(jsonStates.getDouble("hunger"));
        states.put(State.StateType.HUNGER, hunger);

        State entertainment = new State.EntertainmentState();
        entertainment.setValue(jsonStates.getDouble("entertainment"));
        states.put(State.StateType.ENTERTAINMENT, entertainment);

        State hygiene = new State.HygieneState();
        hygiene.setValue(jsonStates.getDouble("hygiene"));
        states.put(State.StateType.HYGIENE, hygiene);

        State sociability = new State.SociabilityState();
        sociability.setValue(jsonStates.getDouble("sociability"));
        states.put(State.StateType.SOCIABILITY, sociability);

        State fatigue = new State.FatigueState();
        fatigue.setValue(jsonStates.getDouble("fatigue"));
        states.put(State.StateType.FATIGUE, fatigue);
    }

}
