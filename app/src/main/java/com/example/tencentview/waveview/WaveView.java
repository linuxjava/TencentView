package com.example.tencentview.waveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 水波纹特效
 * Created by fbchen2 on 2016/5/25.
 */
public class WaveView extends View {
    private static final float SQRT3 = (float) Math.sqrt(3);
    public static final int TYPE_CIRCLE = 0;
    public static final int TYPE_HEXAGON = 1;
    private Context mContext;
    private float mInitialRadius;   // 初始波纹半径
    private float mMaxRadius;   // 最大波纹半径
    private float mInitialWidth;   // 初始线宽
    private float mMaxWidth;   // 最大线宽
    private long mDuration = 2000; // 一个波纹从创建到消失的持续时间
    private int mSpeed = 500;   // 波纹的创建速度，每500ms创建一个
    private float mMaxRadiusRate = 0.85f;
    private boolean mMaxRadiusSet;

    private boolean mIsRunning;
    private long mLastCreateTime;
    private int shapeType = TYPE_HEXAGON;
    private List<Circle> mCircleList = new ArrayList<Circle>();
    private List<Hexagon> mHexagonList = new ArrayList<>();

    private Runnable mCreateCircle = new Runnable() {
        @Override
        public void run() {
            if (mIsRunning) {
                if(shapeType == TYPE_CIRCLE){
                    newCircle();
                }else if(shapeType == TYPE_HEXAGON){
                    newHexagon();
                }

                postDelayed(mCreateCircle, mSpeed);
            }
        }
    };

    private Interpolator mInterpolator = new LinearInterpolator();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context){
        mContext = context;
        mInitialWidth = dp2px(context, 0.2f);
        mMaxWidth = dp2px(context, 2);
    }

    public void setStyle(Paint.Style style) {
        mPaint.setStyle(style);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (!mMaxRadiusSet) {
            mMaxRadius = Math.min(w, h) * mMaxRadiusRate / 2.0f;
        }
    }

    public void setMaxRadiusRate(float maxRadiusRate) {
        mMaxRadiusRate = maxRadiusRate;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    /**
     * 开始
     */
    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            mCreateCircle.run();
        }
    }

    /**
     * 缓慢停止
     */
    public void stop() {
        mIsRunning = false;
    }

    /**
     * 立即停止
     */
    public void stopImmediately() {
        mIsRunning = false;
        mCircleList.clear();
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if(shapeType == TYPE_CIRCLE) {
            Iterator<Circle> iterator = mCircleList.iterator();
            while (iterator.hasNext()) {
                Circle circle = iterator.next();
                float radius = circle.getCurrentRadius();
                if (System.currentTimeMillis() - circle.mCreateTime < mDuration) {
                    mPaint.setAlpha(circle.getAlpha());
                    canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, mPaint);
                } else {
                    iterator.remove();
                }
            }
            if (mCircleList.size() > 0) {
                postInvalidateDelayed(40);
            }
        }else if(shapeType == TYPE_HEXAGON){
            canvas.translate(getWidth()/2, getHeight()/2);
            Iterator<Hexagon> iterator = mHexagonList.iterator();
            while (iterator.hasNext()) {
                Hexagon hexagon = iterator.next();
                if (System.currentTimeMillis() - hexagon.mCreateTime < mDuration) {
                    mPaint.setAlpha(hexagon.getAlpha());
                    mPaint.setStrokeWidth(hexagon.getWidth());
                    canvas.drawPath(hexagon.getHexagonPath(), mPaint);
                } else {
                    iterator.remove();
                }
            }

            if (mHexagonList.size() > 0) {
                postInvalidateDelayed(40);
            }
        }
    }

    public void setInitialRadius(float radius) {
        mInitialRadius = radius;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public void setMaxRadius(float maxRadius) {
        mMaxRadius = maxRadius;
        mMaxRadiusSet = true;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }

    public void setShapeType(int shapeType) {
        this.shapeType = shapeType;
    }

    private void newCircle() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastCreateTime < mSpeed) {
            return;
        }
        Circle circle = new Circle();
        mCircleList.add(circle);
        invalidate();
        mLastCreateTime = currentTime;
    }

    private class Circle {
        private long mCreateTime;

        Circle() {
            mCreateTime = System.currentTimeMillis();
        }

        int getAlpha() {
            float percent = (getCurrentRadius() - mInitialRadius) / (mMaxRadius - mInitialRadius);
            return (int) (255 - mInterpolator.getInterpolation(percent) * 255);
        }

        float getCurrentRadius() {
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / mDuration;
            return mInitialRadius + mInterpolator.getInterpolation(percent) * (mMaxRadius - mInitialRadius);
        }
    }

    class Hexagon {
        long mCreateTime;//创建时间

        Hexagon(){
            mCreateTime = System.currentTimeMillis();
        }

        int getAlpha() {
            float percent = (getCurrentRadius() - mInitialRadius) / (mMaxRadius - mInitialRadius);
            return (int) (255 - mInterpolator.getInterpolation(percent) * 255);
        }

        float getCurrentRadius() {
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / mDuration;
            return mInitialRadius + mInterpolator.getInterpolation(percent) * (mMaxRadius - mInitialRadius);
        }

        float getWidth(){
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / mDuration;
            return mInitialWidth + mInterpolator.getInterpolation(1 - percent) * (mMaxWidth - mInitialWidth);
        }

        Path getHexagonPath(){
            float radiu = getCurrentRadius();
            Path path = new Path();
            float dx = SQRT3 * radiu / 2;
            //计算六边形的六个顶点
            path.moveTo(0, radiu);
            path.lineTo(0 - dx, radiu / 2);
            path.lineTo(0 - dx, 0 - radiu / 2);
            path.lineTo(0, 0 - radiu);
            path.lineTo(dx, 0 - radiu / 2);
            path.lineTo(dx, radiu / 2);
            path.close();
            return path;
        }
    }

    private void newHexagon(){
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastCreateTime < mSpeed) {
            return;
        }
        Hexagon hexagon = new Hexagon();
        mHexagonList.add(hexagon);
        invalidate();
        mLastCreateTime = currentTime;
    }


    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
        if (mInterpolator == null) {
            mInterpolator = new LinearInterpolator();
        }
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}
