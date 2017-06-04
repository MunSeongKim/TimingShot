package com.kim.timingshot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.kim.timingshot.GameView.GameThread;

public class GameActivity extends AppCompatActivity {
    /** A handle to the thread that's actually running the animation. */
    private GameThread mGameThread;
    /** A handle to the View in which the game is running. */
    private GameView mGameView;

    // Private member fields
    private ImageView board = null;
    private Animation rotate = null;
    private Boolean bgmSetting = true;
    private SharedPreferences settings = null;

    /**
     * Invoked when the Activity is created.
     * @param savedInstanceState a Bundle containing state saved from a previous
     *        execution, or null if this is a new execution
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        bgmSetting = settings.getBoolean("bgm", true);

        //Setting on background image
        board = (ImageView)findViewById(R.id.boardImg);
        board.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.game_background));
        //Load on background image animation
        rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);

        // get handle to the GameView from XML, and its GameThread
        mGameView = (GameView)findViewById(R.id.game);
        mGameThread = mGameView.getThread();
        // give the LunarView a handle to the TextView used for messages
        mGameView.setTextView((TextView)findViewById(R.id.score), (TextView)findViewById(R.id.clock));
    /*
        if (savedInstanceState == null) {
            // we were just lanched: set up a new game
           // mGameThread.setState(GameThread.STATE_READY);
            Log.i(this.getClass().getName(), "STS is null");
        } else {
            // we are being resotred: resume a previous game
            //mGameThread.restoreState(savedInstanceState);
            Log.i(this.getClass().getName(), "STS is nonnull");
        }
     */
        Log.i(this.getClass().getName(), "GameActivity onCreate()");
    }

    @Override
    protected void onStart(){
        super.onStart();
        //Start background image animation on Thread
        Thread boardThread = new Thread(new Runnable() {
            @Override
            public void run() {
                board.startAnimation(rotate);
            }
        });
        boardThread.start();

        if(bgmSetting){
            startService(new Intent(this, GameBgmService.class));
        } else {
            stopService(new Intent(this, GameBgmService.class));
        }
        Log.i(this.getClass().getName(), "GameActivity onStart()");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(this.getClass().getName(), "GameActivity onResume()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.i(this.getClass().getName(), "GameActivity onStop()");
        board.setImageBitmap(null);
        this.finish();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if( isFinishing() ) {
            stopService(new Intent(this, GameBgmService.class));
            Log.i(this.getClass().getName(), "GameActivity onDestroy()");
        }
    }
}
