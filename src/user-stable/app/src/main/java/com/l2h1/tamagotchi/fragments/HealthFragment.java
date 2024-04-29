package com.l2h1.tamagotchi.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.l2h1.tamagotchi.R;
import com.l2h1.tamagotchi.activities.HouseActivity;
import com.l2h1.tamagotchi.model.LivingEntity;
import com.l2h1.tamagotchi.model.listeners.OnStateValueChangedListener;
import com.l2h1.tamagotchi.model.states.State;

/**
 * Fragment representing the pharmacy, where the user can treat his Tamagotchi.
 */
public class HealthFragment extends Fragment {

    private ProgressBar healthProgressBar;
    private LivingEntity resident;
    private HouseActivity context;
    private ImageView healthImageView;
    private double medicationValue;
    private boolean pillsGiven;

    /**
     * Default empty constructor, required for replacement management.
     */
    public HealthFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health, container, false);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        healthProgressBar = view.findViewById(R.id.health_progress_bar);
        healthImageView = view.findViewById(R.id.image_view_health);
        ImageView pills = view.findViewById(R.id.capyprane);
        ImageView syrup = view.findViewById(R.id.capysyrup);
        ImageView tea = view.findViewById(R.id.herbalTea);
        pillsGiven = false;

        pills.setOnTouchListener((v, event) -> {
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                startDrag(v, 250.0);
                pillsGiven = true;
            }
            return true;
        });

        syrup.setOnTouchListener((v, event) -> {
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                startDrag(v, 100.0);
            }
            return true;
        });

        tea.setOnTouchListener((v, event) -> {
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                startDrag(v, 25.0);
            }
            return true;
        });

        healthImageView.setOnDragListener((l, event) -> {
            if(event.getAction() == DragEvent.ACTION_DROP && medicationValue != 0.0) {
                Toast.makeText(context,"I feel better",Toast.LENGTH_SHORT).show();
                if(pillsGiven){
                    increasePills();
                    pillsGiven = false;
                }
                heal(medicationValue);
            }
            return true;
        });


        this.context = (HouseActivity) requireContext();
    }

    /**
     * Increases the number of pills given by 1 for this session.
     * This number is an attribute of {@link State.HealthState}.
     */
    private void increasePills(){
        State.HealthState healthState = (State.HealthState) resident.getStates().get(State.StateType.HEALTH);
        if(healthState != null) {
            healthState.setCountPills(healthState.getCountPills()+1);
        }
    }

    /**
     * Starts the <i>drag and drop</i> process with the medication value specified to be entered at the time of drop.
     * @param view the {@link View} to build the {@link android.view.View.DragShadowBuilder} and make it movable by drag and drop
     * @param medicationValue the value of medication to be given at the time of the drop
     */
    private void startDrag(View view, double medicationValue) {
        this.medicationValue = medicationValue;
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDragAndDrop(null, shadowBuilder, null, 0);
    }

    /**
     * Initializes animations according to the value the entity {@link com.l2h1.tamagotchi.model.states.State.HealthState}, and listeners.
     */
    private void initAnimation() {
        Glide.with(context).load(resident.hasCriticalState(State.CriticalType.SICK) ?
                R.drawable.sick : R.drawable.healthy).into(healthImageView);

        resident.setOnStateValueChangedListener(new OnStateValueChangedListener() {
            @Override
            public void onStateCritical(State.CriticalType type) {
                if(type == State.CriticalType.SICK)
                    context.runOnUiThread(() -> Glide.with(context).load(R.drawable.sick).into(healthImageView));
            }

            @Override
            public void onStateStable(State.StateType type) {
                if(type == State.StateType.HEALTH)
                    context.runOnUiThread(() -> Glide.with(context).load(R.drawable.healthy).into(healthImageView));
            }
        });
    }

    /**
     * Updates the display of the {@link ProgressBar} according to the value the entity {@link com.l2h1.tamagotchi.model.states.State.HealthState}.
     */
    private void updateProgressBar() {
        State healthState = resident.getStates().get(State.StateType.HEALTH);
        double value = healthState == null ? 0.0 : healthState.getValue();
        healthProgressBar.setProgress((int) value);
    }

    /**
     * Increases the entity's health ({@link com.l2h1.tamagotchi.model.states.State.HealthState})
     * by the value specified to represent the bonus for healing its companion.
     * @param value the healing value to be added to the health state value
     */
    private void heal(double value){
        State healthState = resident.getStates().get(State.StateType.HEALTH);
        if(healthState != null) {
            healthState.increase(value);
            resident.checkStates();
            updateProgressBar();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        context.removeListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.resident = context.getHouse().getResident();
        updateProgressBar();
        initAnimation();
    }

}