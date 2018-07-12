package com.example.tencentview.wifigrid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.tencentview.R;

import java.util.List;

/**
 * Created by robincxiao on 2017/7/10.
 */

public class WifiGridView extends View {
    private static final float[] BAND_WIDTH = {0, 1F, 2F, 5F, 10F, 100F};//带宽
    private static final int COLOR_SIGNAL_LINE = 0xFF00FEFF;//最高点颜色
    private static final int COLOR_COORDINATE_LINE = 0xFF2D2D6B;//坐标线颜色
    private static final int COLOR_COORDINATE_TEXT = 0xFF8B85B4;//坐标文字颜色
    private static final int COLOR_SPEED_TEXT = Color.WHITE;//文字颜色
    private Context context;
    private int width;
    private int height;
    private int xLines = 8;
    private int yLines = 6;
    private float xGridWidth;
    private float yGridWidth;
    private int DEFAULT_WIDTH = 300;
    private int DEFAULT_HEIGHT = 200;
    private static final float DEFAULT_XY_LINE_WIDTH = 0.3f;//xy坐标线宽(dp)
    private Paint coordinateLinePaint;
    private Paint whitePointPaint;
    private Paint linePaint;
    private Paint gradientPaint;
    private Paint maxValuePaint;//最高点画笔
    private Paint speedTextPaint;
    private Paint coordinateTextPaint;
    private Path pointPath;
    private Path gradientPath;
    private Bitmap leftBubbleBitmap;
    private Bitmap middleBubbleBitmap;
    private Bitmap rightBubbleBitmap;
    private Matrix matrix;
    private float xyLineWidth;
    private float[] gridData;
    private List<Float> speedData;

    public WifiGridView(Context context) {
        this(context, null);
    }

    public WifiGridView(Context context, @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WifiGridView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public void setData(final List<Float> data) {
        if (data == null || data.size() == 0) {
            return;
        }

        speedData = data;
        gridData = new float[Math.min(data.size(), xLines)];

        for (int i = 0; i < data.size(); i++) {
            float speed = data.get(i);

            if (speed <= BAND_WIDTH[0]) {
                gridData[i] = yLines - 1;
            } else if (speed >= BAND_WIDTH[BAND_WIDTH.length - 1]) {
                gridData[i] = 0;
            } else {
                for (int j = 1; j < BAND_WIDTH.length; j++) {
                    if (speed <= BAND_WIDTH[j]) {
                        gridData[i] = (yLines - 1) - ((j - 1)  + (speed - BAND_WIDTH[j - 1]) / (BAND_WIDTH[j] - BAND_WIDTH[j - 1]));
                        break;
                    }
                }
            }
        }

        invalidate();
    }

    private void init(Context context) {
        this.context = context;

        xyLineWidth = dp2px(context, DEFAULT_XY_LINE_WIDTH);

        coordinateLinePaint = new Paint();
        coordinateLinePaint.setAntiAlias(true);
        coordinateLinePaint.setStyle(Paint.Style.STROKE);
        coordinateLinePaint.setStrokeWidth(xyLineWidth);
        coordinateLinePaint.setColor(COLOR_COORDINATE_LINE);

        whitePointPaint = new Paint();
        whitePointPaint.setAntiAlias(true);
        whitePointPaint.setStyle(Paint.Style.FILL);
        whitePointPaint.setStrokeWidth(xyLineWidth * 4f);
        whitePointPaint.setColor(Color.WHITE);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(xyLineWidth * 7);
        linePaint.setColor(COLOR_SIGNAL_LINE);

        gradientPaint = new Paint();
        gradientPaint.setAntiAlias(true);
        gradientPaint.setStyle(Paint.Style.FILL);

        maxValuePaint = new Paint();
        maxValuePaint.setAntiAlias(true);
        maxValuePaint.setStyle(Paint.Style.FILL);
        maxValuePaint.setColor(COLOR_SIGNAL_LINE);

        float textSize = dp2px(context, 13);

        speedTextPaint = new Paint();
        speedTextPaint.setAntiAlias(true);
        speedTextPaint.setColor(COLOR_SPEED_TEXT);
        speedTextPaint.setTextSize(textSize);

        coordinateTextPaint = new Paint();
        coordinateTextPaint.setAntiAlias(true);
        coordinateTextPaint.setColor(COLOR_COORDINATE_TEXT);
        coordinateTextPaint.setTextSize(textSize);

        pointPath = new Path();
        gradientPath = new Path();

        matrix = new Matrix();
        leftBubbleBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_bubble_left);
        middleBubbleBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_bubble_middle);
        rightBubbleBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_bubble_right);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthModeSpec = MeasureSpec.getMode(widthMeasureSpec);
        int widthSizeSpec = MeasureSpec.getSize(widthMeasureSpec);
        int heightModeSpec = MeasureSpec.getMode(heightMeasureSpec);
        int heightSizeSpec = MeasureSpec.getSize(heightMeasureSpec);

        if (widthModeSpec == MeasureSpec.AT_MOST && heightModeSpec == MeasureSpec.AT_MOST) {
            widthSizeSpec = (int) dp2px(context, DEFAULT_WIDTH);
            heightSizeSpec = (int) dp2px(context, DEFAULT_HEIGHT);
        } else if (widthModeSpec == MeasureSpec.AT_MOST) {
            widthSizeSpec = (int) dp2px(context, DEFAULT_WIDTH);
        } else if (heightModeSpec == MeasureSpec.AT_MOST) {
            heightSizeSpec = (int) dp2px(context, DEFAULT_HEIGHT);
        }

        setMeasuredDimension(widthSizeSpec, heightSizeSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        width = getWidth();
        height = getHeight();

        xGridWidth = (width - getPaddingLeft() - getPaddingRight()) * 1.0f / (xLines - 1);
        yGridWidth = (height - getPaddingTop() - getPaddingBottom()) * 1.0f / (yLines - 1);

        int x0 = (width - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft(), y0 = 0;
        int x1 = (width - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
        int y1 = height - getPaddingTop();

//        LinearGradient linearGradient = new LinearGradient(x0, y0, x1, y1, 0x7F00fdff
//                , 0x0500fdff, Shader.TileMode.CLAMP);

        LinearGradient linearGradient = new LinearGradient(0, 0, x1, y1, 0x7F00fdff
                , 0x0500fdff, Shader.TileMode.CLAMP);
        gradientPaint.setShader(linearGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawXCoordinateLine(canvas);
        drawYCoordinateLine(canvas);
        drawData(canvas);


    }

    private void drawXCoordinateLine(Canvas canvas) {
        for (int i = 0; i < xLines; i++) {
            float x = xGridWidth * i + getPaddingLeft();
            canvas.drawLine(x, getPaddingTop(), x, height - getPaddingBottom(), coordinateLinePaint);

            for (int j = 0; j < yLines; j++) {
                canvas.drawPoint(x, yGridWidth * j + getPaddingTop(), whitePointPaint);
            }
        }
    }

    private void drawYCoordinateLine(Canvas canvas) {
        for (int i = 0; i < yLines; i++) {
            float y = yGridWidth * i + getPaddingTop();
            canvas.drawLine(getPaddingLeft(), y, width - getPaddingRight(), y, coordinateLinePaint);
        }

        int strWidth = getStringWidth(coordinateTextPaint, "蓝光");
        float x = (getPaddingLeft() - strWidth) / 2;
        canvas.drawText("蓝光", x, getPaddingTop() + yGridWidth, coordinateTextPaint);
        canvas.drawText("超清", x, getPaddingTop() + yGridWidth * 2, coordinateTextPaint);
        canvas.drawText("高清", x, getPaddingTop() + yGridWidth * 3, coordinateTextPaint);
        canvas.drawText("基线", x, getPaddingTop() + yGridWidth * 4, coordinateTextPaint);
    }

    private void drawData(Canvas canvas) {
        if (gridData == null) {
            return;
        }

        int maxIndex = 0;//最高点索引
        float maxValue = gridData[0];
        pointPath.moveTo(getPaddingLeft(), getPaddingTop() + gridData[0] * yGridWidth);
        for (int i = 1; i < gridData.length; i++) {
            //创建曲线路径
            pointPath.lineTo(xGridWidth * i + getPaddingLeft(), gridData[i] * yGridWidth + getPaddingTop());
            if (gridData[i] <= maxValue) {
                maxValue = gridData[i];
                maxIndex = i;
            }
        }

        //创建闭环渐变区域
        gradientPath.addPath(pointPath);
        gradientPath.lineTo(width - getPaddingRight(), height - getPaddingBottom());
        gradientPath.lineTo(getPaddingLeft(), height - getPaddingBottom());
        gradientPath.close();

        //画最高速率点
        float maxValueX = xGridWidth * maxIndex + getPaddingLeft();//最高点x坐标
        float maxValueY = gridData[maxIndex] * yGridWidth + getPaddingTop();//最高点y坐标

        //绘制渐变效果
        int x1 = width - getPaddingRight();
        int y1 = height - getPaddingBottom();
        LinearGradient linearGradient = new LinearGradient(maxValueX, maxValueY, maxValueX, height - getPaddingBottom(), 0x7F00fdff
                , 0x0500fdff, Shader.TileMode.CLAMP);
        gradientPaint.setShader(linearGradient);
        canvas.drawPath(gradientPath, gradientPaint);
        //绘制曲线
        canvas.drawPath(pointPath, linePaint);

        canvas.drawCircle(maxValueX, maxValueY, dp2px(context, 3), maxValuePaint);
        float radialRadiu = dp2px(context, 30);
        maxValuePaint.setShader(new RadialGradient(maxValueX, maxValueY, radialRadiu,
                new int[]{0x0000fdff, 0x4200fdff, 0x0000fdff}, new float[]{0, 0.1f, 1}, Shader.TileMode.CLAMP));
        canvas.drawCircle(maxValueX, maxValueY, radialRadiu, maxValuePaint);

        //绘制最高速率点速率
        String speed = speedData.get(maxIndex) + "MB/S";
        int strWidth = getStringWidth(speedTextPaint, speed);
        int strHeight = getStringHeight(speedTextPaint);

        int extraWidth = (int) dp2px(context, 20);
        int extraHeight = (int) dp2px(context, 16);
        int targetBubbleWidth = strWidth + extraWidth;
        int targetBubbleHeight = strHeight + extraHeight;
        float bubbleWidthScaleRatio, bubbleHeightScaleRatio;

        //计算bitmap的位置
        float newX = maxValueX;
        float newY = maxValueY - targetBubbleHeight - dp2px(context, 11);
        //计算文本位置
        float textX = extraWidth / 2;
        float textY = strHeight + dp2px(context, 5);

        canvas.save();
        if (maxIndex == 0) {
            bubbleWidthScaleRatio = targetBubbleWidth * 1.0f / leftBubbleBitmap.getWidth();
            bubbleHeightScaleRatio = targetBubbleHeight * 1.0f / leftBubbleBitmap.getHeight();
            matrix.setScale(bubbleWidthScaleRatio, bubbleHeightScaleRatio);
            canvas.translate(newX, newY);
            canvas.drawBitmap(leftBubbleBitmap, matrix, null);
            canvas.drawText(speed, textX, textY, speedTextPaint);
        } else if (maxIndex == gridData.length - 1) {
            bubbleWidthScaleRatio = targetBubbleWidth * 1.0f / rightBubbleBitmap.getWidth();
            bubbleHeightScaleRatio = targetBubbleHeight * 1.0f / rightBubbleBitmap.getHeight();
            matrix.setScale(bubbleWidthScaleRatio, bubbleHeightScaleRatio);
            newX = maxValueX - targetBubbleWidth;
            canvas.translate(newX, newY);
            canvas.drawBitmap(rightBubbleBitmap, matrix, null);
        } else {
            bubbleWidthScaleRatio = targetBubbleWidth * 1.0f / middleBubbleBitmap.getWidth();
            bubbleHeightScaleRatio = targetBubbleHeight * 1.0f / middleBubbleBitmap.getHeight();
            matrix.setScale(bubbleWidthScaleRatio, bubbleHeightScaleRatio);
            newX = maxValueX - targetBubbleWidth / 2;
            canvas.translate(newX, newY);
            canvas.drawBitmap(middleBubbleBitmap, matrix, null);
        }
        canvas.drawText(speed, textX, textY, speedTextPaint);
        canvas.restore();
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return
     */
    public static float dp2px(Context context, float dpVal) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @param context
     * @param spVal
     * @return
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
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
}
