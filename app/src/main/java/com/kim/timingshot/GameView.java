package com.kim.timingshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.res.Resources;

/**
 * Created by Administrator on 2017-05-28.
 */

//Referencing Lunar Lander Game of API Samples to make.
//Make custom view by inheritance SurfaceView
//Draw star image by this view
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    class GameThread extends Thread {
        /* Physics constants */
        public static final int PHYS_SLEW_SEC = 120; // degrees/second rotate
        public static final int PHYS_SPEED_HYPERSPACE = 180;
        public static final int PHYS_SPEED_INIT = 30;
        public static final int PHYS_SPEED_MAX = 120;
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

    /* Member (state) fields */
        /**
         * X/Y of each sprites. : Star/Player/Bullet
         */
        private double mPlayerX;
        private double mPlayerY;
        private double mStarX;
        private double mStarY;
        private double mBulletX;
        private double mBulletY;
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
         * What to draw for the Star/Player/Bullet when it has crashed
         */
        private Drawable mCrashedStar;
        private Drawable mCrashedPlayer;
        private Drawable mCrashedBulltet;
        /**
         * What to draw for the Star/Player/Bullet in its normal state
         */
        private Drawable mStarImage;
        private Drawable mPlayerImage;
        private Drawable mBulletImage;
        /**
         * Pixel width/height of Star/Player/Bullet image.
         */
        private int mStarWidth;
        private int mStarHeight;
        private int mPlayerWidth;
        private int mPlayerHeight;
        private int mBulletWidth;
        private int mBulletHeight;

        /**
         * Used to figure out elapsed time between frames
         */
        private long mLastTime;
        /**
         * Message handler used by thread to interact with TextView
         */
        private Handler mHandler;
        /**
         * Lander heading in degrees, with 0 up, 90 right. Kept in the range: 0..360.
         */
        private double mHeading;
        /**
         * The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN
         */
        private int mMode;
        /**
         * Currently rotating, -1 left, 0 none, 1 right.
         */
        private int mRotating;
        /**
         * Indicate whether the surface has been created & is ready to draw
         */
        private boolean mRun = false;
        private final Object mRunLock = new Object();

        /**
         * Handle to the surface manager object we interact with
         */
        private SurfaceHolder mSurfaceHolder;

        public GameThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;

            Resources res = context.getResources();

            mStarImage = context.getResources().getDrawable(R.drawable.star);
            mPlayerImage = context.getResources().getDrawable(R.drawable.player);
            mBulletImage = context.getResources().getDrawable(R.drawable.bullet);

            mPlayerWidth = mPlayerImage.getIntrinsicWidth();
            mPlayerHeight = mPlayerImage.getIntrinsicHeight();
            mStarWidth = mStarImage.getIntrinsicWidth();
            mStarHeight = mStarImage.getIntrinsicHeight();
            mBulletWidth = mBulletImage.getIntrinsicWidth();
            mBulletHeight = mBulletImage.getIntrinsicHeight();

            mPlayerX = mPlayerWidth;
            mPlayerY = mPlayerHeight * 2;
            mStarX = mStarWidth;
            mStarY = mStarHeight;
            mBulletX = mBulletWidth;
            mBulletY = mBulletHeight;
            mDX = 0;
            mDY = 0;
            mHeading = 0;
        }

        /**
         * Stats the game, setting parameters
         */
        public void doStart() {
            synchronized (mSurfaceHolder) {
                int speedInit = PHYS_SPEED_INIT;

                mPlayerX = mCanvasWidth / 2;
                mPlayerY = mCanvasHeight / 2;
                mStarX = mCanvasWidth / 2;
                mStarY = mCanvasHeight / 2;
                mBulletX = mCanvasWidth / 2;
                mBulletY = mCanvasHeight / 2;

                mHeading = 0;

                mLastTime = System.currentTimeMillis() + 100;
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

        @Override
        public void run() {
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
         * @see #setState(int, CharSequence)
         */
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         *
         * @param mode    one of the STATE_* constants
         * @param message string to add to screen or null
         */
        public void setState(int mode, CharSequence message) {
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
                    b.putString("text", "");
                    b.putInt("viz", View.INVISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                } else {
                    mRotating = 0;

                    Resources res = mContext.getResources();
                    CharSequence str = "";
                    /*
                    if (mMode == STATE_READY)
                        str = res.getText(R.string.mode_ready);
                    else if (mMode == STATE_PAUSE)
                        str = res.getText(R.string.mode_pause);
                    else if (mMode == STATE_LOSE)
                        str = res.getText(R.string.mode_lose);
                    else if (message != null) {
                        str = message + "\n" + str;
                    }*/

                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", str.toString());
                    b.putInt("viz", View.VISIBLE);
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

        /**
         * Resumes from a pause.
         */
        public void unpause() {
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
            }
            setState(STATE_RUNNING);
        }

        /**
         * Draws the Star/Player sprite to the provided Canvas.
         */
        private void doDraw(Canvas canvas) {
            int yStarTop = mCanvasHeight - ((int) mStarY + mStarHeight / 2);
            int xStarLeft = (int) mStarX - mStarWidth / 2;
            int yPlayerTop = (int) mPlayerY; //mCanvasHeight // - ((int) mPlayerY + mPlayerHeight / 2);
            int xPlayerLeft = (int) mPlayerX ;// - mPlayerWidth / 2;
            int yBulletTop = mCanvasHeight - ((int) mBulletY + mBulletHeight / 2);
            int xBulletLeft = (int) mBulletX - mBulletWidth / 2;

            // Draw the player with its current rotation
            //canvas.rotate((float) mHeading, (float) mPlayerX, mCanvasHeight
            //        - (float) mPlayerY);
            /*
            if (mMode == STATE_LOSE) {
                mCrashedImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
                        + mLanderHeight);
                mCrashedImage.draw(canvas);
            } else if (mEngineFiring) {
                mFiringImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
                        + mLanderHeight);
                mFiringImage.draw(canvas);
            } else {
                mLanderImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
                        + mLanderHeight);
                mLanderImage.draw(canvas);
            }*/
            //Set transparent this canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mStarImage.setBounds(xStarLeft, yStarTop, xStarLeft+mStarWidth, yStarTop+mStarHeight);
            mStarImage.draw(canvas);

            mPlayerImage.setBounds(xPlayerLeft, yPlayerTop, xPlayerLeft+mPlayerWidth, yPlayerTop+mPlayerHeight);
            mPlayerImage.draw(canvas);

            Paint p = new Paint();
            p.setColor(Color.WHITE);
            p.setTextSize(100);
            canvas.drawText(Integer.toString(mCanvasHeight), mCanvasWidth/2, 200, p);
            canvas.drawText(Integer.toString(mCanvasWidth), mCanvasWidth/2, 400, p);


            canvas.save();
            canvas.restore();


        }

        private void updatePhysics() {

            long now = System.currentTimeMillis();

            // Do nothing if mLastTime is in the future.
            // This allows the game-start to delay the start of the physics
            // by 100ms or whatever.
            if (mLastTime > now) return;

            double elapsed = (now - mLastTime) / 1000.0;
            // mRotating -- update heading
            if (mRotating != 0) {
                mHeading += mRotating * (PHYS_SLEW_SEC * elapsed);

                // Bring things back into the range 0..360
                if (mHeading < 0)
                    mHeading += 360;
                else if (mHeading >= 360) mHeading -= 360;
            }

            // Base accelerations -- 0 for x, gravity for y
            double ddx = 0.0;
            double ddy = -((int) Math.random() * 10) * elapsed;
            double dxOld = mDX;
            double dyOld = mDY;
            // figure speeds for the end of the period
            mDX += ddx;
            mDY += ddy;
            // figure position based on average speed during the period
            mStarX += elapsed * (mDX + dxOld) / 2;
            mStarY += elapsed * (mDY + dyOld) / 2;
            mLastTime = now;
        }


        /**
         * Dump game state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         *
         * @return Bundle with this view's state
         */
/*
        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {
                if (map != null) {
                    map.putDouble(KEY_X, Double.valueOf(mStarX));
                    map.putDouble(KEY_Y, Double.valueOf(mStarY));
                    map.putDouble(KEY_DX, Double.valueOf(mDX));
                    map.putDouble(KEY_DY, Double.valueOf(mDY));
                    map.putDouble(KEY_HEADING, Double.valueOf(mHeading));
                    map.putInt(KEY_STAR_WIDTH, Integer.valueOf(mStarWidth));
                    map.putInt(KEY_STAR_HEIGHT, Integer
                            .valueOf(mStarHeight));
                }
            }
            return map;
        }
*/
        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         *
         * @param savedState Bundle containing the game state
         */
/*
         public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
                setState(STATE_PAUSE);
                mRotating = 0;

                mStarX = savedState.getDouble(KEY_X);
                mStarY = savedState.getDouble(KEY_Y);
                mDX = savedState.getDouble(KEY_DX);
                mDY = savedState.getDouble(KEY_DY);
                mHeading = savedState.getDouble(KEY_HEADING);

                mStarWidth = savedState.getInt(KEY_STAR_WIDTH);
                mStarHeight = savedState.getInt(KEY_STAR_HEIGHT);

            }
        }
        */
    }



    /**
     * Handle to the application context, used to e.g. fetch Drawables.
     */
    private Context mContext;
    /**
     * Pointer to the text view to display "Paused.." etc.
     */
    private TextView mStatusText;
    /**
     * The thread that actually draws the animation
     */
    private GameThread thread;


    //---------------------------
    //Private code
    //protected Star starList[] = new Star[10];
    //private StarSpliteThread starThread = null;
    //private Bitmap starImg = null;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        // Background of SurfaceView is apply translucent
        SurfaceHolder holder = getHolder();
        this.setZOrderOnTop(true);
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);

        // create thread only; it's started in surfaceCreated()
        thread = new GameThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                mStatusText.setVisibility( m.getData().getInt("viz"));
                mStatusText.setText(m.getData().getString("text"));
            }
        });

        setFocusable(true); //make sure we get key events
        /*
        starImg = BitmapFactory.decodeResource(getResources(), R.drawable.player);
        for (int i = 0; i < 10; i++){
            starList[i] = new Star(starImg);
        }

        starThread = new StarSpliteThread(holder);
        */
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
    }

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView) {
        mStatusText = textView;
    }

    //If SurfaceView is created, calling this method
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //If SurfaceView is available, starting thread
        thread.setRunning(true);
        thread.start();
        Log.i("SurfaceView", "Created");
    }

    // Callback invoked when the surface dimensions change.
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
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
        Log.i("SurfaceView", "Destroyed");
    }
}
/*
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
*/