package com.fei.bethel;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
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
    private FrameLayout mBombFrameLayout;
    private ImageView mBombImageView;
    private WindowManager.LayoutParams mLayoutParams;
    private ViewGroup.LayoutParams mOriginalViewParams;

    public OnBubbleTouchListener(View mView, Context mContext, BethelView.OnDisappearListener onDisappearListener) {
        this.mView = mView;
        this.mContext = mContext;
        this.mDisappearListener = onDisappearListener;
        mWindowManger = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mBethelView = new BethelView(mContext);
        mBombFrameLayout = new FrameLayout(mContext);
        mLayoutParams = new WindowManager.LayoutParams();
        //背景透明
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        mLayoutParams.format = PixelFormat.TRANSPARENT;
        mLayoutParams.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mBombImageView = new ImageView(mContext);
        mBombImageView.setImageResource(R.drawable.anim_bubble_pop);
        mBombFrameLayout.addView(mBombImageView, layoutParams);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //按下隐藏原来的View
                mView.setVisibility(View.INVISIBLE);
                mOriginalViewParams = mView.getLayoutParams();
                //在windowManager添加一个一样的View
                mBethelView.setBitmap(getBitmap(mView));
                mWindowManger.addView(mBethelView, mLayoutParams);
                //取view的中心点
                int[] location = new int[2];
                mView.getLocationOnScreen(location);
                mBethelView.initPoint(location[0] + mView.getWidth() / 2, location[1] + mView.getHeight() / 2);
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
                        mView.setLayoutParams(mOriginalViewParams);
                        mView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void dismiss(PointF pointF) {
                        //清除WindowManager，原来View消失
                        mWindowManger.removeView(mBethelView);
                        //设置imageView在居中位置
                        mBombImageView.setX(pointF.x - mBombImageView.getWidth() / 2);
                        mBombImageView.setY(pointF.y - mBombImageView.getHeight() / 2);
                        //windowManger添加ImageView，启动爆炸动画
                        mWindowManger.addView(mBombFrameLayout, mLayoutParams);
                        //开启爆炸动画
                        startBombAnimator();
                    }
                });
                break;
        }
        return true;
    }

    /**
     * 开启爆炸动画
     */
    private void startBombAnimator() {
        AnimationDrawable animationDrawable = (AnimationDrawable) mBombImageView.getDrawable();
        //获取每帧时间,为了延迟移除WindowManager上的爆炸界面
        long time = 0;
        for (int i = 0; i < animationDrawable.getNumberOfFrames(); i++) {
            time += animationDrawable.getDuration(i);
        }
        animationDrawable.start();
        mBombImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //移除WindowManager上的爆炸界面
                mWindowManger.removeView(mBombFrameLayout);
                mView.setVisibility(View.GONE);
            }
        }, time);
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


    public interface IActionUpListener {
        //回弹
        void springBack();

        //爆炸动画
        void dismiss(PointF pointF);
    }
}
