package com.example.tencentview.sweepview;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.tencentview.R;
import com.example.tencentview.spotlight.SpotLightView;

/**
 * Created by robincxiao on 2018/7/9.
 */

public class SweepViewActivity extends Activity {
    private SweepView sweepView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sweep_view);

        sweepView = findViewById(R.id.sweepview);
    }

    public void onStart(View view){
        sweepView.startAnmation();
    }
}
