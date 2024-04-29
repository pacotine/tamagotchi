package com.l2h1.tamagotchi.model.listeners;

import com.l2h1.tamagotchi.model.LivingEntity;

/**
 * Interface definition for a callback to be invoked when time passes in an active simulation.
 */
public interface OnTimePassedListener {
    /**
     * Called when the time passes on a {@link LivingEntity} in an active simulation.
     * @param entity the {@link LivingEntity} at which the passage of time is simulated
     */
    void onTimePassed(LivingEntity entity);
}
