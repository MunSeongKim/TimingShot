package com.kim.timingshot;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MainBgmService extends Service {
    public static MediaPlayer bgm = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Initialize to play for bgm on start service
    @Override
    public void onCreate(){
        super.onCreate();
        bgm = MediaPlayer.create(this, R.raw.main);
        bgm.setLooping(true);
    }
    //When ended application, stop bgm service
    //and release MediaPlayer
    @Override
    public void onDestroy(){
        bgm.stop();
        bgm.release();
        super.onDestroy();
    }

    //Play for BGM when started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        bgm.start();
        return super.onStartCommand(intent, flags, startId);
    }
}
