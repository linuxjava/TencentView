package com.example.tencentview.scoreview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.util.Locale;

public class ScoreView extends View {
    public static final int SCROLL_DOWN = 1;//数字向下滚动
    public static final int SCROLL_UP = 2;//数字向上滚动
    private static final int SHADOW_COLOR = 0x55000000;//阴影颜色
    private static final int MASK_COLOR = 0x33000000;

    private Context mContext;
    //数字滚动一次的距离
    private int SCROLL_DISTANCE;
    //滚动方向
    private int scrollDirection = SCROLL_UP;

    private int mTextWidth;//两个字符的宽度
    private int mTextHeight;
    private int mInitY;//数字最终绘制的Y坐标

    private Paint mTextPaint;//文字Paint
    private Paint mMuskPaint;//上下遮罩层Paint
    private LinearGradient mTopShader;//顶部遮罩层着色器
    private LinearGradient mBottomShader;//顶部遮罩层着色器
    private float mTextSize;//数字的大小

    private boolean isAnim;//动画是否正在进行

    private Number mTenNumber;//十位
    private Number mBitNumber;//个位
    private IAnimListener listener;//动画监听

    public ScoreView(Context context) {
        this(context, null);
    }

    public ScoreView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    public void setListener(IAnimListener listener) {
        this.listener = listener;
    }

    public void setScrollDirection(int scrollDirection) {
        this.scrollDirection = scrollDirection;
    }

    private void init(Context context, AttributeSet attrs) {
        if (mTextSize == 0) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            switch (displayMetrics.densityDpi) {
                case 480:
                    mTextSize = 258;
                    break;
                case 320:
                    mTextSize = 172;
                    break;
                case 240:
                    mTextSize = 130;
                    break;
                case 160:
                    mTextSize = 86;
                    break;
                case 120:
                    mTextSize = 64;
                    break;
                default:
                    mTextSize = 130;
                    break;
            }
        }

        mContext = context;
        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);//设置mPaint抗锯齿
        mTextPaint.setShadowLayer(20, 10, 10, SHADOW_COLOR);

        mTextWidth = getStringWidth(mTextPaint, "11");//两个字符的宽度
        mTextHeight = getStringHeight(mTextPaint);

        mMuskPaint = new Paint();

        SCROLL_DISTANCE = dp2px(mContext, 6);

        mTenNumber = new Number();
        mBitNumber = new Number();
    }

    /**
     * 开始滚动
     */
    public void start() {
        if (isAnim) {
            return;
        }

        mTenNumber.start();
        mBitNumber.start();

        isAnim = true;
        if (listener != null) {
            listener.onScrollStart();
        }
        invalidate();
    }

    /**
     * 设置结果
     *
     * @param score
     * @param hasOverAnim true：动画过程滚动到结果数字；false：无动画过程，直接到结果数字
     */
    public void setScore(int score, boolean hasOverAnim) {
        if(!isAnim){
            return;
        }

        if (score < 0) {
            score = 0;
        }

        if (score >= 100) {
            score = 99;
        }

        String targetScore = String.format(Locale.getDefault(), "%02d", score);
        int tenNumber = Integer.parseInt(targetScore.substring(0, 1));//十位数字
        int bitNumber = Integer.parseInt(targetScore.substring(1, 2));//个位数字

        mTenNumber.setScore(tenNumber, hasOverAnim);
        mBitNumber.setScore(bitNumber, hasOverAnim);

        if (hasOverAnim) {
            invalidate();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        switch (heightSpecMode) {
            case MeasureSpec.AT_MOST:
                setMeasuredDimension(mTextWidth, mTextHeight);
                break;
            case MeasureSpec.EXACTLY:
                int size = heightSpecSize > mTextHeight ? heightSpecSize : mTextHeight;
                setMeasuredDimension(mTextWidth, size);
                break;
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int width = getWidth();
        int height = getHeight();
        //计算顶部和底部遮罩层位置
        mTopShader = new LinearGradient(width / 2, 0, width / 2, height / 8, MASK_COLOR, 0x0, Shader.TileMode.CLAMP);
        mBottomShader = new LinearGradient(width / 2, height * 7 / 8, width / 2, height, 0x0, MASK_COLOR, Shader.TileMode.CLAMP);
        //计算保证初始绘制字符在中间
        mInitY = (height - mTextHeight) / 2 + mTextHeight;//数字初始绘制的Y坐标
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制背景，测试使用
        canvas.drawColor(0xffff0000);
        //绘制滚动数字
        mTenNumber.drawNumber(canvas, 0, SCROLL_DISTANCE * 2);
        mBitNumber.drawNumber(canvas, mTextWidth / 2, SCROLL_DISTANCE * 3);
        //绘制顶部遮罩层
        mMuskPaint.setShader(mTopShader);
        canvas.drawRect(0, 0, getWidth(), getHeight() / 8, mMuskPaint);
        //绘制底部遮罩层
        mMuskPaint.setShader(mBottomShader);
        canvas.drawRect(0, getHeight() * 7 / 8, getWidth(), getHeight(), mMuskPaint);

        if (mTenNumber.isAnim || mBitNumber.isAnim) {
            invalidate();
        } else {
            //这个地方需要添加isAnim的判断，否则View刚显示时会回调onScrollEnd方法
            if (listener != null && isAnim) {
                listener.onScrollEnd();
            }
            isAnim = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //销毁资源
        listener = null;
    }

    public interface IAnimListener {
        void onScrollStart();

        void onScrollEnd();
    }

    private class Number {
        int currentNum = 9;
        int currentNextNum;
        int targetNum;
        int currentY;
        boolean isAnim;

        Number() {
            currentNum = 0;
            targetNum = 0;
        }

        /**
         * 启动
         */
        void start() {
            if (!isAnim) {
                targetNum = -1;//设置targetNum为-1是进行无限滚动的关键
                //如果当前动画没有开始，且currentNum != targetNum，则设置标志位isAnim=true
                isAnim = (currentNum != targetNum);
            }
        }

        void setScore(int score, boolean hasAnim) {
            if (hasAnim) {
                targetNum = score;
            } else {
                currentNum = targetNum = score;
                currentY = mInitY;
            }
        }

        void drawNumber(Canvas canvas, int positionX, int deltaY) {
            if (isAnim) {
                if (scrollDirection == SCROLL_DOWN) {//向下滚动，数字是递减的
                    currentY = currentY + deltaY;
                    if (currentY > (mInitY + mTextHeight)) {//数字向下移动超过一个字符高度
                        currentY = currentY - mTextHeight;
                        currentNum = currentNum - 1;
                        if (currentNum < 0) {
                            currentNum = 9;
                        }
                    }
                } else {//向上滚动，数字是递增的
                    currentY = currentY - deltaY;
                    if (currentY < (mInitY - mTextHeight)) {//数字向上移动超过一个字符高度
                        currentY = currentY + mTextHeight;
                        currentNum = currentNum + 1;
                        if (currentNum > 9) {
                            currentNum = 0;
                        }
                    }
                }

                /**
                 * 1.无限滚动时endNum==-1，所以currentNum == endNum永远不会满足；
                 * 2.在设置了目标endNum后，为什么需要Math.abs(currentY - mInitY) <= deltaY)检测条件？？
                 */
                if ((Math.abs(currentY - mInitY) <= deltaY) && (currentNum == targetNum)) {
                    /**
                     * Math.abs(currentY - mInitY) <= deltaY)表示一个字符首次开始绘制
                     * 滚动结束条件为：当前绘制的字符为目标字符并且该字母为首次绘制
                     * 为什么需要加入“字符首次绘制”这个条件呢？
                     * 当currentNum == targetNum，表示当前绘制的是目标字符，但是这个字符有可能已经滚动到离mInitY初始位置很远，如果此时
                     * 没有Math.abs(currentY - mInitY) <= deltaY)的判断，那么现实目标字符的动画将会有一个突兀的过程，通过
                     * Math.abs(currentY - mInitY) <= deltaY)控制，带下一次“开始绘制目标字符并且该字母为首次绘制”时，因为currentY
                     * 距离mSourceY非常小，对用户来说没有感觉。
                     */
                    currentY = mInitY;
                    canvas.drawText(String.valueOf(currentNum), positionX, currentY, mTextPaint);
                    isAnim = false;
                } else {
                    //绘制第一个数字
                    canvas.drawText(String.valueOf(currentNum), positionX, currentY, mTextPaint);
                    //绘制第二个数字
                    if (scrollDirection == SCROLL_DOWN) {
                        currentNextNum = currentNum - 1;
                        if (currentNextNum < 0) {
                            currentNextNum = 9;
                        }
                    } else {
                        currentNextNum = currentNum + 1;
                        if (currentNextNum > 9) {
                            currentNextNum = 0;
                        }
                    }
                    canvas.drawText(String.valueOf(currentNextNum), positionX, (scrollDirection == SCROLL_DOWN) ? currentY - mTextHeight : currentY + mTextHeight, mTextPaint);
                }
            } else {
                currentY = mInitY;
                canvas.drawText(String.valueOf(currentNum), positionX, currentY, mTextPaint);
            }
        }
    }

    /**
     * 获取字符串宽度
     *
     * @param paint 画笔对象
     * @param str   字符串
     * @return 字符串宽度
     */
    private int getStringWidth(Paint paint, String str) {
        return (int) paint.measureText(str);
    }

    /**
     * 获取字符串高度
     *
     * @param paint 画笔对象
     * @return 字符串高度
     */
    private int getStringHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) (Math.abs(fm.ascent));
    }

    private static int dp2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }
}
