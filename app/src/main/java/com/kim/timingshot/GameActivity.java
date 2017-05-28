package com.kim.timingshot;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class GameActivity extends AppCompatActivity {
    private ImageView board = null;
    private Animation rotate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Setting on background image
        board = (ImageView)findViewById(R.id.boardImg);
        board.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.board_low));
        //Load on background image animation
        rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
    }

    @Override
    protected void onStart(){
        super.onStart();
        //Start background image animation on Thread
        Thread boardThread = new Thread(new Runnable() {
            @Override
            public void run() {
                board.startAnimation(rotate);
                Log.i("Thread", "running");
            }
        });
        boardThread.start();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if( isFinishing() ){
            Log.i("Thread", "not running");
        }
    }
}
