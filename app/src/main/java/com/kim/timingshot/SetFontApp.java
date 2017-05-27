package com.kim.timingshot;

import android.app.Application;

import com.tsengvn.typekit.Typekit;

/**
 * Created by Administrator on 2017-05-26.
 */

public class SetFontApp extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        //Initialize Typekit library
        //Typekit: Library that applying external font
        Typekit.getInstance()
                .addItalic(Typekit.createFromAsset(this, "fonts/speed.ttf"))
                .addBold(Typekit.createFromAsset(this, "fonts/BusanStation.ttf"))
                .addNormal(Typekit.createFromAsset(this, "fonts/ENORBITB.TTF"));
    }
}
