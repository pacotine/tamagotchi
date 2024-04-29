package com.l2h1.tamagotchi.model.listeners;

import com.l2h1.tamagotchi.model.states.State;

/**
 * Interface definition for a callback to be invoked when a {@link com.l2h1.tamagotchi.model.LivingEntity} dies.
 */
public interface OnEntityDeadListener {
    /**
     * Called when a {@link com.l2h1.tamagotchi.model.LivingEntity} dies.
     * @param deathType the {@link com.l2h1.tamagotchi.model.states.State.DeathType} of this entity
     */
    void onEntityDead(State.DeathType deathType);
}
