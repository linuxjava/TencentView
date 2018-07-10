package com.example.tencentview.popstarview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.tencentview.R;

/**
 * Created by robincxiao on 2018/7/9.
 */

public class PopStarActivity extends Activity {
    private PopStarView mPopStarView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_star);

        mPopStarView = findViewById(R.id.popstarview);
        mPopStarView.setListener(new PopStarView.IAnimListener() {
            @Override
            public void onStart() {
                Log.d("xiao1", "PopStarView start");
            }

            @Override
            public void onFinish() {
                Log.d("xiao1", "PopStarView finish");
            }
        });
    }

    public void onStart(View view) {
        mPopStarView.startAnimation();
    }
}
