package com.l2h1.tamagotchi.model.states;

import androidx.annotation.NonNull;

/**
 * A <i>state</i> is a characteristic of a {@link com.l2h1.tamagotchi.model.LivingEntity}.
 * This class abstractly represents what is more concretely a progress bar.
 * All states have a name, a type and a value.
 * But each state that inherits from this {@link State} class can redefine the way it loses value
 * with each tick (see {@link com.l2h1.tamagotchi.model.LivingEntity}),
 * the way it is in a critical state or the way it is in an over state. Each state also has its own
 * {@link CriticalType}(s), whose logic depends on its own attributes; but also its own {@link DeathType}(s).
 * Each state must redefine its own simulation logic.
 * <p>
 * This class, which is an important part of the backend, is designed to be scalable according to the wishes of future developers.
 */
public abstract class State {

    public static final double STATE_GENERAL_MAX_VALUE = 500.0;
    public static final double STATE_GENERAL_MIN_VALUE = 0.0;
    public static final double HEALTH_TICK = 1.0/24.0;
    public static final double HUNGER_TICK = 1.0/12.0;
    public static final double FATIGUE_TICK = 1.0/30.0;
    public static final double ENTERTAINMENT_TICK = 1.0/48.0;
    public static final double SOCIABILITY_TICK = 1.0/120.0;
    public static final double HYGIENE_TICK = 1.0/30.0;

    public static final double PET_VALUE = 10.0;
    public static final double CLEAN_VALUE = 250.0;

    /**
     * Types of state of a {@link com.l2h1.tamagotchi.model.LivingEntity}
     */
    public enum StateType {
        HEALTH,
        HUNGER,
        ENTERTAINMENT,
        HYGIENE,
        SOCIABILITY,
        FATIGUE
    }

    /**
     * Critical types for states of a {@link com.l2h1.tamagotchi.model.LivingEntity}
     */
    public enum CriticalType {
        SICK,
        HUNGRY,
        OVERWEIGHT,
        BORED,
        LONELY,
        TIRED,
        DIRTY
    }

    /**
     * Types of death for states of a {@link com.l2h1.tamagotchi.model.LivingEntity}
     */
    public enum DeathType {
        STARVATION("Starvation"),
        DISEASE("Disease"),
        OBESITY("Obesity"),
        DEPRESSION("Depression"),
        SOLITUDE("Solitude"),
        POOR_HYGIENE("Poor hygiene"),
        OVERDOSE("Overdose"),
        FATAL_LACK_OF_SLEEP("Fatal lack of sleep");


        private final String str;
        DeathType(String str) {
            this.str = str;
        }

        @NonNull
        @Override
        public String toString() {
            return this.str;
        }
        }

    protected final StateType type;
    protected final String stringName;
    protected double value;

    /**
     * Constructs a {@link State} of the specified name and type
     * @param stringName
     * @param type
     */
    public State(String stringName, StateType type) {
        this.type = type;
        this.stringName = stringName;
    }

    /**
     * Retrieves the name of this state.
     * @return the name of this state
     */
    public String getStringName() { return this.stringName; }

    /**
     * Retrieves the {@link StateType} of this state.
     * @return the {@link StateType} of this state
     */
    public StateType getType() { return this.type;}

    /**
     * Retrieves the value of this state.
     * @return the value of this state
     */
    public double getValue() { return this.value; }

    /**
     * Sets the value of this state
     * @param value the new value of this state
     */
    public void setValue(double value) { this.value = value; }

    /**
     * Increases the state value by the specified value.
     * <p>
     * Calling this method has no effect if the new state value exceeds the {@link State#STATE_GENERAL_MAX_VALUE}.
     * @param value the value to be added to the state value
     */
    public void increase(double value) { this.value = Math.min(this.value + value, STATE_GENERAL_MAX_VALUE); }

    /**
     * Decreases the state value by the specified value.
     * <p>
     * Calling this method has no effect if the new state value is below the {@link State#STATE_GENERAL_MIN_VALUE}.
     * @param value the value to be subtracted from the state value
     */
    public void decrease(double value) { this.value = Math.max(this.value - value, STATE_GENERAL_MIN_VALUE); }

    /**
     * Represents how the state loses value with each tick.
     * The state that implements this method can decide to multiply the specified number of ticks by a constant,
     * or to customize the way it loses value with each tick.
     * @param ticks the number of ticks to make the loss of value depend on the state
     */
    public abstract void decreaseTick(int ticks);

    /**
     * Represents how a state is critical according to its value.
     * @return {@code true} if this state is critical, {@code false} otherwise
     */
    public abstract boolean isValueCritical();

    /**
     * Represents how a state is in a state above the limits (i.e. if it no longer has any reason to be updated)
     * as a function (or not) of its value.
     * @return {@code true} if this state is over, {@code false} otherwise
     */
    public abstract boolean isOver();

    /**
     * Represents the {@link CriticalType} of state to be returned depending (or not) on its value.
     * <p>
     * However, a state <b>can only have one critical type at a time</b>.
     * @return the {@link CriticalType} depending (or not) the state value
     */
    public abstract CriticalType getCriticalType();

    /**
     * Represents the {@link DeathType} of state to be returned depending (or not) on its value.
     * <p>
     * However, a state <b>can only have one death type at a time</b>.
     * @return the {@link DeathType} depending (or not) the state value
     */
    public abstract DeathType getDeathType();


    /**
     * State representing the <i>health</i> of the entity. This class has an attribute that counts the number of
     * pills the entity has taken in the session. One of the entity's deaths depends
     * on this number ({@link DeathType#OVERDOSE}).
     */
    public static class HealthState extends State {
        private static final int MAX_PILLS = 2;
        private int countPills;

        /**
         * Constructs a {@link HealthState} with a default value of 500.
         */
        public HealthState() {
            super("health", StateType.HEALTH);
            this.value = 500.0;
        }

        @Override
        public void decreaseTick(int ticks) {
            value -= HEALTH_TICK*ticks;
        }

        @Override
        public boolean isValueCritical() {
            return value <= 200;
        }

        @Override
        public boolean isOver() {
            return value <= 0 || countPills >= MAX_PILLS;
        }

        @Override
        public CriticalType getCriticalType() {
            return CriticalType.SICK;
        }

        @Override
        public DeathType getDeathType() {return countPills < MAX_PILLS ? DeathType.DISEASE : DeathType.OVERDOSE ;}

        /**
         * Sets the number of pills taken.
         * @param countPills the new number of pills taken
         */
        public void setCountPills(int countPills) {
            this.countPills = countPills;
        }

        /**
         * Retrieves the number of pills taken.
         * @return the number of pills taken
         */
        public int getCountPills(){
            return countPills;
        }
    }

    /**
     * State representing the <i>hunger</i> of the entity.
     */
    public static class HungerState extends State {

        /**
         * Constructs a {@link HungerState} with a default value of 350.
         */
        public HungerState() {
            super("hunger", StateType.HUNGER);
            this.value = 350.0;
        }

        @Override
        public void decreaseTick(int ticks) {
            value -= HUNGER_TICK*ticks;
        }

        @Override
        public boolean isValueCritical() {
            return value <= 200 || value >= 400;
        }

        //1.0/100.0 are for display approximation : values are displayed to the nearest hundredth,
        //but it may happen that the value is less than 1.0/100.0 and then the display gives 0.0 without
        //the entity being considered dead
        //(example : 23.0/264.0 - 1.0/12.0 gives 0.0037878...,
        //display is 0.0 but isOver() returns false if we test 'value <= 0')
        @Override
        public boolean isOver() {
            return value < 1.0/100.0 || value >= 500;
        }

        @Override
        public CriticalType getCriticalType() {
            return value <= 200 ? CriticalType.HUNGRY : CriticalType.OVERWEIGHT;
        }

        @Override
        public DeathType getDeathType() {
            return value <= 200 ? DeathType.STARVATION : DeathType.OBESITY;
        }
    }

    /**
     * State representing the <i>entertainment</i> of the entity.
     */
    public static class EntertainmentState extends State {

        /**
         * Constructs a {@link EntertainmentState} with a default value of 500.
         */
        public EntertainmentState() {
            super("entertainment", StateType.ENTERTAINMENT);
            this.value = 500.0;
        }

        @Override
        public void decreaseTick(int ticks) {
            value -= ENTERTAINMENT_TICK*ticks;
        }

        @Override
        public boolean isValueCritical() {
            return value <= 200;
        }

        @Override
        public boolean isOver() {
            return value < 1.0/100.0;
        }

        @Override
        public CriticalType getCriticalType() {
            return CriticalType.BORED;
        }

        @Override
        public DeathType getDeathType() {
            return DeathType.DEPRESSION;
        }
    }

    /**
     * State representing the <i>hygiene</i> of the entity.
     */
    public static class HygieneState extends State {

        /**
         * Constructs a {@link HygieneState} with a default value of 500.
         */
        public HygieneState() {
            super("hygiene", StateType.HYGIENE);
            this.value = 500.0;
        }

        @Override
        public void decreaseTick(int ticks) {
            value -= HYGIENE_TICK*ticks;
        }

        @Override
        public boolean isValueCritical() {
            return value <= 200;
        }

        @Override
        public boolean isOver() {
            return value < 1.0/100.0;
        }

        @Override
        public CriticalType getCriticalType() {
            return CriticalType.DIRTY;
        }

        @Override
        public DeathType getDeathType() {
            return DeathType.POOR_HYGIENE;
        }
    }

    /**
     * State representing the <i>sociability</i> of the entity.
     */
    public static class SociabilityState extends State {

        /**
         * Constructs a {@link SociabilityState} with a default value of 500.
         */
        public SociabilityState() {
            super("sociability", StateType.SOCIABILITY);
            this.value = 500.0;
        }

        @Override
        public void decreaseTick(int ticks) {
            value -= SOCIABILITY_TICK*ticks;
        }

        @Override
        public boolean isValueCritical() {
            return value <= 200;
        }

        @Override
        public boolean isOver() {
            return value < 1.0/100.0;
        }

        @Override
        public CriticalType getCriticalType() {
            return CriticalType.LONELY;
        }

        @Override
        public DeathType getDeathType() {
            return DeathType.SOLITUDE;
        }
    }

    /**
     * State representing the <i>fatigue</i> of the entity.
     * This class has an attribute representing the user's screen time over the last 24 hours.
     * This screen time determines the loss of state value.
     */
    public static class FatigueState extends State {
        private double screenTime;

        /**
         * Constructs a {@link FatigueState} with a default value of 500.
         */
        public FatigueState() {
            super("fatigue", StateType.FATIGUE);
            this.value = 500.0;
            this.screenTime = 0.0; //will still be 0.0 if permissions are not granted for USAGE_STATS
        }

        public void setScreenTime(double screenTime) {
            this.screenTime = screenTime;
        }

        @Override
        public void decreaseTick(int ticks) {
            value -= FATIGUE_TICK*ticks + (screenTime/100.0)*ticks;
        }

        @Override
        public boolean isValueCritical() {
            return value <= 200;
        }

        @Override
        public boolean isOver() {
            return value < 1.0/100.0;
        }

        @Override
        public CriticalType getCriticalType() {
            return CriticalType.TIRED;
        }

        @Override
        public DeathType getDeathType() {
            return DeathType.FATAL_LACK_OF_SLEEP;
        }
    }

}
