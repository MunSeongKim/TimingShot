package com.kim.timingshot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tsengvn.typekit.TypekitContextWrapper;

import org.w3c.dom.Text;

public class ResultActivity extends AppCompatActivity {
    private DBHelper gameDb;
    private TextView resScore;
    private TextView resTime;
    private EditText resName;

    protected SoundPool effect = null;
    protected int effectId = 0;

    private Vibrator vibe = null;

    private SharedPreferences settings = null;
    private Boolean vibeSetting = true;
    private Boolean effectSetting = true;
    private Boolean bgmSetting = true;

    private String name, score, time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        findViewById(R.id.resultBackground).setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.result_background));

        effect = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        effectId = effect.load(this, R.raw.button, 1);

        //Initialize vibrator setting
        vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        //Initialize ShearedPreferences Object
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        gameDb = new DBHelper(this);
        Intent intent = getIntent();
        resScore = (TextView)findViewById(R.id.resScore);
        resTime = (TextView)findViewById(R.id.resTime);
        resName = (EditText)findViewById(R.id.inputName);

        score = intent.getStringExtra("score");
        time = intent.getStringExtra("time");
        if(score == null) score="0";
        if(time == null) time="0s";
        else time = time.replace("s", "");

        resScore.setText(score);
        resTime.setText(time + "s");

        Log.i(this.getClass().getName(), "Result Activity onCreate()");
    }

    @Override
    protected void onStart(){
        super.onStart();
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
        Log.i(this.getClass().getName(), "ResultActivity onStart()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        findViewById(R.id.resultBackground).setBackground(null);
    }


    public void onClickButton(View v){
        if( effectSetting ){
            //Setting up to sound effect on button click
            effect.play(effectId, 1, 1, 1, 0, 1);
        }
        if( vibeSetting ){
            //Setting up to vibe effect on button click
            vibe.vibrate(150);
        }

        switch( v.getId() ){
            case R.id.btResSave:
                name = resName.getText().toString();
                Log.i(this.getClass().getName(), "name1: " + name);
                name = name.trim();
                Log.i(this.getClass().getName(), "name2: " + name);
                if( name.getBytes().length <= 0 ) {
                    Toast.makeText(this, "No inputted name", Toast.LENGTH_SHORT).show();
                } else {
                    if(gameDb.insertResult(name, score, time)){
                        Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                break;
            case R.id.btGoHome:
                finish();
                break;
        }
    }

    //Apply external font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
