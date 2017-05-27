package com.kim.timingshot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tsengvn.typekit.TypekitContextWrapper;

public class MainActivity extends AppCompatActivity {
    protected SoundPool effect = null;
    protected int effectId = 0;
    private Vibrator vibe = null;
    private static final String PREFS_NAME = "gamePrefs";
    private Boolean vibeSetting = true;
    private SharedPreferences settings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize to sound effect on click button
        effect = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        effectId = effect.load(this, R.raw.button, 1);

        //Initialize vibrator setting
        vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        //Initialize ShearedPreferences Object

        settings = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        //When started application, start play for bgm service
        startService(new Intent(this, BgmService.class));
        Log.i("LifeCycle", "onStart()");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.i("LifeCycle", "onRestart()");
    }

    @Override
    protected void onResume(){
        super.onResume();
        vibeSetting = settings.getBoolean("vibrate", true);
        Log.i("LifeCycle", "onResume()");
    }

    @Override
    protected void onPause(){
        Log.i("LifeCycle", "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop(){
        Log.i("LifeCycle", "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        Log.i("LifeCycle", "onDestroy()");
        //When ended application, stop play for bgm service
        stopService(new Intent(this, BgmService.class));
        super.onDestroy();
    }

    //Apply to external font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    protected void onClickButton(View v){
        //Setting up to sound effect on click button
        effect.play(effectId, 1, 1, 1, 0, 1);

        if( vibeSetting ){
            vibe.vibrate(150);
        }

        switch( v.getId() ){
            case R.id.btRanking:
                break;
            case R.id.btStart:
                //if GameStart button click, stop to music service
                stopService(new Intent(this, BgmService.class));
                break;
            case R.id.btSetting:
                //if this button click, Start settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
    }

}
