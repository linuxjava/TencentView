package com.example.tencentview.scoreview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.tencentview.R;

/**
 * Created by robincxiao on 2018/7/3.
 */

public class ScoreViewActivity extends Activity {
    private ScoreView mScoreView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_view);

        mScoreView = findViewById(R.id.scoreview);
        mScoreView.setListener(new ScoreView.IAnimListener() {
            @Override
            public void onScrollStart() {
                Log.d("xiao1", "onScrollStart");
            }

            @Override
            public void onScrollEnd() {
                Log.d("xiao1", "onScrollEnd");
            }
        });
    }

    public void onStart(View view){
        mScoreView.start();
    }

    public void onFinishWithAnim(View view){
        mScoreView.setScore(56, true);
    }

    public void onFinishNoAnim(View view){
        mScoreView.setScore(9, false);
    }
}
