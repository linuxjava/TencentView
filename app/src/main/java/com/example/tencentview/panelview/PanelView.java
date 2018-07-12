package com.example.tencentview.panelview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.example.tencentview.R;

/**
 * Created by robincxiao on 2017/7/12.
 */

public class PanelView extends View {
    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 100;
    private static final float[] KE_DU = {0, 0.5F, 1.0F, 2.5F, 4F, 7F, 10F, 55F, 100F};
    private static final int COLOR_EXTERNAL_ARC = 0xFF01B7DA;
    private static final int COLOR_ARC_SHADOW = 0xCC01B7DA;
    private static final int COLOR_INTERNAL_ARC = 0xFF00F2FF;//内部弧线颜色
    private static final float RATIO = 0.169f;//仪表盘透明区域的padding占整个仪表盘宽度的比率
    private static final float INTERNAL_CIRCLE_RATIO = 0.405f;//内部圆占整个仪表盘宽度的比率

    private static final float START_ANGLE = -210;//指针起始角度
    private static final float SWEEP_MAX_ANGLE = 240;//指针最大sweep角度
    private static final float PANEL_POINTER_SUPPLEMENT_ANGLE = 1.3f;//指针补充角度
    private static final int MAX_FRAME_NUM = 25;//一次动画中，所需刷新的帧数
    private static final float DEFAULT_MAX_STROKE_WIDTH = 2.0f;//默认最大最大线宽(dp)
    private static final float CICLE_POINT_RADIU = 4.0f;//指针上圆点半径(dp)
    private Context context;
    private Paint externalPaint;//外部弧线画笔
    private Paint pointPaint;//指针上发光点画笔
    private Paint internalArcPaint;//内部弧线画笔
    private int width;
    private int height;
    private RectF externalRectF;//外部圆弧矩形
    private RectF internalRectF;//内部圆弧矩形
    private float currentSweepAngle = 0;//当前指针sweep的角度(0<=currentSweepAngle<=240)
    private int frameCounter = 1;//一次动画中，当前刷新的帧计数
    private int flag = 0;
    private Bitmap panelBitmap;
    private Bitmap panelPointerBitmap;
    private Matrix matrix;
    private float panelTransparencyPadding;//一边盘外边缘透明区域大小
    private boolean isStop;
    private float bitmapWidthRatio;
    private float bitmapHeightRatio;
    private AccelerateInterpolator accelerateInterpolator;
    private int defalutMaxStrokeWidth;//默认最大线宽
    private float ciclePointRadiu;//指针上圆点半径
    private float changeAngle;

    public PanelView(Context context) {
        this(context, null);
    }

    public PanelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        this.context = context;

        defalutMaxStrokeWidth = dp2px(context, DEFAULT_MAX_STROKE_WIDTH);
        ciclePointRadiu = dp2px(context, CICLE_POINT_RADIU);

        externalPaint = new Paint();
        externalPaint.setAntiAlias(true);
        externalPaint.setStyle(Paint.Style.STROKE);
        externalPaint.setStrokeCap(Paint.Cap.ROUND);
        externalPaint.setColor(COLOR_EXTERNAL_ARC);

        pointPaint = new Paint(externalPaint);
        pointPaint.setStyle(Paint.Style.FILL);

        internalArcPaint = new Paint(externalPaint);
        internalArcPaint.setStrokeWidth(defalutMaxStrokeWidth);
        externalPaint.setStrokeCap(Paint.Cap.ROUND);
        internalArcPaint.setColor(COLOR_INTERNAL_ARC);

        matrix = new Matrix();
        panelBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_instrument_panel);
        panelPointerBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_instrument_panel_pointer);

        accelerateInterpolator = new AccelerateInterpolator(2f);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setStop() {
        isStop = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthModeSpec = MeasureSpec.getMode(widthMeasureSpec);
        int widthSizeSpec = MeasureSpec.getSize(widthMeasureSpec);
        int heightModeSpec = MeasureSpec.getMode(heightMeasureSpec);
        int heightSizeSpec = MeasureSpec.getSize(heightMeasureSpec);

        if (widthModeSpec == MeasureSpec.AT_MOST && heightModeSpec == MeasureSpec.AT_MOST) {
            widthSizeSpec = dp2px(context, DEFAULT_WIDTH);
            heightSizeSpec = dp2px(context, DEFAULT_HEIGHT);
        } else if (widthModeSpec == MeasureSpec.AT_MOST) {
            widthSizeSpec = dp2px(context, DEFAULT_WIDTH);
        } else if (heightModeSpec == MeasureSpec.AT_MOST) {
            heightSizeSpec = dp2px(context, DEFAULT_HEIGHT);
        }

        setMeasuredDimension(widthSizeSpec, heightSizeSpec);
    }



    /**
     *
     * @param speed 当前网速(M/S)
     */
    public void setSpeed(float speed){
        if(speed <= 0){
            return;
        }else if(speed >= 100){
            changeAngle(SWEEP_MAX_ANGLE - currentSweepAngle);
        }else {
            int index = 1;

            for (int i = 1; i< KE_DU.length; i++){
                if(speed <= KE_DU[i]){
                    index = i;
                    break;
                }
            }

            float angle = 30 * (index - 1) + (speed - KE_DU[index - 1]) / (KE_DU[index] - KE_DU[index - 1]) * 30;
            changeAngle(angle - currentSweepAngle);
        }

    }

    public void changeAngle(float angle) {
        if (angle == 0) {
            return;
        }

        if (currentSweepAngle == SWEEP_MAX_ANGLE && angle > 0) {
            return;
        }

        if (currentSweepAngle == 0 && angle < 0) {
            return;
        }

        if (angle > 0) {
            angle = Math.min(angle, SWEEP_MAX_ANGLE - currentSweepAngle);
        } else if (angle < 0) {
            angle = Math.max(angle, -currentSweepAngle);
        }

        this.flag = 1;
        frameCounter = 1;
        changeAngle = angle;
        invalidate();
    }



    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        width = getWidth();
        height = getHeight();
        panelTransparencyPadding = width * RATIO / 2;
        bitmapWidthRatio = width * 1.0f / panelBitmap.getWidth();
        bitmapHeightRatio = height * 1.0f / panelBitmap.getHeight();

        //对指针的bitmap进行自适应缩放
        float panelPointerBitmapHeightRatio = (height / 2 - panelTransparencyPadding)/ panelPointerBitmap.getHeight();
        matrix.setScale(panelPointerBitmapHeightRatio, panelPointerBitmapHeightRatio);
        panelPointerBitmap = Bitmap.createBitmap(panelPointerBitmap, 0, 0, panelPointerBitmap.getWidth(), panelPointerBitmap.getHeight()
                , matrix, true);

        externalRectF = new RectF(panelTransparencyPadding, panelTransparencyPadding, getWidth() - panelTransparencyPadding
                , getHeight() - panelTransparencyPadding);
        float internalCircleDiameter = INTERNAL_CIRCLE_RATIO * width;
        internalRectF = new RectF((width - internalCircleDiameter) / 2, (width - internalCircleDiameter) / 2,
                (width + internalCircleDiameter) / 2, (width + internalCircleDiameter) / 2);

        //设置指针上圆点渐变，注意前连个参数的设置与drawPanelPointer左边的变换息息相关
        RadialGradient radialGradient = new RadialGradient(0, getHeight() / 2 - panelTransparencyPadding,
                ciclePointRadiu, COLOR_EXTERNAL_ARC, 0x3301B7DA, Shader.TileMode.CLAMP);
        pointPaint.setShader(radialGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制仪表盘
        matrix.setScale(bitmapWidthRatio, bitmapHeightRatio);
        canvas.drawBitmap(panelBitmap, matrix, null);


        if (flag == 0) {
            drawPanelPointer(canvas, currentSweepAngle);

            if (currentSweepAngle == 0) {
                return;
            }

            float step = currentSweepAngle / MAX_FRAME_NUM;

            for (int i = 0; i < frameCounter; i++) {
                float tmp = step * (i + 1);//当前所画角度
                float ratio = tmp / SWEEP_MAX_ANGLE;//当所画角度占总的需要画的角度的比率
                externalPaint.setStrokeWidth(defalutMaxStrokeWidth * 1.0f * ratio);
                externalPaint.setShadowLayer(defalutMaxStrokeWidth * 4.0f * ratio, 0, 0, COLOR_ARC_SHADOW);

                canvas.drawArc(externalRectF, START_ANGLE + step * i, step, false, externalPaint);
            }

            if (frameCounter < MAX_FRAME_NUM && !isStop) {
                frameCounter++;
                postInvalidateDelayed(10);
            }
        } else if (flag == 1) {
            int tmpFrameNum = getFrameNum(changeAngle);
            //currentSweepAngle += changeAngle / 10;
            currentSweepAngle += changeAngle / tmpFrameNum;
            int arcSegmentNum = getArcSegmentNum(currentSweepAngle);
            //float step = currentSweepAngle * 1.0f / MAX_FRAME_NUM;
            float step = currentSweepAngle * 1.0f / arcSegmentNum;

            /**
             * 绘制内部弧线
             * 注：因适配问题导致直接旋转currentSweepAngle角度时，弧线会超出指针，所以稍作调整
             */
            //float adjustSweepAngle = currentSweepAngle - 5 < 0 ? 0 : currentSweepAngle - 5;
            canvas.drawArc(internalRectF, START_ANGLE, currentSweepAngle, false, internalArcPaint);
            /**
             * 绘制外部弧线
             */
            float currentStrokeWidth = 0;
            for (int i = 0; i < arcSegmentNum; i++) {
                float tmp = step * (i + 1);//当前所画角度
                float ratio = tmp / SWEEP_MAX_ANGLE;//当所画角度占总的需要画的角度的比率
                currentStrokeWidth = defalutMaxStrokeWidth * 1.0f * ratio;
                externalPaint.setStrokeWidth(currentStrokeWidth);
                externalPaint.setShadowLayer(defalutMaxStrokeWidth * 4.0f * ratio, 0, 0, COLOR_ARC_SHADOW);

                canvas.drawArc(externalRectF, START_ANGLE + step * i, step, false, externalPaint);
            }
            /**
             * 绘制指针
             */
            drawPanelPointer(canvas, currentSweepAngle);

            if (frameCounter < tmpFrameNum && !isStop) {
                frameCounter++;
                //延时递减形成加速度
                int delayTime = (int) (10 * (1 - accelerateInterpolator.getInterpolation(frameCounter * 1.0f / tmpFrameNum)));
                postInvalidateDelayed(delayTime);
            }
        }
    }

    /**
     * 获取圆弧绘制分割的段数
     * 注：角度小时分段少；扫描角度大时分段多（将240度分为30小段）
     *
     * @return
     */
    private int getArcSegmentNum(float sweepAngle) {
        if (sweepAngle == SWEEP_MAX_ANGLE) {
            return (int) (SWEEP_MAX_ANGLE / 8);
        }

        return (int) (sweepAngle / 8 + 1);
    }

    private int getFrameNum(float changeAngle) {
        if (Math.abs(changeAngle) == SWEEP_MAX_ANGLE) {
            return 15;
        }

        int num = (int) (Math.abs(changeAngle) / 3 + 1);
        return num > 15 ? 15 : num;
    }

    /**
     * 绘制指针
     *
     * @param canvas
     */
    private void drawPanelPointer(Canvas canvas, float sweepAngle) {
        canvas.save();
        float pointerBitmapWidth = panelPointerBitmap.getWidth();//计算出指针的宽度
        //绘制指针时将指针绘制在中心
        canvas.translate(getWidth() / (float)2, getHeight() / (float)2);
        canvas.rotate(60 + sweepAngle);
        //drawTextLine(canvas);
        canvas.drawBitmap(panelPointerBitmap, -pointerBitmapWidth / 2, 0, null);
        //绘制圆点
        canvas.drawCircle(0, getHeight() / 2 - panelTransparencyPadding, ciclePointRadiu, pointPaint);
        canvas.restore();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isStop = true;
        if(panelPointerBitmap != null){
            panelPointerBitmap.recycle();
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
