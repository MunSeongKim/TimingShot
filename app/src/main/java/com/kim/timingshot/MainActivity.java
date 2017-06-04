package com.kim.timingshot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.tsengvn.typekit.TypekitContextWrapper;

public class MainActivity extends AppCompatActivity {
    protected SoundPool effect = null;
    protected int effectId = 0;

    private Vibrator vibe = null;

    private SharedPreferences settings = null;
    private Boolean vibeSetting = true;
    private Boolean effectSetting = true;
    private Boolean bgmSetting = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.mainBackground).setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.main_background));
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
        Log.i(this.getClass().getName(), "MainActivity onStart()");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.i(this.getClass().getName(), "MainActivity onRestart()");
    }

    @Override
    protected void onResume(){
        super.onResume();
        findViewById(R.id.mainBackground).setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.main_background));
        //Get setting data from SharedPreferences
        vibeSetting = settings.getBoolean("vibrate", true);
        effectSetting = settings.getBoolean("effect", true);
        bgmSetting = settings.getBoolean("bgm", true);

        //When started and focused application, start play for bgm service
        //Based on bgm preference value
        if(bgmSetting){
            startService(new Intent(this, MainBgmService.class));
        } else {
            stopService(new Intent(this, MainBgmService.class));
        }
        Log.i(this.getClass().getName(), "MainActivity onResume()");
    }

    @Override
    protected void onPause(){
        super.onPause();
        /*
        //Check the state to lock
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        //if mobile is locked, stop play
        if(km.inKeyguardRestrictedInputMode()){
            stopService(new Intent(this, MainBgmService.class));
        }*/
        stopService(new Intent(this, MainBgmService.class));
        Log.i(this.getClass().getName(), "MainActivity onPause()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        findViewById(R.id.mainBackground).setBackground(null);
        Log.i(this.getClass().getName(), "MainActivity onStop()");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i(this.getClass().getName(), "MainActivity onDestroy()");
        //When this application is completely finished, stop play for bgm service
        if( isFinishing() ) {
            stopService(new Intent(this, MainBgmService.class));
        } else {
            Log.i(this.getClass().getName(), "MainActivity Not finished");
        }

    }

    //Apply external font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    protected void onClickButton(View v){
        if( effectSetting ){
            //Setting up to sound effect on button click
            effect.play(effectId, 1, 1, 1, 0, 1);
        }
        if( vibeSetting ){
            //Setting up to vibe effect on button click
            vibe.vibrate(150);
        }

        switch( v.getId() ){
            case R.id.btRanking:
                startActivity(new Intent(this, RankActivity.class));
                break;
            case R.id.btStart:
                //if GameStart button click, stop to music service
                stopService(new Intent(this, MainBgmService.class));
                //and then start to game activity
                startActivity(new Intent(this, GameActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
                break;
            case R.id.btSetting:
                //if this button click, Start settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
    }
}
