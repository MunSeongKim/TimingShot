package com.kim.timingshot;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.tsengvn.typekit.TypekitContextWrapper;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Log.i(this.getClass().getName(), "Result Activity onCreate()");
        Intent intent = getIntent();

        String score = intent.getStringExtra("score");
        String time = intent.getStringExtra("time");
        if(score.equals("")) score="0";
        if(time.equals("")) time="0s";

        ((TextView)findViewById(R.id.resScore)).setText(score);
        ((TextView)findViewById(R.id.resTime)).setText(time);
    }
    
    //Apply external font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
