package com.kim.timingshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Setting on background image to game
        ImageView board = (ImageView)findViewById(R.id.boardImg);
        Bitmap bit = BitmapFactory.decodeResource(getResources(), R.drawable.board);
        board.setImageBitmap(bit);
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        board.setAnimation(rotate);
    }
}
