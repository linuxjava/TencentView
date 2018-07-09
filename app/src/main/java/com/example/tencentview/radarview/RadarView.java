package com.example.tencentview.radarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robincxiao on 2018/7/9.
 */

public class RadarView extends View {
    private static final int STATE_STOP = 1;
    private static final int STATE_RUNNING = 2;
    private static final int STATE_FADE_OUT = 3;

    private static final int MSG_CYCLE_DRAW = 1;
    private static final int MSG_FADE_OUT_DRAW = 2;
    private static final int MSG_CYCLE = 3;
    private static final int MSG_FADE_OUT = 4;


    private static final int COLOR_100 = Color.WHITE;
    private static final int COLOR_0 = 0x00ffffff;
    private static final float SQRT3 = (float) Math.sqrt(3);
    private static final int HEXAGON_COUNT = 4;//六边形最大数量

    private static final int ROTATE_TIME = 1000;//六边形旋转周期
    public static final int TRANSLATE_TIME = 2000;//六边形向外移动周期
    private static final int FRAME_TIME = 40;//每帧动画停留时间
    private static final int FRAME_COUNT = TRANSLATE_TIME / FRAME_TIME;//一个周期内，六边形平移过程中绘制的帧数
    private static final int ROTATE_COUNT = ROTATE_TIME / FRAME_TIME;//一个周期内，六边形旋转过程中绘制的帧数

    private Paint mHexagonPaint;//六边形画笔
    private Path mPath;//六边形的path
    private List<Hexagon> mHexagons;//六边形Hexagon的集合
    private int MAX_HEXAGON_STROKE;//六边形最大线宽
    private DecelerateInterpolator mInterpolator;
    private boolean mPauseDecelerate;//是否停止mInterpolator减速动画，因为平移减速动画只在第一个周期内出现
    private SweepGradient mSweepGradient;
    private RectF mCanvasRect;
    private Matrix mMatrix;
    private int mCenterX, mCenterY;//View中心
    private int mRadiu, mStartRadiu, mAlphaRadiu;
    private int mHexagonMargin;//六边形之间的间距
    private int mMaxDistance;//六边形从mStartRadiu到mRadiu可移动的最大距离
    private int mSharderDegrees = 0;//mSweepGradient着色器旋转角度
    private float mRadio;
    private boolean isFix = false;//六边形是否向外移动
    private volatile int mState = STATE_STOP;//当前状态
    private int defaultSize;
    private IAnimListener listener;//动画监听

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CYCLE_DRAW: {
                    mHexagons.clear();
                    List<Hexagon> list = getHexagons((Integer) msg.obj);
                    if (list != null) {
                        mHexagons.addAll(list);
                    }

                    invalidate();
                    break;
                }
                case MSG_FADE_OUT_DRAW: {
                    mHexagons.clear();
                    List<Hexagon> list = getFadeOutHexagons((Integer) msg.obj);
                    if (list != null) {
                        mHexagons.addAll(list);
                    }

                    invalidate();
                    break;
                }
                case MSG_CYCLE: {
                    int frameCounter = msg.arg1;
                    if (frameCounter > FRAME_COUNT) {
                        frameCounter = 1;
                        mPauseDecelerate = true;
                    }

                    float f;
                    if (isFix) {
                        f = 15 / (float) FRAME_COUNT;
                    } else {
                        f = (float) frameCounter / (float) FRAME_COUNT;
                    }

                    //mRadio用于控制角度的变化，达到旋转效果
                    if (!mPauseDecelerate) {
                        mRadio = mInterpolator.getInterpolation(f);
                    }

                    //计算六边形绘制的半径
                    int r1 = (int) (mMaxDistance * f) + mStartRadiu;

                    //发送绘制六边形图形
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_CYCLE_DRAW, r1));

                    if (mState == STATE_RUNNING) {
                        //准备下一次绘制
                        Message message = mHandler.obtainMessage(MSG_CYCLE);
                        message.arg1 = frameCounter + 1;
                        mHandler.sendMessageDelayed(message, FRAME_TIME);
                    } else if (mState == STATE_FADE_OUT) {
                        Message message = mHandler.obtainMessage(MSG_FADE_OUT);
                        message.arg1 = 1;
                        mHandler.sendMessageDelayed(message, FRAME_TIME);
                    } else if (mState == STATE_STOP) {
                        if (listener != null) {
                            listener.onEnd();
                        }
                    }
                    break;
                }
                case MSG_FADE_OUT: {
                    int frameCounter = msg.arg1;
                    float f2 = (float) frameCounter / (float) FRAME_COUNT;
                    int r2 = (int) (mMaxDistance * f2) + mStartRadiu;

                    mRadio = mInterpolator.getInterpolation(1 - f2);

                    //发送绘制六边形图形
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_FADE_OUT_DRAW, r2));

                    if (frameCounter <= FRAME_COUNT) {
                        //准备下一次绘制
                        Message message = mHandler.obtainMessage(MSG_FADE_OUT);
                        message.arg1 = frameCounter + 1;
                        mHandler.sendMessageDelayed(message, FRAME_TIME);
                    } else {
                        mState = STATE_STOP;
                        if (listener != null) {
                            listener.onEnd();
                        }
                    }
                    break;
                }
            }

        }
    };

    public RadarView(Context context) {
        super(context);

        init(context);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        mHexagonPaint = new Paint();
        mHexagonPaint.setStrokeWidth(10);
        mHexagonPaint.setColor(Color.WHITE);
        mHexagonPaint.setStyle(Paint.Style.STROKE);
        mHexagonPaint.setAntiAlias(true);
        mSweepGradient = new SweepGradient(0, 0,
                new int[]{COLOR_0, COLOR_0, COLOR_100, COLOR_0, COLOR_0, COLOR_100, COLOR_0, COLOR_0, COLOR_100},
                new float[]{0f, 0.083f, 0.333f, 0.334f, 0.417f, 0.666f, 0.667f, 0.75f, 1f});
        mHexagonPaint.setShader(mSweepGradient);

        mPath = new Path();
        mHexagons = new ArrayList<>();
        MAX_HEXAGON_STROKE = dp2px(context, 6);
        mInterpolator = new DecelerateInterpolator(2f);
        mMatrix = new Matrix();
        defaultSize = dp2px(context, 200);
    }

    public void setListener(IAnimListener listener) {
        this.listener = listener;
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (mState != STATE_STOP) {
            return;
        }

        //初始化数据
        if (mHexagons != null) {
            mHexagons.clear();
        }
        mPauseDecelerate = false;


        mState = STATE_RUNNING;
        Message message = mHandler.obtainMessage(MSG_CYCLE);
        message.arg1 = 1;
        mHandler.sendMessage(message);

        if (listener != null) {
            listener.onStart();
        }
    }

    /**
     * 停止（带有淡出动画的停止过程）
     */
    public void fadeOutAnimation() {
        mState = STATE_FADE_OUT;
    }

    /**
     * 直接停止
     */
    public void stopAnimation() {
        mState = STATE_STOP;
    }

    public boolean isRunning() {
        return mState != STATE_STOP;
    }

    private int getAlpha(int radiu) {
        if (radiu < mStartRadiu) {//小于mStartRadiu全透明
            return 0;
        } else if (radiu < mAlphaRadiu) {
            return (radiu - mStartRadiu) * 255 / (mAlphaRadiu - mStartRadiu);
        } else if (radiu < mRadiu) {
            return (mRadiu - radiu) * 255 / (mRadiu - mAlphaRadiu);
        } else {//全透明
            return 0;
        }
    }

//    private List<Hexagon> getHexagons(int radiu) {
//        List<Hexagon> list = new ArrayList<>();
//        //每次只创建两个Hexagon
//        list.add(getHexagon(radiu));
//        if (radiu + mHexagonMargin < mRadiu) {
//            list.add(getHexagon(radiu + mHexagonMargin));
//        } else {
//            list.add(getHexagon(radiu - mHexagonMargin));
//        }
//        return list;
//    }

    private List<Hexagon> getHexagons(int radiu) {
        List<Hexagon> list = new ArrayList<Hexagon>();

        //此处需要主要radiu + mHexagonMargin < mRadiu条件的判断，当条件满足时最外面的六边形快消失了，类似水波纹一样，最外面的一层消失后，最里面
        //需要新产生一个播放，因此需要有list.add(getHexagon(radiu - mHexagonMargin));
        list.add(getHexagon(radiu));

        int newRadiu = radiu;
        while (true) {
            newRadiu += mHexagonMargin;
            if (newRadiu < mRadiu) {
                list.add(getHexagon(newRadiu));
            } else {
                break;
            }
        }

        newRadiu = radiu;
        while (true) {
            newRadiu -= mHexagonMargin;
            if (newRadiu > 0) {
                list.add(getHexagon(newRadiu));
            } else {
                break;
            }
        }

        return list;
    }

    private List<Hexagon> getFadeOutHexagons(int radiu) {
        List<Hexagon> list = new ArrayList<>();
        while (radiu < mRadiu) {
            list.add(getHexagon(radiu));
            radiu = radiu + mHexagonMargin;
        }
        return list;
    }

    private Hexagon getHexagon(int radiu) {
        Hexagon hexagon = new Hexagon();
        float f = (float) (radiu - mStartRadiu) / (float) (mRadiu - mStartRadiu);
        if (isFix) {
            hexagon.width = MAX_HEXAGON_STROKE * 2 / 3;
        } else {
            hexagon.width = MAX_HEXAGON_STROKE - (int) (MAX_HEXAGON_STROKE * f);
        }

        Path path = new Path();
        float dx = SQRT3 * radiu / 2;
        path.moveTo(0, radiu);
        path.lineTo(0 - dx, radiu / 2);
        path.lineTo(0 - dx, 0 - radiu / 2);
        path.lineTo(0, 0 - radiu);
        path.lineTo(dx, 0 - radiu / 2);
        path.lineTo(dx, radiu / 2);
        path.close();

        hexagon.path = path;
        hexagon.radiu = radiu;
        return hexagon;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultSize, defaultSize);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize);
        }
    }

    @Override
    public void onLayout(boolean c, int l, int t, int r, int b) {
        super.onLayout(c, l, t, r, b);

        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;

        mRadiu = mCenterX > mCenterY ? mCenterY : mCenterX;
        mStartRadiu = mRadiu / 3;
        mAlphaRadiu = mStartRadiu + (mRadiu - mStartRadiu) / 7;
        mHexagonMargin = (mRadiu - mStartRadiu) / HEXAGON_COUNT;
        mCanvasRect = new RectF(0, 0, getWidth(), getHeight());
        mMaxDistance = mRadiu - mStartRadiu;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(mCenterX, mCenterY);
        for (int i = 0; i < mHexagons.size(); i++) {
            mMatrix.setRotate(mSharderDegrees + i * 30);
            mSweepGradient.setLocalMatrix(mMatrix);
            Hexagon h = mHexagons.get(i);
            mHexagonPaint.setStrokeWidth(h.width);
            mHexagonPaint.setAlpha(getAlpha(h.radiu));
            canvas.drawPath(h.path, mHexagonPaint);
        }

        /**
         * mSharderDegrees通过角度的变化实现旋转效果，角度的变化过程是：
         * 在第一次波纹动画时，mRadio会逐渐变大，因此旋转速度逐渐变快；当mRadio=1后，就不在改变了，旋转速度就固定了
         */
        mSharderDegrees = mSharderDegrees + (int) (360 / ROTATE_COUNT * mRadio);
        if (mSharderDegrees >= 360) {
            mSharderDegrees = mSharderDegrees - 360;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }

    public interface IAnimListener {
        void onStart();

        void onEnd();
    }

    class Hexagon {
        Path path;
        int width;
        int radiu;//六边形半径
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}
