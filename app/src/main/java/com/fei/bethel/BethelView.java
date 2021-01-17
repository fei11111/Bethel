package com.fei.bethel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @ClassName: BethelView
 * @Description: 贝塞尔曲线
 * @Author: Fei
 * @CreateDate: 2021-01-17 11:28
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-01-17 11:28
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BethelView extends View {

    private Paint mPaint;
    private int mColor = Color.RED;
    //固定圆半径
    private float mFixCircleRadius;
    //固定圆最大半径
    private float mFixCircleRadiusMax = 7;
    //固定圆最小半径
    private float mFixCircleRadiusMix = 2;
    //拖拽圆半径
    private float mDragCircleRadius = 10;

    //固定圆坐标
    private PointF mFixPoint = new PointF();
    //拖拽圆坐标
    private PointF mDragPoint = new PointF();

    public BethelView(Context context) {
        this(context, null);
    }

    public BethelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BethelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BethelView);
        mColor = typedArray.getColor(R.styleable.BethelView_circleColor, mColor);
        mDragCircleRadius = typedArray.getDimension(R.styleable.BethelView_dragCircleRadius, dp2px(mDragCircleRadius));
        mFixCircleRadiusMax = typedArray.getDimension(R.styleable.BethelView_fixCircleRadiusMax, dp2px(mFixCircleRadiusMax));
        mFixCircleRadiusMix = typedArray.getDimension(R.styleable.BethelView_fixCircleRadiusMin, dp2px(mFixCircleRadiusMix));
        typedArray.recycle();

        mFixCircleRadius = mFixCircleRadiusMax;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setColor(mColor);
    }

    /**
     * 给View添加贝塞尔图形效果
     *
     * @param view
     * @param onDisappearListener
     */
    public static void attach(View view, OnDisappearListener onDisappearListener) {
        view.setOnTouchListener(new OnBubbleTouchListener(view, view.getContext(), onDisappearListener));
    }

    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //1.画两个圆，一个固定圆，一个跟随手指移动圆
        //2.固定圆半径会改变，移动圆半径不变,固定圆半径小到一定程度就不用画
        //画拖拽圆
        canvas.drawCircle(mDragPoint.x, mDragPoint.y, mDragCircleRadius, mPaint);
        Path bethelPath = createBethelPath();
        if (bethelPath != null) {
            //画固定圆
            canvas.drawCircle(mFixPoint.x, mFixPoint.y, mFixCircleRadius, mPaint);
            //3.画贝塞尔曲线
            canvas.drawPath(bethelPath, mPaint);
        }

    }

    /**
     * 创建贝塞尔曲线
     */
    private Path createBethelPath() {
        float dx = mDragPoint.x - mFixPoint.x;
        float dy = mDragPoint.y - mFixPoint.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        mFixCircleRadius = (float) (mFixCircleRadiusMax - distance / 20f);
        if (mFixCircleRadius < mFixCircleRadiusMix) {
            //半径小于最小值，不用画
            return null;
        }
        //对比邻
        float tanA = dy / dx;
        //获取A的角度
        double atan = Math.atan(tanA);
        //p0的x轴位置=固定圆x轴+固定圆半径*sinA
        float p0x = (float) (mFixPoint.x + mFixCircleRadius * Math.sin(atan));
        //p0的y轴位置=固定圆y轴-固定圆半径*cosA
        float p0y = (float) (mFixPoint.y - mFixCircleRadius * Math.cos(atan));

        //p1的x轴位置=拖拽圆x轴+拖拽圆半径*sinA
        float p1x = (float) (mDragPoint.x + mDragCircleRadius * Math.sin(atan));
        //p1的y轴位置=拖拽圆y轴-拖拽圆半径*cosA
        float p1y = (float) (mDragPoint.y - mDragCircleRadius * Math.cos(atan));

        //p2的x轴位置=拖拽圆x轴-拖拽圆半径*sinA
        float p2x = (float) (mDragPoint.x - mDragCircleRadius * Math.sin(atan));
        //p2的y轴位置=拖拽圆y轴+拖拽圆半径*cosA
        float p2y = (float) (mDragPoint.y + mDragCircleRadius * Math.cos(atan));

        //p3的x轴位置=固定圆x轴-固定圆半径*sinA
        float p3x = (float) (mFixPoint.x - mFixCircleRadius * Math.sin(atan));
        //p3的y轴位置=固定圆y轴+固定圆半径*cosA
        float p3y = (float) (mFixPoint.y + mFixCircleRadius * Math.cos(atan));
        Path mBethelPath = new Path();
        mBethelPath.moveTo(p0x, p0y);
        //固定点x0,y0，取中心点
        float x0 = (mDragPoint.x + mFixPoint.x) / 2;
        float y0 = (mDragPoint.y + mFixPoint.y) / 2;
        mBethelPath.quadTo(x0, y0, p1x, p1y);
        mBethelPath.lineTo(p2x, p2y);
        mBethelPath.quadTo(x0, y0, p3x, p3y);
        mBethelPath.close();
        return mBethelPath;
    }

    public void initPoint(float x, float y) {
        mFixPoint.x = x;
        mFixPoint.y = y;
        mDragPoint.x = x;
        mDragPoint.y = y;
        invalidate();
    }

    public void updateDragPoint(float x, float y) {
        mDragPoint.x = x;
        mDragPoint.y = y;
        invalidate();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        float x = event.getX();
//        float y = event.getY();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//
//                break;
//            case MotionEvent.ACTION_MOVE:
//
//                break;
//            case MotionEvent.ACTION_UP:
//                break;
//        }
//
//
//        return true;
//    }

    public interface OnDisappearListener {
        void onDismiss();
    }

}
