package com.kim.timingshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2017-05-28.
 */

//This class is expression for StarSplite
class Star {
    int x, y, xInc = 1, yInc = 1;
    static int WIDTH = 1920, HEIGHT = 1080;
    private Bitmap starImg;

    //Create to object and set up
    public Star(Bitmap starImg) {
        x = (int)(Math.random() * (WIDTH - 9) + 3);
        y = (int)(Math.random() * (HEIGHT - 9) + 3);

        xInc = (int)(Math.random() * 5 + 1);
        yInc = (int)(Math.random() * 5 + 1);

        this.starImg = starImg;
    }

    //Draw image over move on SurfaceView
    public void paint(Canvas c){
       // Paint paint = new Paint();

        if( x < 0 || x > (WIDTH - 9) ) xInc = -xInc;
        if( y < 0 || y > (HEIGHT - 9) ) yInc = -yInc;

        x += xInc;
        y += yInc;

        c.drawBitmap(starImg, x, y, null);
    }
}

//Make custom view by inheritance SurfaceView
//Draw star image by this view
public class GameBoardView extends SurfaceView implements SurfaceHolder.Callback{
    protected Star starList[] = new Star[10];
    private StarSpliteThread starThread = null;
    private Bitmap starImg = null;

    public GameBoardView(Context context) {
        super(context);

        //Get Surface holder
        SurfaceHolder holder = getHolder();
        //Set this view on top in layout
        this.setZOrderOnTop(true);
        //Add callback function of this view to holder
        holder.addCallback(this);
        //Set transparent for background of this view
        holder.setFormat(PixelFormat.TRANSLUCENT);

        //Convert to Bitmap image from Drawable image
        starImg = BitmapFactory.decodeResource(getResources(), R.drawable.star);
        for (int i = 0; i < 10; i++){
            starList[i] = new Star(starImg);
        }

        //Creates Thread object
        starThread = new StarSpliteThread(holder);
    }

    public GameBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder holder = getHolder();
        this.setZOrderOnTop(true);
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSLUCENT);

        starImg = BitmapFactory.decodeResource(getResources(), R.drawable.star);
        for (int i = 0; i < 10; i++){
            starList[i] = new Star(starImg);
        }

        starThread = new StarSpliteThread(holder);
    }

    public GameBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        SurfaceHolder holder = getHolder();
        this.setZOrderOnTop(true);
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSLUCENT);

        starImg = BitmapFactory.decodeResource(getResources(), R.drawable.star);
        for (int i = 0; i < 10; i++){
            starList[i] = new Star(starImg);
        }

        starThread = new StarSpliteThread(holder);
    }
/*
    public StarSpliteThread getThread(void) {
        return starThread;
    }
*/
    //If SurfaceView is created, calling this method
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //If SurfaceView is available, starting thread
        starThread.setRunning(true);
        starThread.start();
        Log.i("SurfaceView", "Created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    //If SurfaceView is destroyed, calling this method
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        //If SurfaceView is not available, stop thread
        //And merge this thread to main thread
        starThread.setRunning(false);
        while(retry) {
            try {
                starThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
        Log.i("SurfaceView", "Destroyed");

    }

    //Actually draw image by this thread
    public class StarSpliteThread extends Thread {
        private boolean mRun = false;
        private SurfaceHolder mSurfaceHolder;

        //Create thread get SurfaceHolder
        public StarSpliteThread(SurfaceHolder surfaceHolder){
            this.mSurfaceHolder = surfaceHolder;
        }

        //This part is actual work in this thread
        @Override
        public void run(){
            while( mRun ){
                Canvas c = null;
                try {
                    //Get canvas from SurfaceHolder
                    c = mSurfaceHolder.lockCanvas(null);
                    if(c != null){
                        //Set transparent this canvas
                        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        synchronized (mSurfaceHolder) {
                            //To do work
                            for (Star s: starList){
                                s.paint(c);
                            }
                        }
                    }
                } finally {
                    if( c != null) {
                        //Unlock for canvas
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        //Controlling thread through this method
        public void setRunning(boolean b) {
            this.mRun = b;
        }
    }
}
