package com.kim.timingshot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.content.res.Resources;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 2017-05-28.
 */

//Referencing Lunar Lander Game of API Samples to make.
//Make custom view by inheritance SurfaceView
//Draw star image by this view
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    class GameThread extends Thread {
        /* Physics constants */
        public static final double PHYS_ACCEL_SPEED = 5.0;
        public static final double PHYS_SPEED_INIT = 3.0;
        /* State-tracking constants */
        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_READY = 3;
        public static final int STATE_RUNNING = 4;
        public static final int STATE_WIN = 5;
        /* UI constants (i.e. the speed & fuel bars) */
        private static final String KEY_X = "mX";
        private static final String KEY_Y = "mY";
        private static final String KEY_DX = "mDX";
        private static final String KEY_DY = "mDY";
        private static final String KEY_HEADING = "mHeading";
        private static final String KEY_ROT_ANGLE = "mAngle";
        private static final String KEY_STAR_HEIGHT = "mStarHeight";
        private static final String KEY_STAR_WIDTH = "mStarWidth";
        private static final String KEY_PLAYER_HEIGHT = "mPlayerHeight";
        private static final String KEY_PLAYER_WIDTH = "mPlayerWidth";
        /* Initial constants */
        public static final int INIT_STAR_COUNT = 4;

    /* Member (state) fields */
        /**
         * X/Y of each sprites. : Star/Player/Bullet
         */
        private double mPlayerX;
        private double mPlayerY;
        protected int mScoreX;
        protected int mScoreY;
        /**
         * Velocity dx/dy.
         */
        private double mDX;
        private double mDY;

        /**
         * Current width/height of the surface/canvas. @see #setSurfaceSize
         */
        private int mCanvasWidth = 1;
        private int mCanvasHeight = 1;
        /**
         * What to draw for the Star/Player/Bullet in its normal state
         */
        private Drawable mStarImage;
        private Drawable mPlayerImage;
        private Drawable mBulletImage;
        private Drawable mScoreImage;
        /**
         * Objects of sprites
         */
        private PlayerSprite player;
        private CopyOnWriteArrayList<StarSprite> starList = new CopyOnWriteArrayList<>();
        private CopyOnWriteArrayList<BulletSprite> bulletList = new CopyOnWriteArrayList<>();
        /**
         * Pixel width/height of Star image.
         */
        private int mPlayerWidth;
        private int mPlayerHeight;
        private int mBulletWidth;
        private int mBulletHeight;
        /**
         * Message handler used by thread to interact with TextView
         */
        private Handler mHandler;
        /**
         * Lander heading in degrees, with 0 up, 90 right. Kept in the range: 0..360.
         */
        private double mHeading;
        /** Accelerator rotate speed, with accumulating heading value */
        private double mAccelHeading;
        /**
         * The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN
         */
        private int mMode;
        /**
         * Indicate whether the surface has been created & is ready to draw
         */
        private boolean mRun = false;
        private final Object mRunLock = new Object();
        /**
         * Handle to the surface manager object we interact with
         */
        private SurfaceHolder mSurfaceHolder;

        private Resources res;

        private SoundPool expEffect = null;
        private int expEffectId = 0;

        private int scoreCnt = 0;

        private GameView mView;

        public GameThread(GameView view, SurfaceHolder surfaceHolder, Context context, Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;
            mView = view;
            res = context.getResources();


            expEffect = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            expEffectId = expEffect.load(mContext, R.raw.explosion, 1);

            mStarImage = context.getResources().getDrawable(R.drawable.star);
            mPlayerImage = context.getResources().getDrawable(R.drawable.player_arrow);
            mPlayerWidth = mPlayerImage.getIntrinsicWidth();
            mPlayerHeight = mPlayerImage.getIntrinsicHeight();
            mScoreImage = context.getResources().getDrawable(R.drawable.score);
        }

        /**
         * Stats the game, setting parameters
         */
        public void doStart() {
            synchronized (mSurfaceHolder) {
                Log.i(this.getClass().getName(), "gameThread doStart()");
                mAccelHeading = 3.0;
                mScoreX = -100;
                mScoreY = -100;
                double centerX = mCanvasWidth / 2.0;
                double centerY = mCanvasHeight / 2.0;
                mPlayerX = (mCanvasWidth / 2) - (mPlayerWidth / 2);
                mPlayerY = (mCanvasHeight / 2) - (mPlayerHeight / 2);
                player = new PlayerSprite(this, mPlayerImage, (int)mPlayerX, (int)mPlayerY);
                int r;
                for(int i = 0; i < INIT_STAR_COUNT*2; i++) {
                    int sidePos = i % INIT_STAR_COUNT;
                    switch ( sidePos ){
                        case 0:
                            r = (int)(Math.random() * mCanvasWidth);
                            starList.add(new StarSprite(this, mStarImage, r, 0));
                            break;
                        case 1:
                            r = (int)(Math.random() * mCanvasHeight);
                            starList.add(new StarSprite(this, mStarImage, 0, r));
                            break;
                        case 2:
                            r = (int)(Math.random() * mCanvasWidth);
                            starList.add(new StarSprite(this, mStarImage, r, mCanvasHeight-mStarImage.getIntrinsicHeight()));
                            break;
                        case 3:
                            r = (int)(Math.random() * mCanvasHeight);
                            starList.add(new StarSprite(this, mStarImage, mCanvasWidth-mStarImage.getIntrinsicWidth(), r));
                            break;
                    }
                }

                for(int i = 0; i < starList.size(); i++){
                    if(starList.get(i) instanceof StarSprite){
                        double velocity = (Math.random() * 0.4) + 0.1;
                        mDX = ( ((starList.get(i).getCx() - centerX) / centerX) * 13.28 ) * velocity;
                        mDY = ( ((starList.get(i).getCy() - centerY) / centerY) * 8.48 ) * velocity;
                        mDX = -mDX;
                        mDY = -mDY;
                    /*
                        int n = 100;
                        mDX = ( (centerX - sprites.get(i).getCx()) / n );
                        mDY = ( (centerY - sprites.get(i).getCy()) / n );
                    */
                        starList.get(i).setDx(mDX);
                        starList.get(i).setDy(mDY);
                    }
                }

                setState(STATE_RUNNING);
            }
        }

        /**
         * Pauses the physics update & animation
         */
        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
            }
        }

        public void doEnd(){
            gameOver();
        }

        @Override
        public void run() {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        if (mMode == STATE_RUNNING) updatePhysics();

                        synchronized (mRunLock) {
                            if (mRun) doDraw(c);
                        }
                    }

                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         *
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) {
            // Do not allow mRun to be modified while any canvas operations
            // are potentially in-flight. See doDraw().
            synchronized (mRunLock) {
                mRun = b;
            }
        }


        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         *
         * @param mode one of the STATE_* constants
         * @see #setState(int, String)
         */
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, "0");
            }
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         *
         * @param mode    one of the STATE_* constants
         * @param message string to add to screen or null
         */
        public void setState(int mode, String message) {
            /*
             * This method optionally can cause a text message to be displayed
             * to the user when the mode changes. Since the View that actually
             * renders that text is part of the main View hierarchy and not
             * owned by this thread, we can't touch the state of that View.
             * Instead we use a Message + Handler to relay commands to the main
             * thread, which updates the user-text View.
             */
            synchronized (mSurfaceHolder) {
                mMode = mode;
                if (mMode == STATE_RUNNING) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", message);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;
            }
        }

        /* Return width/height of this canvas */
        public int getmCanvasWidth(){ return mCanvasWidth; }
        public int getmCanvasHeight(){ return mCanvasHeight; }
        /* Return List of starSprites/bulletSprites */
        public CopyOnWriteArrayList<BulletSprite> getBulletList(){ return bulletList; }
        public CopyOnWriteArrayList<StarSprite> getStarList(){ return starList; }
        public Drawable getmScoreImage(){ return mScoreImage; }
        /**
         * Resumes from a pause.
         */
        public void unpause() {
            setState(STATE_RUNNING);
        }

        private Bitmap getRotatedBullet(){
            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bullet);
            int w = bmp.getWidth();
            int h = bmp.getHeight();
            thread.mBulletWidth = w;
            thread.mBulletHeight = h;
            Matrix mat = new Matrix();
            mat.postRotate((float)mHeading);
            mat.postScale((float)3.0, (float)3.0);
            Bitmap resBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, mat, true);
            bmp.recycle();

            return resBmp;
        }

        /**
         * Draws the Star/Player sprite to the provided Canvas.
         */
        private void doDraw(Canvas canvas) {
            //Set transparent this canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            for(StarSprite star: starList){
                star.draw(canvas);
            }

            for(BulletSprite bullet : bulletList){
                bullet.draw(canvas);
            }

            mScoreImage.setBounds(mScoreX, mScoreY, (mScoreX + mScoreImage.getIntrinsicWidth()), (mScoreY + mScoreImage.getIntrinsicHeight()));
            mScoreImage.draw(canvas);
            canvas.save();
            mHeading = (mHeading + mAccelHeading) % 360.0;
            canvas.rotate((float)(mHeading), (float)(mCanvasWidth/2.0), (float)(mCanvasHeight/2.0));
            player.draw(canvas);

            canvas.restore();
        }

        private void updatePhysics() {
            // Move StarSprite to center position
            synchronized (mSurfaceHolder) {
                for (StarSprite star : starList) {
                    star.move();
                }

                for (BulletSprite bullet : bulletList) {
                    bullet.move();
                }

                for(Sprite star : starList){
                    for(Sprite bullet : bulletList){
                        if(bullet.checkCollision(star)){
                            if(effectSetting) {
                                expEffect.play(expEffectId, 1, 1, 1, 0, 1);
                                Log.i(this.getClass().getName(), "play explosion");
                            }
                            scoreCnt++;
                            setState(STATE_RUNNING, Integer.toString(scoreCnt));
                            bullet.handleCollision(star);
                        }
                    }
                    if(player.checkCollision(star)) {
                        player.handleCollision(star);
                    }
                }
            }
        }

        private double getmAccelHeading(){
            return mAccelHeading;
        }

        private void setmAccelHeading(double heading){
            this.mAccelHeading = heading;
        }
    }



    /**
     * Handle to the application context, used to e.g. fetch Drawables.
     */
    private Context mContext;
    /**
     * Pointer to the text view to display "Paused.." etc.
     */
    private TextView mScoreText;
    private TextView mClockText;
    /**
     * The thread that actually draws the animation
     */
    private GameThread thread;

    private SoundPool shootEffect = null;
    private int shootEffectId = 0;
    private Vibrator vibe = null;
    private SharedPreferences settings = null;
    private Boolean vibeSetting = true;
    private Boolean effectSetting = true;



    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // register our interest in hearing about changes to our surface
        // Background of SurfaceView is apply translucent
        SurfaceHolder holder = getHolder();
        this.setZOrderOnTop(true);
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);

        // Initialize to sound effect on click button
        shootEffect = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        shootEffectId = shootEffect.load(context, R.raw.shoot, 1);
        // Initialize vibrator setting
        vibe = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize SharedPreferences Object
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        effectSetting = settings.getBoolean("effect", true);
        vibeSetting = settings.getBoolean("vibrate", true);

        // create thread only; it's started in surfaceCreated()
        Log.i(this.getClass().getName(), "before create gameThread");
        thread = new GameThread(this, holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                // mStatusText.setVisibility( m.getData().getInt("viz"));
                mScoreText.setText(m.getData().getString("text"));
            }
        });
        Log.i(this.getClass().getName(), "after create gameThread");
        setFocusable(true); // make sure we get key events
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     */
    public GameThread getThread() {
        return thread;
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on focus lost.
     * e.g. user switches to take a call.
     *
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) thread.pause();
        else thread.unpause();
    }

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView scoreText, TextView clockText) {
        mScoreText = scoreText;
        mClockText = clockText;
    }

    public void gameOver(){
        Intent intent = new Intent(mContext, ResultActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("score", mScoreText.getText());
        intent.putExtra("time", mClockText.getText());
        mContext.startActivity(intent);
    }

    //If SurfaceView is created, calling this method
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //If SurfaceView is available, starting thread
        thread.setRunning(true);
        thread.start();
        Thread progressTime = new PrgsTimeThread(new Handler() {
            @Override
            public void handleMessage(Message m) {
                mClockText.setText(m.getData().getString("time")+ "s");
            }
        });
        progressTime.start();
        Log.i(this.getClass().getName(), "surfaceCreated");
    }

    // Callback invoked when the surface dimensions change.
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(this.getClass().getName(), "surfaceChanged");
        thread.setSurfaceSize(width, height);
        thread.doStart();
    }

    //If SurfaceView is destroyed, calling this method
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        //If SurfaceView is not available, stop thread
        //And merge this thread to main thread
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }

        Log.i(this.getClass().getName(), "surfaceDestroyed");
    }

    boolean actionFlag = true;
    Thread touchThread;
    @Override
    public boolean onTouchEvent(MotionEvent e){
        switch(e.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.i(this.getClass().getName(), "onTouchDown");
                actionFlag = true;
                touchThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(actionFlag){
                            try {
                                thread.setmAccelHeading(thread.getmAccelHeading() + GameThread.PHYS_ACCEL_SPEED);
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                touchThread.start();
                break;
            case MotionEvent.ACTION_UP:
                Log.i(this.getClass().getName(), "onTouchUp");

                if(effectSetting) {
                    shootEffect.play(shootEffectId, 1, 1, 1, 0, 1);
                }

                if(vibeSetting){
                    vibe.vibrate(100);
                }

                actionFlag = false;
                touchThread = null;
                thread.setmAccelHeading(GameThread.PHYS_SPEED_INIT);
                thread.mBulletImage = new BitmapDrawable(thread.getRotatedBullet());

                double bX = 0.0, bY = 0.0;
                double r = thread.mPlayerHeight /1.5;
                double bw = thread.mBulletImage.getIntrinsicWidth() /2;
                double bh = thread.mBulletImage.getIntrinsicHeight()/2;
                double rad = Math.toRadians(thread.mHeading);
                if( (0.0 <= thread.mHeading) && (thread.mHeading < 90.0) ){
                    bX = (Math.sin(rad) * r);
                    bY = -(Math.cos(rad) * r);
                } else if( (90.0 <= thread.mHeading) && (thread.mHeading < 180.0) ){
                    bX = (Math.sin(rad) * r);
                    bY = -(Math.cos(rad) * r);
                } else if( (180.0 <= thread.mHeading) && (thread.mHeading< 270.0) ){
                    bX = (Math.sin(rad) * r);
                    bY = -(Math.cos(rad) * r);
                } else if( (270.0 <= thread.mHeading) && (thread.mHeading< 360.0) ){
                    bX = (Math.sin(rad) * r);
                    bY = -(Math.cos(rad) * r);
                }

                double cx = thread.mCanvasWidth / 2;
                double cy = thread.mCanvasHeight /2;
                double x = cx + bX;
                double y = cy + bY;
                thread.bulletList.add( new BulletSprite(thread, thread.mBulletImage, (int)(x-bw), (int)(y-bh), bX, bY));
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }

        return true;
    }

    class PrgsTimeThread extends Thread {
        private Handler mHandler;
        private int time = 0;

        public PrgsTimeThread(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void run() {
            while(thread.mRun) {
                try {
                    sleep(1000);
                    time++;
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("time", Integer.toString(time));
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}