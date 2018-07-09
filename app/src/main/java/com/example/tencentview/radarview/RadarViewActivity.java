package com.example.tencentview.radarview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.tencentview.R;

/**
 * Created by robincxiao on 2018/7/9.
 */

public class RadarViewActivity extends Activity {
    private RadarView mRadarView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar_view);

        mRadarView = findViewById(R.id.radarview);
        mRadarView.setListener(new RadarView.IAnimListener() {
            @Override
            public void onStart() {
                Log.d("xiao1", "onStart");
            }

            @Override
            public void onEnd() {
                Log.d("xiao1", "onEnd");
            }
        });
    }

    public void onStart(View view){
        mRadarView.startAnimation();
    }

    public void onFinishWithAnim(View view){
        mRadarView.fadeOutAnimation();
    }

    public void onFinishNoAnim(View view){
        mRadarView.stopAnimation();
    }
}
