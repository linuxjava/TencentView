package com.example.tencentview.hexagonloading;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.tencentview.R;
import com.example.tencentview.popstarview.PopStarView;

/**
 * Created by robincxiao on 2018/7/9.
 */

public class HexagonLoadingActivity extends Activity {
    private HexagonLoadingView hexagonLoadingView1;
    private HexagonLoadingView hexagonLoadingView2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hexagon_loading);

        hexagonLoadingView1 = findViewById(R.id.hexagonloading1);
        hexagonLoadingView1.setLoadingViewByType(HexagonLoadingView.TYPE_NOMAL);

        hexagonLoadingView2 = findViewById(R.id.hexagonloading2);
        hexagonLoadingView2.setLoadingViewByType(HexagonLoadingView.TYPE_SPECIAL);
        hexagonLoadingView2.setStrokeWidth(5);
    }

    public void onStart(View view){
        hexagonLoadingView1.startAnimation();
        hexagonLoadingView2.startAnimation();
    }
}
