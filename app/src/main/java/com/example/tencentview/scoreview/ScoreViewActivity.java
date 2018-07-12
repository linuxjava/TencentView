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
    private ScoreViewFull mScoreViewFull;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_view);

        mScoreView = findViewById(R.id.scoreview);
        mScoreViewFull = findViewById(R.id.scoreview1);
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

        mScoreViewFull.setListener(new ScoreViewFull.IAnimListener() {
            @Override
            public void onScrollStart() {
                Log.d("xiao1", "ScoreViewFull_onScrollStart");
            }

            @Override
            public void onScrollEnd() {
                Log.d("xiao1", "ScoreViewFull_onScrollEnd");
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

    public void onStart1(View view){
        mScoreViewFull.start();
    }

    public void onReset(View view){
        mScoreViewFull.reset();
    }

    public void onFinishWithAnim1(View view){
        mScoreViewFull.setScore(99, true);
    }

    public void onFinishNoAnim1(View view){
        mScoreViewFull.setScore(56, false);
    }
}
