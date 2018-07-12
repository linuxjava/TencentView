package com.example.tencentview.spotlight;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.tencentview.R;
import com.example.tencentview.waveview.WaveView;

/**
 * Created by robincxiao on 2018/7/9.
 */

public class SpotlightActivity extends Activity {
    private SpotLightView spotLightView1;
    private SpotLightView spotLightView2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_light);

        spotLightView1 = findViewById(R.id.spotlightview1);
        spotLightView2 = findViewById(R.id.spotlightview2);

        spotLightView1.setOneShot(false);
        spotLightView1.setColor(Color.BLUE);
        spotLightView1.setStrokeWidth(4);
        spotLightView1.setDuration(1200);
        spotLightView1.startAnimation();

        spotLightView2.setOneShot(true);
        spotLightView2.setColor(Color.BLUE);
        spotLightView2.setStrokeWidth(4);
        spotLightView2.setDuration(3000);
        spotLightView2.startAnimation();
    }

}
