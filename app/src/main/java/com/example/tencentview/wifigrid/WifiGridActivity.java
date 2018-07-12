package com.example.tencentview.wifigrid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.tencentview.R;
import com.example.tencentview.panelview.PanelView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robincxiao on 2018/7/9.
 */

public class WifiGridActivity extends Activity {
    private WifiGridView wifiGridView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_grid);

        wifiGridView = findViewById(R.id.wifiview);

        List<Float> data = new ArrayList<>();
        data.add(0f);
        data.add(0.5f);
        data.add(2.1f);
        data.add(5f);
        data.add(105f);
        data.add(1.1f);
        data.add(3.4f);
        data.add(7f);
        wifiGridView.setData(data);
    }

}
