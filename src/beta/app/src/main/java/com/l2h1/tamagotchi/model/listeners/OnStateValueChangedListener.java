package com.l2h1.tamagotchi.model.listeners;

import com.l2h1.tamagotchi.model.states.State;

/**
 * Interface definition for a callback to be invoked when a state values of a {@link com.l2h1.tamagotchi.model.LivingEntity} change.
 */
public interface OnStateValueChangedListener {
    /**
     * Called when a state value of an entity becomes critical.
     * @param type the {@link com.l2h1.tamagotchi.model.states.State.CriticalType} of the state
     */
    void onStateCritical(State.CriticalType type); //one of the entity's states becomes critical

    /**
     * Called when a state value of an entity becomes stable again.
     * @param type the {@link com.l2h1.tamagotchi.model.states.State.StateType} of the state
     */
    void onStateStable(State.StateType type); //one of the entity's critical states becomes stable again
}

//since several states can have their value vary at the same time with each tick,
//this listener must be able to manage each state "involved", and is therefore not specific
//to each state (State.java class) in order to guarantee entity-specific use and simulation
