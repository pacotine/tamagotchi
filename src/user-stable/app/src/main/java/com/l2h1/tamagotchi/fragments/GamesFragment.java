package com.l2h1.tamagotchi.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.l2h1.tamagotchi.utils.RPS;

import java.util.Random;

/**
 * Fragment representing the games room, where users can play with their Tamagotchi.
 */
public class GamesFragment extends Fragment {
    private TextView resultText, makeChoiceText;
    private HouseActivity context;
    private ProgressBar entertainmentProgressBar;
    private LivingEntity resident;
    private ImageView entertainmentImageView, resultImageView, userChoiceImageView;
    private ImageButton rockButton, paperButton, scissorsButton;
    private int countVictories;

    /**
     * Default empty constructor, required for replacement management.
     */
    public GamesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_games, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rockButton = view.findViewById(R.id.rockButton);
        paperButton = view.findViewById(R.id.paperButton);
        scissorsButton = view.findViewById(R.id.scissorsButton);

        entertainmentProgressBar = view.findViewById(R.id.entertainment_progress_bar);
        resultImageView = view.findViewById(R.id.compChoiceImage);
        resultImageView.setVisibility(View.GONE);
        userChoiceImageView = view.findViewById(R.id.humanChoiceImage);
        userChoiceImageView.setVisibility(View.GONE);

        resultText = view.findViewById(R.id.result);

        makeChoiceText = view.findViewById(R.id.make_choice);

        entertainmentImageView = view.findViewById(R.id.image_view_entertainment);


        //the user has to wait 2 seconds before the next match (buttons become invisible)
        Handler handler = new Handler();
        Runnable runnable = () -> {
            rockButton.setVisibility(View.VISIBLE);
            paperButton.setVisibility(View.VISIBLE);
            scissorsButton.setVisibility(View.VISIBLE);
            makeChoiceText.setVisibility(View.VISIBLE);
            //Toast.makeText(context,"you can play",Toast.LENGTH_SHORT).show();
        };

        rockButton.setOnClickListener(e -> {
            removeButtons();
            chooseAndShow(RPS.ROCK);
            handler.postDelayed(runnable,2000);
        });

        paperButton.setOnClickListener(e -> {
            removeButtons();
            chooseAndShow(RPS.PAPER);
            handler.postDelayed(runnable,2000);
        });
        scissorsButton.setOnClickListener(e -> {
            removeButtons();
            chooseAndShow(RPS.SCISSORS);
            handler.postDelayed(runnable,2000);
        });

        this.context = (HouseActivity) requireContext();
    }

    private void removeButtons() {
        rockButton.setVisibility(View.GONE);
        paperButton.setVisibility(View.GONE);
        scissorsButton.setVisibility(View.GONE);
        makeChoiceText.setVisibility(View.GONE);
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
        countVictories = 0;
    }

    /**
     * Initializes animations according to the value the entity {@link com.l2h1.tamagotchi.model.states.State.EntertainmentState}, and listeners.
     */
    private void initAnimation() {
        Glide.with(context).load(resident.hasCriticalState(State.CriticalType.BORED) ?
                R.drawable.bored : R.drawable.entertained).into(entertainmentImageView);

        resident.setOnStateValueChangedListener(new OnStateValueChangedListener() {
            @Override
            public void onStateCritical(State.CriticalType type) {
                if (type == State.CriticalType.BORED)
                    context.runOnUiThread(() -> Glide.with(context).load(R.drawable.bored).into(entertainmentImageView));
            }

            @Override
            public void onStateStable(State.StateType type) {
                if (type == State.StateType.ENTERTAINMENT)
                    context.runOnUiThread(() -> Glide.with(context).load(R.drawable.entertained).into(entertainmentImageView));
            }
        });
    }

    /**
     * Updates the display of the {@link ProgressBar} according to the value the entity {@link com.l2h1.tamagotchi.model.states.State.EntertainmentState}.
     */
    private void updateProgressBar() {
        State entertainmentState = resident.getStates().get(State.StateType.ENTERTAINMENT);
        double value = entertainmentState == null ? 0.0 : entertainmentState.getValue();
        entertainmentProgressBar.setProgress((int) value);
    }

    /**
     * Updates the display (animation, text and images) according to the {@link RPS} value chosen by the user.
     * @param userChoice the {@link RPS} value chosen by the user
     */
    private void chooseAndShow(RPS userChoice) {
        RPS compChoice = RPS.randomChoice();
        RPS.RESULT result = RPS.getResult(userChoice, compChoice);
        if(result != null) {
            int userImageResID = userChoice.equals(RPS.ROCK) ? R.drawable.humanrock
                    : (userChoice.equals(RPS.SCISSORS) ? R.drawable.humanscissors : R.drawable.humanpaper);
            Glide.with(context).load(userImageResID).into(userChoiceImageView);

            int compImageResID = compChoice.equals(RPS.ROCK) ? R.drawable.capyrock
                    : (compChoice.equals(RPS.SCISSORS) ? R.drawable.capyscissors : R.drawable.capypaper);
            Glide.with(context).load(compImageResID).into(resultImageView);

            State entertainmentState = resident.getStates().get(State.StateType.ENTERTAINMENT);
            if (entertainmentState != null) {
                entertainmentState.increase(new Random().nextDouble() * 10);
                resident.checkStates();
                updateProgressBar();
            }

            resultText.setText(RPS.getResultText(result));
            resultImageView.setVisibility(View.VISIBLE);
            userChoiceImageView.setVisibility(View.VISIBLE);

            if (countVictories == 2 && !result.equals(RPS.RESULT.WIN)) {
                Toast.makeText(context, "You're such a loser !", Toast.LENGTH_SHORT).show();
                Glide.with(context).load(R.drawable.thug_life).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        GifDrawable gifDrawable = ((GifDrawable) resource);
                        gifDrawable.setLoopCount(1);
                        return false;
                    }
                }).into(entertainmentImageView);
            }
            countVictories = result.equals(RPS.RESULT.WIN) ? countVictories + 1 : 0;

            if(countVictories == 3) {
                Glide.with(context).load(R.drawable.cigar).into(entertainmentImageView);
                Toast.makeText(context, "~I'm hiiiiiigH~", Toast.LENGTH_SHORT).show();
            } else if(countVictories == 5) {
                Glide.with(context).load(R.drawable.high).into(entertainmentImageView);
                Toast.makeText(context, "ooooh shit, 5 TIMES ?!?", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
