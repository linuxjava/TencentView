package com.example.tencentview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.tencentview.popstarview.PopStarActivity;
import com.example.tencentview.radarview.RadarViewActivity;
import com.example.tencentview.scoreview.ScoreViewActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onScoreView(View view){
        startActivity(new Intent(this, ScoreViewActivity.class));
    }

    public void onRadarView(View view){
        startActivity(new Intent(this, RadarViewActivity.class));
    }

    public void onPopStarView(View view){
        startActivity(new Intent(this, PopStarActivity.class));
    }
}
