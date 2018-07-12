package com.example.tencentview.panelview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.tencentview.R;
import com.example.tencentview.popstarview.PopStarView;

/**
 * Created by robincxiao on 2018/7/9.
 */

public class PanelViewActivity extends Activity {
    private PanelView panelView;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            float angle = (float) getRandom();
            panelView.setSpeed(angle);
            handler.sendEmptyMessageDelayed(1, 1000);
            return true;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_view);

        panelView = findViewById(R.id.panelview);
    }

    public void onStart(View view) {
        handler.sendEmptyMessage(1);
    }

    private double getRandom() {
        return Math.random() * 100;
    }
}
