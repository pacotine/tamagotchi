package com.l2h1.tamagotchi.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.l2h1.tamagotchi.model.listeners.OnTimePassedListener;
import com.l2h1.tamagotchi.utils.json.JSONable;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The {@link House} class represents both the habitat of a <i>living entity</i>
 * ({@link LivingEntity}, called <i>resident</i> in this context) and its simulation.
 * <p>
 * There are 2 types of simulation: <i>active</i> and <i>passive</i>.
 * <ul>
 * <li> Passive simulation causes an entity to pass through time in one go,
 * from point A in time to point B in time (see {@link House#simulateWithTimestamps(long, long)})
 * <li> Active simulation, on the other hand, passes time to the entity continuously,
 * updating its states every 15s (see {@link House#scheduleSimulation()})
 * </ul>
 * It's important to note how the active simulation works: in fact, active simulation is run in a separate thread
 * from a <i>thread pool</i> given by the {@link ScheduledExecutorService}, called the <i>scheduler</i>.
 * <p>
 * Therefore, 2 points need to be taken into account:
 * <ul>
 * <li> running a <b>UI task</b> that depends on active simulation must be done on the UI thread <b>only</b> (see {@link android.app.Activity#runOnUiThread(Runnable)})
 * <li> stop simulation when it is no longer in use, to avoid memory leaks and unnecessary use of simulation thread resources
 * </ul>
 *
 */
public class House implements JSONable {

    private LivingEntity resident;
    private String name;
    private ScheduledExecutorService scheduler;
    private OnTimePassedListener onTimePassedListener;

    /**
     * Constructs a {@link House} with the specified house name and living entity resident.
     * @param name the name of the house
     * @param resident the {@link LivingEntity} object representing this house resident
     */
    public House(String name, LivingEntity resident) {
        this.resident = resident;
        this.name = name;
        if(scheduler == null) this.scheduler = Executors.newScheduledThreadPool(1); //because a new thread pool must be created once
    }

    /**
     * Constructs a {@link House} with an empty name and a default {@link LivingEntity} resident.
     */
    public House() {
        this.resident = new LivingEntity();
        if(scheduler == null) this.scheduler = Executors.newScheduledThreadPool(1); //because a new thread pool must be created once
    }

    /**
     * Starts the house active simulation.
     * Checks the entity states before schedule the simulation.
     * <p>
     * Calling this method has no effect if the scheduler is {@code null}.
     * @see House#scheduleSimulation()
     */
    public void startSimulation() {
        if(scheduler != null) {
            resident.checkStates(); //check before starting
            if(resident.isAlive()) {
                Log.v("House:startSimulation", "schedule the simulation");
                scheduleSimulation();
            }
            else {
                Log.v("House:startSimulation", "entity already dead, aborted");
            }
        } else {
            Log.e("House:startSimulation", "no scheduler set up");
        }
    }

    /**
     * Schedule a periodic execution of the simulation command.
     * The start delay is 15s. After this delay, the simulation is updated every 15s as follows:
     * <ul>
     * <li> check if the resident is alive; if not, call stopSimulation()
     * <li> calls {@link OnTimePassedListener#onTimePassed(LivingEntity)} if the {@link OnTimePassedListener} is not {@code null}
     * <li> simulate the time spent on the resident by calling {@link LivingEntity#checkStates()}
     * </ul>
     * This method should only be called if it has been verified that the scheduler is not {@code null}.
     */
    private void scheduleSimulation() {
        scheduler.scheduleAtFixedRate(() -> {
            if (resident.isAlive()) {
                resident.passTime();
                if(onTimePassedListener != null) { onTimePassedListener.onTimePassed(resident); }

                resident.devLogs();

                resident.checkStates();
            } else {
                stopSimulation();
            }
        }, 15, 15, TimeUnit.SECONDS);
    }

    /**
     * Shutdowns the scheduler. This actions has no effect if the scheduler is {@code null}.
     * @see ScheduledExecutorService#shutdownNow()
     */
    public void stopSimulation() {
        if(scheduler != null) {
            scheduler.shutdownNow();
            Log.i("House:stopSimulation", "shutdown the scheduler (simulation)");
        }
    }

    /**
     * Register a callback to be invoked when the time passed in the active simulation.
     * @param listener the new {@link OnTimePassedListener} for this house
     */
    public void setOnTimePassedListener(OnTimePassedListener listener) {
        this.onTimePassedListener = listener;
    }

    /**
     * Performs passive simulation for the resident for a given time elapsed (calculated from the two arguments).
     * @param start the timestamp for the start of passive simulation
     * @param end the timestamp for the end of passive simulation
     */
    public void simulateWithTimestamps(long start, long end) {
        long timeElapsedMillis = end - start; //ms
        double secs = timeElapsedMillis/1000.0d;
        Log.v("simulateWithTimestamps", start + "/" + end + "/" + secs);
        if(secs > 15.0) resident.passTime(secs);
    }

    /**
     * Retrieves this house resident.
     * @return this house resident (as {@link LivingEntity})
     */
    public LivingEntity getResident() {
        return resident;
    }

    /**
     * Retrieves the name of the house.
     * @return the name of the house
     */
    public String getName() {
        return name;
    }

    /**
     * Converts the house to a {@link JSONObject} representation.
     *
     * @return a {@link JSONObject} representing the house
     * @throws JSONException if there is an error during JSON serialization
     * @see JSONable#toJSONObject()
     */
    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonHouse = new JSONObject();
        jsonHouse.put("lastUpdate", System.currentTimeMillis());
        jsonHouse.put("name", this.name);
        jsonHouse.put("resident", this.resident.toJSONObject());
        return jsonHouse;
    }

    /**
     * Populates the house's fields from the provided {@link JSONObject}.
     *
     * @param object the {@link JSONObject} containing the data to populate the house with
     * @throws JSONException if there is an error during JSON deserialization
     * @see JSONable#fromJSON(JSONObject)
     */
    @Override
    public void fromJSON(@NonNull JSONObject object) throws JSONException {
        this.name = object.getString("name");
        resident.fromJSON(object.getJSONObject("resident"));
    }
}
