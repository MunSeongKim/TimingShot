package com.kim.timingshot;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.tsengvn.typekit.TypekitContextWrapper;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

}
