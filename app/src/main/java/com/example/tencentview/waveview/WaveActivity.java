package com.example.tencentview.waveview;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.tencentview.R;
import com.example.tencentview.hexagonloading.HexagonLoadingView;

/**
 * Created by robincxiao on 2018/7/9.
 */

public class WaveActivity extends Activity {
    private WaveView waveView1;
    private WaveView waveView2;
    private WaveView waveView3;
    private WaveView waveView4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave);

        waveView1 = findViewById(R.id.waveview1);
        waveView2 = findViewById(R.id.waveview2);
        waveView3 = findViewById(R.id.waveview3);
        waveView4 = findViewById(R.id.waveview4);

        waveView1.setShapeType(WaveView.TYPE_CIRCLE);
        waveView1.setDuration(5000);
        waveView1.setStyle(Paint.Style.STROKE);
        waveView1.setSpeed(500);
        waveView1.setColor(Color.RED);
        waveView1.setInterpolator(new AccelerateDecelerateInterpolator());

        waveView2.setShapeType(WaveView.TYPE_CIRCLE);
        waveView2.setDuration(5000);
        waveView2.setStyle(Paint.Style.FILL);
        waveView2.setSpeed(500);
        waveView2.setColor(Color.RED);
        waveView2.setInterpolator(new AccelerateDecelerateInterpolator());

        waveView3.setShapeType(WaveView.TYPE_HEXAGON);
        waveView3.setDuration(5000);
        waveView3.setStyle(Paint.Style.STROKE);
        waveView3.setColor(Color.RED);
        waveView3.setInterpolator(new LinearOutSlowInInterpolator());

        waveView4.setShapeType(WaveView.TYPE_HEXAGON);
        waveView4.setDuration(5000);
        waveView4.setStyle(Paint.Style.FILL);
        waveView4.setColor(Color.RED);
        waveView4.setInterpolator(new LinearOutSlowInInterpolator());
    }

    public void onStart(View view){
        waveView1.start();
        waveView2.start();
        waveView3.start();
        waveView4.start();
    }
}
