package com.example.tencentview.sweepview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by robincxiao on 2017/8/4.
 */

public class SweepView extends View {
    private static final int DEAULT_WIDTH = 200;
    private static final int DEAULT_HEIGHT = 200;
    private static final int COLOR_CIRCLE_BG1 = 0xFF09258C;
    private static final int COLOR_CIRCLE_BG2 = 0xFF091C72;
    private static final int COLOR_CIRCLE_BG3 = 0xFF062D9A;
    private static final int COLOR_ARC = 0xFF03C5EB;
    private int strokeWidth1;
    private int strokeWidth2;
    private int strokeWidth3;
    private RectF rect1;
    private RectF rect2;
    private RectF rect3;
    private Context context;
    private int minCircleBgRadiu = 0;
    private int distance = 0;
    private Paint paint;
    private RectF rect;
    private Path path;
    private Path segmentPath;
    private float mAnimatorValue = 0;
    private ValueAnimator animator1;
    private ValueAnimator animator2;
    private int flag = 0;
    private int width;
    private int height;
    private int radiu1;
    private int radiu2;
    private int radiu3;

    public SweepView(Context context) {
        super(context, null);
    }

    public SweepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SweepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

//        minCircleBgRadiu = dp2px(context, 70);
//        distance = dp2px(context, 26);

        rect = new RectF();
        path = new Path();
        segmentPath = new Path();

        strokeWidth1 = dp2px(context, 3);
        strokeWidth2 = dp2px(context, 2);
        strokeWidth3 = dp2px(context, 1);

        animator1 = ValueAnimator.ofFloat(0f, 1f).setDuration(700);
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animator2 != null) {
                    flag = 1;
                    animator2.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator2 = ValueAnimator.ofFloat(0f, 1f).setDuration(700);
        animator2.setInterpolator(new AccelerateDecelerateInterpolator());
        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public void startAnmation() {
        if (animator1 != null) {
            animator1.start();
        }
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEAULT_WIDTH, DEAULT_HEIGHT);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEAULT_WIDTH, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, DEAULT_HEIGHT);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        width = getWidth();
        height = getHeight();
        int maxRadiu = width > height ? height : width;

        radiu3 = maxRadiu / 2 - strokeWidth3;
        rect3 = new RectF(-radiu3, -radiu3, radiu3, radiu3);

        radiu2 = radiu3 - (int) getAdapterY(context, 52f) - strokeWidth2;
        rect2 = new RectF(-radiu2, -radiu2, radiu2, radiu2);

        radiu1 = radiu3 - (int) getAdapterY(context, 104f) - strokeWidth1;
        rect1 = new RectF(-radiu1, -radiu1, radiu1, radiu1);
    }

    public static float getAdapterY(Context context, float y) {
        int screenHeight = getScreenHeight(context);
        return y * screenHeight / 1080;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(getWidth() / 2, getHeight() / 2);
        if (flag == 1) {
            canvas.rotate(360 * mAnimatorValue);
        }

        paint.setColor(COLOR_CIRCLE_BG1);
        paint.setStrokeWidth(strokeWidth1);
        canvas.drawCircle(0, 0, radiu1, paint);
        if (flag == 1) {
            paint.setColor(COLOR_ARC);
            canvas.drawArc(rect1, 225, 90, false, paint);
        } else {
            drawArcAnimator(canvas, radiu1);
        }

        paint.setColor(COLOR_CIRCLE_BG2);
        paint.setStrokeWidth(strokeWidth2);
        canvas.drawCircle(0, 0, radiu2, paint);
        if (flag == 1) {
            paint.setColor(COLOR_ARC);
            canvas.drawArc(rect2, 225, 90, false, paint);
        } else {
            drawArcAnimator(canvas, radiu2);
        }

        paint.setColor(COLOR_CIRCLE_BG3);
        paint.setStrokeWidth(strokeWidth3);
        canvas.drawCircle(0, 0, radiu3, paint);
        if (flag == 1) {
            paint.setColor(COLOR_ARC);
            canvas.drawArc(rect3, 225, 90, false, paint);
        } else {
            drawArcAnimator(canvas, radiu3);
        }
    }

    private void drawArcAnimator(Canvas canvas, int radiu) {
        rect.set(-radiu, -radiu, radiu, radiu);
        path.reset();
        path.addArc(rect, 225, 90);

        PathMeasure pathMeasure = new PathMeasure();
        pathMeasure.setPath(path, false);
        float start = 0;
        float stop = pathMeasure.getLength() * mAnimatorValue;
        segmentPath.reset();
        pathMeasure.getSegment(start, stop, segmentPath, true);

        paint.setColor(COLOR_ARC);
        canvas.drawPath(segmentPath, paint);
    }
}
