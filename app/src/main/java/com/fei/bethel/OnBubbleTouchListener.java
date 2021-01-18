package com.fei.bethel;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * @ClassName: OnBubbleTouchListener
 * @Description: java类作用描述
 * @Author: Fei
 * @CreateDate: 2021-01-17 21:12
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-01-17 21:12
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class OnBubbleTouchListener implements View.OnTouchListener {

    // 点下，原来的View要不可见，WindowManager添加一样的View，可以拖拽
    // 可拖拽的View是原来的View
    // 手指松开，1.消失不见开启消失动画 2.回到原来位置，原来View可见

    private final BethelView.OnDisappearListener mDisappearListener;
    private final View mView;
    private final Context mContext;
    private WindowManager mWindowManger;
    private BethelView mBethelView;
    //爆炸FrameLayout，需要包裹ImageView，防止ImageView覆盖全屏
    private FrameLayout mBoundFrameLayout;
    private ImageView mBoundImageView;

    public OnBubbleTouchListener(View mView, Context mContext, BethelView.OnDisappearListener onDisappearListener) {
        this.mView = mView;
        this.mContext = mContext;
        this.mDisappearListener = onDisappearListener;
        mWindowManger = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mBethelView = new BethelView(mContext);
        mBoundFrameLayout = new FrameLayout(mContext);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mBoundImageView = new ImageView(mContext);
        mBoundFrameLayout.addView(mBoundImageView, layoutParams);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //按下隐藏原来的View
                mView.setVisibility(View.INVISIBLE);
                //在windowManager添加一个一样的View
                mBethelView.setBitmap(getBitmap(mView));
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                //背景透明
                layoutParams.format = PixelFormat.TRANSPARENT;
                mWindowManger.addView(mBethelView, layoutParams);
                //取view的中心点
                int[] location = new int[2];
                mView.getLocationOnScreen(location);
                mBethelView.initPoint(location[0] + mView.getWidth() / 2, location[1] + mView.getHeight() / 2 - getStatusBarHeight(mContext));
                break;
            case MotionEvent.ACTION_MOVE:
                mBethelView.updateDragPoint(event.getRawX(), event.getRawY());
                break;
            case MotionEvent.ACTION_UP:
                mBethelView.actionUp(new IActionUpListener() {
                    @Override
                    public void springBack() {
                        //回弹后，清除WindowManager，原来View可见
                        mWindowManger.removeView(mBethelView);
                        mView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void dismiss(PointF pointF) {
                        //清除WindowManager，原来View消失
                        //windowManger添加ImageView，启动爆炸动画
                    }
                });
                break;
        }
        return true;
    }

    /**
     * 获取View的截图
     *
     * @param view
     * @return
     */
    private Bitmap getBitmap(View view) {
        if (view == null) return null;
        Bitmap b;
        //请求转换
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //获取layout的位置
            final int[] location = new int[2];
            view.getLocationInWindow(location);
            //准备一个bitmap对象，用来将copy出来的区域绘制到此对象中
            b = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888, true);
            PixelCopy.request(((Activity) (view.getContext())).getWindow(),
                    new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight()),
                    b, new PixelCopy.OnPixelCopyFinishedListener() {
                        @Override
                        public void onPixelCopyFinished(int copyResult) {

                        }
                    }, new Handler(Looper.getMainLooper()));
        } else {
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY));
                view.layout((int) view.getX(), (int) view.getY(),
                        (int) view.getX() + view.getMeasuredWidth(), (int) view.getY() + view.getMeasuredWidth());
            } else {
                view.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            }
            b = Bitmap.createBitmap(view.getDrawingCache(),
                    0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.setDrawingCacheEnabled(false);
            view.destroyDrawingCache();
        }
        return b;

    }

    /**
     * 状态栏高度
     *
     * @param context
     * @return
     */
    private int getStatusBarHeight(Context context) {
        int id = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) {
            return context.getResources().getDimensionPixelSize(id);
        }
        return dp2px(context, 25);
    }

    private int dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public interface IActionUpListener {
        //回弹
        void springBack();

        //爆炸动画
        void dismiss(PointF pointF);
    }
}
