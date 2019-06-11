package com.example.vidri.vidrinegabriel_homework3;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = "PigGame Activity";

    private int playerScore = 0;                        //Player's current score
    private int computerScore = 0;                      //Computer's current score
    private int trough = 0;                             //Trough (current run of roll totals)
    private int currentRoll;                            //Amount of current roll
    private int gamesWon = 0;                           //Number of games won by player
    private int numberOfRolls;                          //Number of rolls made
    private int targetValue;                            //Target value to win
    private Handler handler;                            //Used to delay computer rolls
    private Animation diceRoll;                         //Animation for dice roll
    private String imgName;
    Drawable dice;


    private GridLayout gameGridLayout;                  //GridLayout containing game
    private ImageView dieImageView;                     //ImageView of dice file
    private TextView playerScoreValue;                  //Displays player's current score
    private TextView computerScoreValue;                //Displays computer's current score
    private TextView troughTextValue;                   //Displays trough value
    private TextView resultTextView;                    //Displays result of rolls
    private Button rollAgain;                           //Button to roll again
    private Button hold;                                //Button to hold (and go to computer turn)


    //configures MainActivityFragment when view is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_main, container, false);


        handler = new Handler();

        //Load shake animation for dice roll
        diceRoll = AnimationUtils.loadAnimation(getActivity(),R.anim.dice_roll);
        diceRoll.setRepeatCount(3);

        //Get references to GUI components
        gameGridLayout = (GridLayout) view.findViewById(R.id.gameGridLayout);
        playerScoreValue = (TextView) view.findViewById(R.id.playerScoreValue);
        computerScoreValue = (TextView) view.findViewById(R.id.computerScoreValue);
        dieImageView = (ImageView) view.findViewById(R.id.dieImageView);
        troughTextValue = (TextView) view.findViewById(R.id.troughValue);
        rollAgain = (Button)view.findViewById(R.id.rollAgainButton);
        hold = (Button)view.findViewById(R.id.holdButton);
        resultTextView = (TextView) view.findViewById(R.id.resultTextView);


        resultTextView.setText(getString(R.string.welcome_message));

        //Make first roll
        //rollAgain();



        //Configure listeners for buttons
        rollAgain.setOnClickListener(rollAgainButtonListener);
        hold.setOnClickListener(holdButtonListener);



        return view;
    }

    //Updates the Target Score when user changes preferences
    public void updateTargetScore(SharedPreferences sharedPreferences) {
        String targetVal = sharedPreferences.getString(MainActivity.TARGET, "100");

        targetValue = Integer.parseInt(targetVal);
    }

    //Resets game and starts next game
    public void resetGame() {

        //Resets scores for a new game
        playerScore = 0;
        playerScoreValue.setText(Integer.toString(playerScore));

        computerScore = 0;
        computerScoreValue.setText(Integer.toString(computerScore));

        trough = 0;
        troughTextValue.setText(Integer.toString(trough));

        numberOfRolls = 1;



        //Start the next game by rolling the first die
        //rollAgain();
    }

    //Starts game with first roll and then whenever player chooses to roll the dice again
    private void rollAgain() {


        //Randomly generates roll number, animates file of corresponding die face
        currentRoll = (int) (Math.random() * 6) + 1;

        //updates number of rolls
        numberOfRolls++;

        //Get dice pictures and display correct one according to roll, and shake
                AssetManager am = getActivity().getAssets();

                try (InputStream stream = am.open(currentRoll + ".png")) {

                    dice = Drawable.createFromStream(stream,currentRoll + ".png");
                    dieImageView.setImageDrawable(dice);
                    dieImageView.startAnimation(diceRoll);

                }

            catch (Exception exception) {
                Log.e(TAG, "Error loading" + imgName, exception);
            }


            //If the die roll is 1, resets trough and starts the computer's turn
            if (currentRoll == 1) {
                trough = 0;
                troughTextValue.setText(Integer.toString(trough));
                resultTextView.setText(R.string.player_loses_trough);

                //Delay start of computer turn
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                computerTurn();
                            }
                        }, 2000);

            }

            //If the die roll is anything but 1, adds to trough and updates view
            else {
                trough = trough + currentRoll;
                troughTextValue.setText(Integer.toString(trough));
            }
        }


    //Listener for when player clicks Roll Again button
    private OnClickListener rollAgainButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            rollAgain();
        }
    };

    //Listener for when player clicks Hold button and control passes to computer
    private OnClickListener holdButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //Player Score updated
            playerScore = playerScore + trough;
            playerScoreValue.setText(Integer.toString(playerScore));

            //Reset trough for computer
            trough = 0;
            troughTextValue.setText(Integer.toString(trough));

            //Check to see if player has hit target score
            if (playerScore >= targetValue)
            {
                gamesWon++;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                //builder.setMessage(getString(R.string.player_wins));

                builder.setMessage(getText(R.string.player_wins) + "  Score: " + playerScore + "   Number of Wins: " + gamesWon);

                builder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetGame();
                    }
                });

                builder.create();

                builder.show();
            }

            //If player holds but score is not enough to win
            else {
                resultTextView.setText(R.string.player_holds);

                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                computerTurn();
                            }
                        }, 2000);

            }

        }
    };

    //Computer's turn
    private void computerTurn() {

        //Reset trough
        trough = 0;
        troughTextValue.setText(Integer.toString(trough));


        //Computer will only roll 3 times before "holding" or rolling a 1
        for (int i = 0; i < 3; i++) {

            currentRoll = (int) (Math.random() * 6) + 1;

            AssetManager am = getActivity().getAssets();

            try (InputStream stream = am.open(currentRoll + ".png")) {

                dice = Drawable.createFromStream(stream,currentRoll + ".png");

                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                dieImageView.setImageDrawable(dice);
                                dieImageView.startAnimation(diceRoll);
                            }
                        }, 2000);


            }

            catch (Exception exception) {
                Log.e(TAG, "Error loading" + imgName, exception);
            }

            //If computer rolls a 1, control returns to player
            if (currentRoll == 1) {
                trough = 0;
                troughTextValue.setText(Integer.toString(trough));

                resultTextView.setText(R.string.computer_loses_trough);

                break;

            }

            //Computer will roll until 3 rolls are up or reaches target
            else {

                trough = trough + currentRoll;
                troughTextValue.setText(Integer.toString(trough));

            }
        }

        //Update computer score
        computerScore = computerScore + trough;
        computerScoreValue.setText(Integer.toString(computerScore));


            //If computer reaches target, computer wins, game restarts
            if (computerScore >= targetValue) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage(getText(R.string.player_loses) + "  Score: " + playerScore + "   Number of Wins: " + gamesWon);


                builder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetGame();
                    }
                });

                builder.create();

                builder.show();
            }

        //Reset trough and player rolls again
        trough = 0;
        troughTextValue.setText(Integer.toString(trough));

        //Display computer holds if it completes all 3 rolls without rolling 1
        if (currentRoll != 1) {
            resultTextView.setText(R.string.computer_holds);
        }
    }
}
