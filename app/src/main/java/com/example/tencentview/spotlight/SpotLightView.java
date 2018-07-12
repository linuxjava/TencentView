package com.example.tencentview.spotlight;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by robincxiao on 2017/7/28.
 */

public class SpotLightView extends View {
    private static final int DEAULT_WIDTH = 200;
    private static final int DEAULT_HEIGHT = 200;
    private static final float SQRT3 = (float) Math.sqrt(3);
    private Context mContext;
    private int radiu;
    private Path hexagonPath;
    private Path dstPath;
    private Paint paint;
    private ValueAnimator animator;
    private float mAnimatorValue = 0;
    private PathMeasure pathMeasure;
    private int strokeWidth;

    public SpotLightView(Context context) {
        this(context, null);
    }

    public SpotLightView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpotLightView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = dp2px(mContext, strokeWidth);
        paint.setStrokeWidth(this.strokeWidth);
    }

    public void setDuration(int duration) {
        if (animator != null) {
            animator.setDuration(duration);
        }
    }

    private void init(Context context) {
        mContext = context;
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        strokeWidth = dp2px(mContext, 4);
        paint.setStrokeWidth(strokeWidth);

        dstPath = new Path();
        pathMeasure = new PathMeasure();

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1200);
        animator.setRepeatCount(-1);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public void setOneShot(boolean oneShot) {
        if (oneShot) {
            animator.setRepeatCount(0);
        } else {
            animator.setRepeatCount(-1);
        }
    }

    public void startAnimation() {
        animator.start();
    }

    public void stop() {
        animator.cancel();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(dp2px(mContext, DEAULT_WIDTH), dp2px(mContext, DEAULT_HEIGHT));
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(dp2px(mContext, DEAULT_WIDTH), heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, dp2px(mContext, DEAULT_HEIGHT));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        radiu = getWidth() / 2 - strokeWidth;
        hexagonPath = getHexagonPath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.scale(-1, -1);//通过改变坐标系，改变旋转方向

        pathMeasure.setPath(hexagonPath, false);
        dstPath.reset();
        float stop = pathMeasure.getLength() * mAnimatorValue;
        float start = (float) (stop - ((0.5 - Math.abs(mAnimatorValue - 0.5)) * pathMeasure.getLength() * 2 / 3));

        pathMeasure.getSegment(start, stop, dstPath, true);
        canvas.drawPath(dstPath, paint);
    }

    private Path getHexagonPath() {
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

    public int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}
