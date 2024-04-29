package com.l2h1.tamagotchi.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
 * Fragment representing the bathroom, where the user can wash his Tamagotchi.
 */
public class HygieneFragment extends Fragment {
    private ProgressBar hygieneProgressBar, washingProgressBar;
    private LivingEntity resident;
    private HouseActivity context;
    private ImageView hygieneImageView;
    private float soapX, soapY, totalX, totalY, lastX, lastY;

    private static final int WASHING_MAX = 1000;

    /**
     * Default empty constructor, required for replacement management.
     */
    public HygieneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hygiene, container, false);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView soap = view.findViewById(R.id.soap);
        ImageButton showerButton = view.findViewById(R.id.showerButton);
        hygieneProgressBar = view.findViewById(R.id.hygiene_progress_bar);
        washingProgressBar = view.findViewById(R.id.washing_progress_bar);
        washingProgressBar.setMax(WASHING_MAX);
        hygieneImageView = view.findViewById(R.id.image_view_hygiene);

        //to make the soap move following the user's finger
        soap.setOnTouchListener(new View.OnTouchListener() {
            boolean shower = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = soap.getX();
                float y = soap.getY();
                saveInitSoapXY(x, y);

                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float moveX = event.getX();
                        float moveY = event.getY();
                        float deltaX = Math.abs(moveX-lastX);
                        float deltaY = Math.abs(moveY-lastY);
                        totalX += deltaX%10;
                        totalY += deltaY%10;
                        lastX = moveX;
                        lastY = moveY;

                        soap.setX(x + moveX - (float)soap.getWidth()/2);
                        soap.setY(y + moveY - (float)soap.getHeight()/2);
                        washingProgressBar.setProgress((int) Math.max(totalX, totalY));

                        //Log.v("xy", deltaX + "/" + deltaY + "--" + totalX + "/" + totalY);
                        if(!shower && (totalX > WASHING_MAX || totalY > WASHING_MAX)) { //user has to move his finger and not just click & wait 3s without moving
                            showerButton.setVisibility(View.VISIBLE);
                            Glide.with(context).load(R.drawable.bubbles).into(hygieneImageView);
                            Toast.makeText(context,"You can now rinse your Tamagotchi !",Toast.LENGTH_SHORT).show();
                            shower = true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        shower = false;
                        soap.setX(soapX);
                        soap.setY(soapY);
                        break;
                }

                return true;
            }
        });

        showerButton.setOnClickListener(e -> {
            clean();
            resetSoapProgress();
            showerButton.setVisibility(View.GONE);
            soap.setX(soapX);
            soap.setY(soapY);
            initAnimation();
        });

        this.context = (HouseActivity) requireContext();
    }

    /**
     * Retrieves the X and Y coordinates of the soap at the moment the user's finger is first placed on the screen.
     * This allows the soap to be repositioned when the user releases it, as the exact position of a {@link View} cannot be known before rendering.
     * @param soapX the X position of the soap
     * @param soapY the Y position of the soap
     */
    private void saveInitSoapXY(float soapX, float soapY) {
        //we save the soap X & Y coordinates when it's drawn and ONLY if the values are not set yet
        //thus the soapX and soapY values are retrieved on the first click, the first time onTouch is called
        if(this.soapX == 0.0 && this.soapY == 0.0) {
            this.soapX = soapX;
            this.soapY = soapY;
        }
    }

    /**
     * Resets soaping progress value to 0.
     */
    private void resetSoapProgress() {
        totalX = 0;
        totalY = 0;
        washingProgressBar.setProgress(0);
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
        resetSoapProgress();
        updateProgressBar();
        initAnimation();
    }

    /**
     * Initializes animations according to the value the entity {@link com.l2h1.tamagotchi.model.states.State.HygieneState}, and listeners.
     */
    private void initAnimation() {
        Glide.with(context).load(resident.hasCriticalState(State.CriticalType.DIRTY) ?
                R.drawable.dirty_bath : R.drawable.clean_bath).into(hygieneImageView);

        resident.setOnStateValueChangedListener(new OnStateValueChangedListener() {
            @Override
            public void onStateCritical(State.CriticalType type) {
                if(type == State.CriticalType.DIRTY)
                    context.runOnUiThread(() -> Glide.with(context).load(R.drawable.dirty_bath).into(hygieneImageView));
            }

            @Override
            public void onStateStable(State.StateType type) {
                if(type == State.StateType.HYGIENE)
                    context.runOnUiThread(() -> Glide.with(context).load(R.drawable.clean_bath).into(hygieneImageView));
            }
        });
    }

    /**
     * Updates the display of the {@link ProgressBar} according to the value the entity {@link com.l2h1.tamagotchi.model.states.State.HygieneState}.
     */
    private void updateProgressBar() {
        State hygieneState = resident.getStates().get(State.StateType.HYGIENE);
        double value = hygieneState == null ? 0.0 : hygieneState.getValue();
        hygieneProgressBar.setProgress((int) value);
    }

    /**
     * Increases the entity's hygiene ({@link com.l2h1.tamagotchi.model.states.State.HygieneState})
     * by {@link State#CLEAN_VALUE} to represent the bonus for cleaning its companion.
     */
    private void clean(){
        State hygieneState = resident.getStates().get(State.StateType.HYGIENE);
        if(hygieneState != null) {
            hygieneState.increase(State.CLEAN_VALUE);
            resident.checkStates();
            updateProgressBar();
        }
    }
}