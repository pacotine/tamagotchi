package com.l2h1.tamagotchi.fragments;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.l2h1.tamagotchi.R;
import com.l2h1.tamagotchi.activities.HouseActivity;
import com.l2h1.tamagotchi.model.LivingEntity;
import com.l2h1.tamagotchi.model.listeners.OnStateValueChangedListener;
import com.l2h1.tamagotchi.model.states.State;


/**
 * Fragment representing the kitchen, where the user can feed his Tamagotchi.
 */
public class FoodFragment extends Fragment {
    private LivingEntity resident;
    private HouseActivity context;
    private ImageView hungerImageView;
    private ImageView choice;
    private ImageView nextLeft;
    private ImageView nextRight;

    private static final int[] FOODS_RES_ID = new int[]{
            R.drawable.fruits,
            R.drawable.burger,
            R.drawable.cupcake,
            R.drawable.pasta,
            R.drawable.soda,
            R.drawable.vegetables,
            R.drawable.water,
            R.drawable.sushi,
            R.drawable.milk,
            R.drawable.chicken,
            R.drawable.cheese,
            R.drawable.bread
    };
    private static final int len = FOODS_RES_ID.length;
    private static final double[] FOODS_VALUES = new double[]{
            12.0,
            40.0,
            20.0,
            30.0,
            15.0,
            12.0,
            5.0,
            23.0,
            10.0,
            25.0,
            13.0,
            18.0
    };

    private int pos;
    private boolean canGive;

    /**
     * Default empty constructor, required for replacement management.
     */
    public FoodFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food, container, false);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hungerImageView = view.findViewById(R.id.image_view_hunger);

        choice = view.findViewById(R.id.choice);
        nextRight = view.findViewById(R.id.nextRight);
        nextLeft = view.findViewById(R.id.nextLeft);

        ImageView arrowRight = view.findViewById(R.id.arrowRight);
        ImageView arrowLeft = view.findViewById(R.id.arrowLeft);


        choice.setImageResource(FOODS_RES_ID[0]);
        nextRight.setImageResource(FOODS_RES_ID[1]);
        nextLeft.setImageResource(FOODS_RES_ID[len-1]);

        arrowRight.setOnClickListener(e -> {
            pos++;
            updateChoice();
        });

        arrowLeft.setOnClickListener(e -> {
            pos--;
            updateChoice();
        });

        choice.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if(canGive) {
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(null, shadowBuilder, null, 0);
                } else {
                    Toast.makeText(context, "WAIT PLEASE !!!", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });

        hungerImageView.setOnDragListener((l, event) -> {
            if(event.getAction() == DragEvent.ACTION_DROP) {
                feed(FOODS_VALUES[Math.floorMod(pos, len)]); //should we place this in the listener ?
                canGive = false;
                Glide.with(context).load(getAnimationResID(resident, true))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                if (resource instanceof GifDrawable) {
                                    GifDrawable gifDrawable = ((GifDrawable)resource);
                                    gifDrawable.setLoopCount(5);
                                    gifDrawable.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                                        @Override
                                        public void onAnimationStart(Drawable drawable) {
                                            super.onAnimationStart(drawable);
                                        }

                                        @Override
                                        public void onAnimationEnd(Drawable drawable) {
                                            super.onAnimationEnd(drawable);
                                            Toast.makeText(context,"Yummy",Toast.LENGTH_SHORT).show();
                                            initAnimation();
                                            canGive = true;
                                        }
                                    });
                                }
                                return false;
                            }
                        }).into(hungerImageView);
            }
            return true;
        });

        this.context = (HouseActivity) requireContext();
    }

    /**
     * Updates the image display showing the selected dish, the next dish on the right
     * and the next dish on the left, based on position in the dish list.
     */
    private void updateChoice() {
        choice.setImageResource(FOODS_RES_ID[Math.floorMod(pos, len)]); //eqv. to (pos % len + len) % len
        nextRight.setImageResource(FOODS_RES_ID[Math.floorMod(pos+1, len)]);
        nextLeft.setImageResource(FOODS_RES_ID[Math.floorMod(pos-1, len)]);
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
        this.pos = 0;
        this.canGive = true;
        initAnimation();
    }

    /**
     * Initializes animations according to the value the entity {@link com.l2h1.tamagotchi.model.states.State.HungerState}, and listeners.
     */
    private void initAnimation() {
        Glide.with(context).load(getAnimationResID(resident, false)).into(hungerImageView);

        resident.setOnStateValueChangedListener(new OnStateValueChangedListener() {
            @Override
            public void onStateCritical(State.CriticalType type) {
                if(type == State.CriticalType.HUNGRY)
                    context.runOnUiThread(() -> Glide.with(context).load(R.drawable.hungry).into(hungerImageView));
                else if (type == State.CriticalType.OVERWEIGHT)
                    context.runOnUiThread(() -> Glide.with(context).load(R.drawable.fat).into(hungerImageView));
            }

            @Override
            public void onStateStable(State.StateType type) {
                if(type == State.StateType.HUNGER)
                    context.runOnUiThread(() -> Glide.with(context).load(R.drawable.normal).into(hungerImageView));
            }
        });
    }

    /**
     * Returns the animation's resource ID according to whether the entity is in
     * a critical or stable state, and whether it is eating or not.
     * @param resident the {@link LivingEntity} whose reports depend on the returned ID
     * @param isEating indicates whether the desired ID is that of an animation of the eating entity
     * @return an animation resource ID
     */
    private static int getAnimationResID(LivingEntity resident, boolean isEating) {
        if(resident.hasCriticalState(State.CriticalType.HUNGRY))
            return isEating ? R.drawable.hungry_eating : R.drawable.hungry;
        if(resident.hasCriticalState(State.CriticalType.OVERWEIGHT))
            return isEating ? R.drawable.fat_eating : R.drawable.fat;
        return isEating ? R.drawable.eating : R.drawable.normal;
    }

    /**
     * Increases the entity's satiety ({@link com.l2h1.tamagotchi.model.states.State.HungerState})
     * by the value specified to represent the bonus for feeding its companion.
     * @param value the feeding value to be added to the hunger state value
     */
    private void feed(double value) {
        State hungerState = resident.getStates().get(State.StateType.HUNGER);
        if (hungerState != null) {
            hungerState.increase(value);
            resident.checkStates();
        }

    }
}







