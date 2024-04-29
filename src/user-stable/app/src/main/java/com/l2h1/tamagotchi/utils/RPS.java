package com.l2h1.tamagotchi.utils;

import java.util.Random;

/**
 * Enumeration, generation and comparison of RPS results : ROCK, PAPER or SCISSORS.
 */
public enum RPS {
    ROCK,
    PAPER,
    SCISSORS;

    /**
     * @see RPS
     */
    public enum RESULT {
        WIN,
        LOSE,
        TIE
    }

    private static final Random RANDOM = new Random();

    /**
     * Generates a random RPS choice : ROCK, PAPER or SCISSORS.
     * @return a random {@link RPS} choice
     */
    public static RPS randomChoice(){
        RPS [] choices = values();
        return choices[RANDOM.nextInt(choices.length)];
    }

    /**
     * Gives a {@link String} representing a {@link RESULT}.
     * @param result the {@link RESULT}
     * @return the {@link String} representation of this {@link RESULT}
     */
    public static String getResultText(RESULT result) {
        switch(result) {
            case WIN:
                return "Bravo! You won!";
            case LOSE:
                return "Good luck next time...";
            case TIE:
                return "It's a tie";

            default:
                return "?"; //never reached
        }
    }

    /**
     * Gives the {@link RESULT} (win, lose or tie) of an RPS game between two players.
     * <p>
     * RPS rules :
     * <ul>
     * <li> {@code ROCK} vs {@code PAPER} -> {@code PAPER} wins
     * <li> {@code PAPER} vs {@code SCISSORS} -> {@code SCISSORS} wins
     * <li> {@code SCISSORS} vs {@code ROCK} -> {@code ROCK} wins
     * </ul>
     * @param firstChoice the first player's choice
     * @param secondChoice the second player's choice
     * @return the result of an RPS game between two players
     */
    public static RESULT getResult(RPS firstChoice, RPS secondChoice) {
        switch (firstChoice) {
            case ROCK:
                return secondChoice.equals(SCISSORS) ? RESULT.WIN : (secondChoice.equals(PAPER) ? RESULT.LOSE : RESULT.TIE);
            case PAPER:
                return secondChoice.equals(ROCK) ? RESULT.WIN : (secondChoice.equals(SCISSORS) ? RESULT.LOSE : RESULT.TIE);
            case SCISSORS:
                return secondChoice.equals(PAPER) ? RESULT.WIN : (secondChoice.equals(ROCK) ? RESULT.LOSE : RESULT.TIE);

            default:
                return null; //never reached
        }
    }


}
