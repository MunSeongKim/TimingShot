package com.kim.timingshot;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-05-28.
 */

public class GameBgmService extends Service {
    private static final int BGM_LIST_COUNT = 3;
    public MediaPlayer bgm = null;
    public ArrayList<Integer> bgmList = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Initialize to play for bgm on start service
    @Override
    public void onCreate(){
        super.onCreate();
        for(int i = 0; i < BGM_LIST_COUNT; i++){
            bgmList.add(R.raw.bgm1 + i);
        }

        int randBgm = (int)(Math.random() * (BGM_LIST_COUNT -1));
        bgm = MediaPlayer.create(this, bgmList.get(randBgm));
        bgm.setLooping(true);
    }

    //When ended application, stop bgm service
    //and release MediaPlayer
    @Override
    public void onDestroy(){
        bgm.stop();
        bgm.reset();
        bgm.release();
        super.onDestroy();
    }

    //Play for BGM when started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        bgm.start();
        Log.i(this.getClass().getName(), "GameBgmService onStart()");
        return super.onStartCommand(intent, flags, startId);
    }
}
