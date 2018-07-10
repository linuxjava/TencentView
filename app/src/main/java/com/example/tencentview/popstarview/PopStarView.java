package com.example.tencentview.popstarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by robincxiao on 2018/7/10.
 */

public class PopStarView extends View {
    private static final float SQRT3 = (float) Math.sqrt(3);
    //每一帧持续的时间(ms)
    private static final int FRAME_TIME = 60;
    //整个动画总的帧数
    private static final int FRAME_COUNT = 22;
    private Context mContext;
    private Paint mPaint;
    private int mCenterX, mCenterY;//PopStarView中心坐标
    private int mRadiu;//PopStarView的半径
    /**
     * 绘图的方式是通过两个圆裁剪出一个圆环，in代表内圆半径，out代表外圆半径，当内圆半径和外圆半径发生变化时，就能绘制出我们想要的效果;
     * 12个角分为两组，所以会有inClipRadiu1、inClipRadiu2
     */
    //in代表内圆半径，out代表外圆半径
    private int inClipRadiu1, outClipRadiu1;
    private int inClipRadiu2, outClipRadiu2;
    //内圆起始半径
    private int inStartRadiu;
    //内圆向外移动的最大值
    private int inMaxDistance;
    //外圆起始半径
    private int outStartRadiu;
    //外圆向外移动的最大值
    private int outMaxDistance;
    private AccelerateDecelerateInterpolator mInterpolator;
    private int mInterpolatorCount;
    private IAnimListener listener;//动画监听
    private Path starPath, circlePath;

    private boolean isRunning;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int frameCounter = msg.what;
            float radio = (float) frameCounter / (float) mInterpolatorCount;
            if (radio > 1)
                radio = 1;

            radio = mInterpolator.getInterpolation(radio);
            inClipRadiu1 = inStartRadiu + (int) (inMaxDistance * radio);
            outClipRadiu1 = outStartRadiu + (int) (outMaxDistance * radio);

            //开始第二组动画
            if (frameCounter >= 3) {
                radio = (float) (frameCounter - 3) / (float) mInterpolatorCount;
                if (radio > 1)
                    radio = 1;

                radio = mInterpolator.getInterpolation(radio);
                //动画过程是通过系数改变inClipRadiu2和outClipRadiu2的值
                inClipRadiu2 = inStartRadiu + (int) (inMaxDistance * radio);
                outClipRadiu2 = outStartRadiu + (int) (outMaxDistance * radio);
            }

            invalidate();

            if (frameCounter < FRAME_COUNT) {
                handler.sendEmptyMessageDelayed(frameCounter + 1, FRAME_TIME);
            } else {
                isRunning = false;
                if (listener != null) {
                    listener.onFinish();
                }
            }
        }
    };

    public PopStarView(Context context) {
        super(context);

        init(context);
    }

    public PopStarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public PopStarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mInterpolator = new AccelerateDecelerateInterpolator();
        mInterpolatorCount = FRAME_COUNT - 4;

        starPath = new Path();
        circlePath = new Path();

        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(0);

        /**
         * canvas.clipPath*()裁剪方法可能会因为硬件加速默认开启而导致失效，
         * 因此需要在某些版本上关闭view的硬件加速效果
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void setListener(IAnimListener listener) {
        this.listener = listener;
    }

    public void startAnimation() {
        if (!isRunning()) {
            isRunning = true;
            handler.sendEmptyMessage(0);
            if (listener != null) {
                listener.onStart();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;

        mRadiu = (mCenterX > mCenterY ? mCenterY : mCenterX);
        //创建六角形path
        createStarPath(dp2px(mContext, 6), mRadiu);

        inStartRadiu = mRadiu / 2;
        outStartRadiu = mRadiu * 3 / 4;

        inMaxDistance = mRadiu - inStartRadiu;
        outMaxDistance = mRadiu - outStartRadiu;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(mCenterX, mCenterY);

        //绘制第一组
        if (inClipRadiu1 > 0 && inClipRadiu1 <= mRadiu) {
            drawPopStar(canvas, inClipRadiu1, outClipRadiu1);
        }
        //绘制第二组
        if (inClipRadiu2 >= inStartRadiu && inClipRadiu2 <= mRadiu) {
            canvas.rotate(30);
            drawPopStar(canvas, inClipRadiu2, outClipRadiu2);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
        listener = null;
    }

    private void drawPopStar(Canvas canvas, int inClipRadiu, int outClipRadiu) {
        /**
         * Clip(剪切)的时机：通常理解的clip(剪切)，是对已经存在的图形进行clip的。但是，在android上是对canvas（画布）上进行clip的，
         * 要在画图之前对canvas进行clip，如果画图之后再对canvas进行clip不会影响到已经画好的图形。一定要记住clip是针对canvas而非图形
         */
        circlePath.rewind();
        circlePath.addCircle(0, 0, inClipRadiu, Path.Direction.CW);
        //Region.Op.DIFFERENCE表示，画布除去circlePath的区域后，剩下的为有效区域
        canvas.clipPath(circlePath, Region.Op.DIFFERENCE);

        circlePath.rewind();
        circlePath.addCircle(0, 0, outClipRadiu, Path.Direction.CW);
        //Region.Op.INTERSECT表示获取交集
        canvas.clipPath(circlePath, Region.Op.INTERSECT);

        //绘制六角形
        canvas.drawPath(starPath, mPaint);
    }

    /**
     * 创建六角形12个点的path
     *
     * @param inRadiu  内圆半径
     * @param outRadiu 外圆半径
     */
    private void createStarPath(int inRadiu, int outRadiu) {
        starPath.rewind();

        float dx_s = SQRT3 * inRadiu / 2;
        float dx_l = SQRT3 * outRadiu / 2;
        starPath.moveTo(0, outRadiu);
        starPath.lineTo(0 - inRadiu / 2, dx_s);

        starPath.lineTo(0 - dx_l, outRadiu / 2);
        starPath.lineTo(0 - inRadiu, 0);

        starPath.lineTo(0 - dx_l, 0 - outRadiu / 2);
        starPath.lineTo(0 - inRadiu / 2, 0 - dx_s);

        starPath.lineTo(0, 0 - outRadiu);
        starPath.lineTo(inRadiu / 2, 0 - dx_s);

        starPath.lineTo(dx_l, 0 - outRadiu / 2);
        starPath.lineTo(inRadiu, 0);

        starPath.lineTo(dx_l, outRadiu / 2);
        starPath.lineTo(inRadiu / 2, dx_s);

        starPath.close();
    }

    private static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public interface IAnimListener {
        void onStart();

        void onFinish();
    }
}
