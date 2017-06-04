package com.kim.timingshot;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RankActivity extends AppCompatActivity {
    private ListView rankList;
    private ArrayList<HashMap<String, String>> allData;
    private DBHelper gameDb;
    private CustomList mAdapter;

    private int rankImgList[] = { R.drawable.rank1st, R.drawable.rank2nd, R.drawable.rank3rd };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        findViewById(R.id.rankBackground).setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rank_background));

        gameDb = new DBHelper(this);
        allData = gameDb.getAllDataByDesc(DBHelper.COLUMN_SCORE);
        mAdapter = new CustomList(this);
        rankList = (ListView)findViewById(R.id.rankList);

        rankList.setAdapter(mAdapter);
    }

    @Override
    protected void onResume(){
        super.onResume();
        findViewById(R.id.rankBackground).setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rank_background));

        mAdapter.clear();
        mAdapter.addAll(gameDb.getAllDataByDesc(DBHelper.COLUMN_SCORE));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        findViewById(R.id.rankBackground).setBackground(null);
    }

    //Apply external font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    public class CustomList extends ArrayAdapter<HashMap<String, String>> {
        private final Activity context;

        public CustomList(@NonNull Activity context) {
            super(context, R.layout.list_item, allData);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View itemView = inflater.inflate(R.layout.list_item, null, true);

            ImageView imageView = (ImageView)itemView.findViewById(R.id.rankImage);
            TextView name = (TextView)itemView.findViewById(R.id.itemName);
            TextView score = (TextView)itemView.findViewById(R.id.itemScore);
            TextView time = (TextView)itemView.findViewById(R.id.itemTime);

            if( position < 3 ){
                imageView.setImageResource(rankImgList[position]);
            } else {
                imageView.setVisibility(View.INVISIBLE);
            }

            HashMap<String, String> data = allData.get(position);

            Log.i(this.getClass().getName(), data.toString());
            Log.i(this.getClass().getName(), "position "+position);
            name.setText(data.get("name"));
            score.setText(data.get("score"));
            time.setText(data.get("time") + "s");

            return itemView;
        }
    }
}


