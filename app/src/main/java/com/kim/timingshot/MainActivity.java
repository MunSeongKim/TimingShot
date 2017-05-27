package com.kim.timingshot;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tsengvn.typekit.TypekitContextWrapper;

public class MainActivity extends Activity {
    protected SoundPool effect;
    private static MediaPlayer bgm;
    protected int effectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize to sound effect on click button
        effect = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        effectId = effect.load(this, R.raw.button, 1);

        //Initialize to sound bgm on start application
        bgm = MediaPlayer.create(this, R.raw.main);
        bgm.setLooping(true);
    }

    @Override
    protected void onStart(){
        super.onStart();
        bgm.start();
        Log.i("LifeCycle", "onStart()");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        bgm.start();
        Log.i("LifeCycle", "onRestart()");
    }

    @Override
    protected void onResume(){
        super.onResume();
        bgm.start();
        Log.i("LifeCycle", "onResume()");
    }

    @Override
    protected void onPause(){
        super.onPause();
        bgm.pause();
        Log.i("LifeCycle", "onPause()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        bgm.pause();
        Log.i("LifeCycle", "onStop()");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        bgm.stop();
        Log.i("LifeCycle", "onDestroy()");
    }
    //Apply to external font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    protected void onClickButton(View v){
        //Setting up to sound effect on click button
        effect.play(effectId, 1, 1, 1, 0, 1);

    }

}
