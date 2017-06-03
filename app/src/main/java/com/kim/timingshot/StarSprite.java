package com.kim.timingshot;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.kim.timingshot.GameView.GameThread.INIT_STAR_COUNT;

/**
 * Created by Administrator on 2017-06-01.
 */

public class StarSprite extends Sprite {
    private GameView.GameThread thread;
    private Drawable starImage;
    private int canvasWidth, canvasHeight;
    public StarSprite(GameView.GameThread thread, Drawable img, int x, int y) {
        super(img, x, y);
        this.thread = thread;
        starImage = img;
        canvasWidth = thread.getmCanvasWidth();
        canvasHeight = thread.getmCanvasHeight();
    }

    @Override
    public void move(){
        if( ((dx < 0) && (x < 0)) || ((dx > 0) && (x > canvasWidth-width)) ){
            thread.getStarList().remove(this);
            addStarSprite(thread.getStarList());
            return ;
        }
        if( ((dy < 0) && (y < 0)) || ((dy > 0 ) && (y > canvasHeight-height)) ){
            thread.getStarList().remove(this);
            addStarSprite(thread.getStarList());
            return ;
        }
        super.move();
    }

    public Drawable getImg(){
        return this.starImage;
    }

    public void addStarSprite(CopyOnWriteArrayList<StarSprite> starList){
        int r;
        int sidePos = (int)(Math.random() * 4);
        switch ( sidePos ){
            case 0:
                r = (int)(Math.random() * canvasWidth);
                starList.add(new StarSprite(thread, starImage, r, 0));
                break;
            case 1:
                r = (int)(Math.random() * canvasHeight);
                starList.add(new StarSprite(thread, starImage, 0, r));
                break;
            case 2:
                r = (int)(Math.random() * canvasWidth);
                starList.add(new StarSprite(thread, starImage, r, canvasHeight-height));
                break;
            case 3:
                r = (int)(Math.random() * canvasHeight);
                starList.add(new StarSprite(thread, starImage, canvasWidth-width, r));
                break;
        }

        for(int i = 0; i < starList.size(); i++){
            if(starList.get(i) instanceof StarSprite){
                double velocity = (Math.random() * 0.4) + 0.1;
                double mDX = ( ((starList.get(i).getCx() - (canvasWidth/2)) / (canvasWidth/2)) * 13.28 ) * velocity;
                double mDY = ( ((starList.get(i).getCy() - (canvasHeight/2)) / (canvasHeight/2)) * 8.48 ) * velocity;
                mDX = -mDX;
                mDY = -mDY;
                starList.get(i).setDx(mDX);
                starList.get(i).setDy(mDY);
            }
        }
    }
}
