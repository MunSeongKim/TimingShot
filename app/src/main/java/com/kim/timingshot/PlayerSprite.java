package com.kim.timingshot;

import android.graphics.drawable.Drawable;
/**
 * Created by Administrator on 2017-06-02.
 */

public class PlayerSprite extends Sprite {
    private GameView.GameThread thread;
    public PlayerSprite(GameView.GameThread thread, Drawable img, int x, int y) {
        super(img, x, y);
        this.thread = thread;
        dx = dy = 0;
    }

    @Override
    public void handleCollision(Sprite other){
        if( other instanceof StarSprite ){
            thread.setRunning(false);
            thread.doEnd();
        }
    }

}
