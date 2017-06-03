package com.kim.timingshot;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawableResource;

/**
 * Created by Administrator on 2017-06-02.
 */

public class BulletSprite extends Sprite {
    private GameView.GameThread thread;

    public BulletSprite(GameView.GameThread thread, Drawable img, int x, int y, double cx, double cy){
        super(img, x, y);
        this.thread = thread;
        dx = cx / 3;
        dy = cy / 3;
    }

    @Override
    public void move(){
        if( ((dx < 0) && (x < 0)) || ((dx > 0) && (x > thread.getmCanvasWidth()-width)) ){
            thread.getBulletList().remove(this);
            return  ;
        }
        if( ((dy < 0) && (y < 0)) || ((dy > 0 ) && (y > thread.getmCanvasHeight()-height)) ){
            thread.getBulletList().remove(this);
            return ;
        }
        super.move();
    }

    @Override
    public void handleCollision(Sprite other){
        if( other instanceof StarSprite ){
            Drawable tmpImg = ((StarSprite)other).getImg();
            thread.mScoreX = other.getX();
            thread.mScoreY = other.getY();
            thread.getBulletList().remove(this);
            thread.getStarList().remove(other);
            StarSprite tmpStar = new StarSprite(thread, tmpImg, 0, 0);
            tmpStar.addStarSprite(thread.getStarList());
        }
    }
}
