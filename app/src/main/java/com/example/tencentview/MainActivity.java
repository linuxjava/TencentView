package com.example.tencentview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.tencentview.hexagonloading.HexagonLoadingActivity;
import com.example.tencentview.panelview.PanelViewActivity;
import com.example.tencentview.popstarview.PopStarActivity;
import com.example.tencentview.radarview.RadarViewActivity;
import com.example.tencentview.scoreview.ScoreViewActivity;
import com.example.tencentview.spotlight.SpotlightActivity;
import com.example.tencentview.sweepview.SweepViewActivity;
import com.example.tencentview.waveview.WaveActivity;
import com.example.tencentview.wifigrid.WifiGridActivity;

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

    public void onHexagonLoadingView(View view){
        startActivity(new Intent(this, HexagonLoadingActivity.class));
    }

    public void onWaveView(View view){
        startActivity(new Intent(this, WaveActivity.class));
    }

    public void onSpotLight(View view){
        startActivity(new Intent(this, SpotlightActivity.class));
    }

    public void onSweepView(View view){
        startActivity(new Intent(this, SweepViewActivity.class));
    }

    public void onPanelView(View view){
        startActivity(new Intent(this, PanelViewActivity.class));
    }

    public void onWifiGrid(View view){
        startActivity(new Intent(this, WifiGridActivity.class));
    }
}
