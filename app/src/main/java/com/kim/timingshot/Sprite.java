package com.kim.timingshot;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2017-06-01.
 */

//Base of Game images and object
public class Sprite {
    protected int x = -1; // X of current position
    protected int y = -1; // Y of current position
    protected double cX = -1; // X of image's center
    protected double cY = -1; // Y of image's center
    protected double dx = 0; // Velocity of X
    protected double dy = 0; // Velocity of y
    protected int width = 0; // Width of image
    protected int height = 0; // Height of image

    protected Drawable image = null;

    public Sprite(Drawable img, int x, int y){
        this.image = img;
        this.x = x;
        this.y = y;
        this.width = this.image.getIntrinsicWidth();
        this.height = this.image.getIntrinsicHeight();
        this.cX = this.x + (this.width / 2);
        this.cY = this.y + (this.height / 2);
    }

    // Return this sprite to width/height
    public int getWidth(){
        return this.width;
    }
    public int getHeight(){
        return this.height;
    }

    // Moving sprite
    public void move() {
        x = x + (int)dx;
        y = y + (int)dy;
    }

    // Drawing sprite on SurfaceView
    public void draw(Canvas c) {
        image.setBounds(x, y, x+width, y+height);
        image.draw(c);
    }

    // Setting dx, dy
    public void setDx(double dx) { this.dx = dx; }
    public void setDy(double dy) { this.dy = dy; }

    // Return x, y, dx, dy, cX, cY
    public int getX() { return this.x; }
    public int getY() { return this.y; }
    //public int getDx() { return this.dx; }
    //public int getDy() { return this.dy; }
    public double getCx() { return this.cX; }
    public double getCy() { return this.cY; }

    // Checking collision the other sprite. if collision, return true
    public boolean checkCollision(Sprite other) {
        Rect myRect;
        if( this instanceof PlayerSprite){
            myRect = new Rect(this.x, this.y, this.x+this.width-50, this.y+this.height-50);
        } else {
            myRect = new Rect(this.x, this.y, this.x + this.width, this.y + this.height);
        }
        Rect otherRect = new Rect(other.x, other.y, other.x+other.width, other.y+other.height);

        return myRect.intersect(otherRect);
    }

    // Processing collision
    public void handleCollision(Sprite other) {

    }


}
