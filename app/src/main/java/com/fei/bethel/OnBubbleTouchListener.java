package com.fei.bethel;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

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

    public OnBubbleTouchListener(View mView, Context mContext, BethelView.OnDisappearListener onDisappearListener) {
        this.mView = mView;
        this.mContext = mContext;
        this.mDisappearListener = onDisappearListener;
        mWindowManger = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mBethelView = new BethelView(mContext);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mView.setVisibility(View.INVISIBLE);
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
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
                break;
        }
        return true;
    }

    private int getStatusBarHeight(Context context) {
        int id = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) {
            return context.getResources().getDimensionPixelSize(id);
        }
        return dp2px(context, 25);
    }

    private int dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp, context.getResources().getDisplayMetrics());
    }
}
