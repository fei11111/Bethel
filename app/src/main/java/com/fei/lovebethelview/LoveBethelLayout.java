package com.fei.lovebethelview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;

import androidx.core.content.ContextCompat;

/**
 * @ClassName: LoveBethelLayout
 * @Description: 花束点赞自定义布局
 * @Author: Fei
 * @CreateDate: 2021-01-20 20:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-01-20 20:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class LoveBethelLayout extends RelativeLayout {

    private Random mRandom;
    private int[] mDrawables;
    private Interpolator[] mInterpolators;
    private int mAppearDuration = 350;//出现时动画时间
    private int mBehtelDuration = 3000;//贝瑟尔运动时间
    private int mWidth;//宽度
    private int mHeight;//高度
    private int mDrawableWidth;//图片宽度
    private int mDrawableHeight;//图片高度
    private int[] mColors;

    public LoveBethelLayout(Context context) {
        this(context, null);
    }

    public LoveBethelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoveBethelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mRandom = new Random();
        mColors = new int[]{
                Color.parseColor("#aa00ee00"),
                Color.parseColor("#aa0000ee"),
                Color.parseColor("#aaee0000"),
                Color.parseColor("#aacccccc"),
                Color.parseColor("#aae8c9c9"),
                Color.parseColor("#aaF289c5")
        };
        mDrawables = new int[]{
                R.drawable.pl_blue, R.drawable.pl_red, R.drawable.pl_yellow
        };
        mInterpolators = new Interpolator[]{
                new AccelerateDecelerateInterpolator(),
                new DecelerateInterpolator(),
                new LinearInterpolator(),
                new AccelerateInterpolator()
        };
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.pl_blue);
        mDrawableWidth = drawable.getIntrinsicWidth();
        mDrawableHeight = drawable.getIntrinsicHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    /**
     * 添加点赞爱心图片
     */
    public void addLoveView() {
        //添加爱心在底部
        ImageView imageView = new ImageView(getContext());
        imageView.setColorFilter(mColors[mRandom.nextInt(mColors.length)]);
        imageView.setImageResource(R.drawable.pl_blue);
//        imageView.setImageResource(mDrawables[mRandom.nextInt(mDrawables.length)]);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(CENTER_HORIZONTAL);
        addView(imageView, layoutParams);

        //开启动画效果
        openAnimation(imageView);
    }

    /**
     * 开启动画效果
     *
     * @param imageView
     */
    private void openAnimation(ImageView imageView) {
        AnimatorSet allAnimatorSet = new AnimatorSet();
        //透明度从0.3-1.0
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(imageView, "alpha", 0.3f, 1f);
        //缩放从0.3-1.0
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(imageView, "scaleX", 0.3f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(imageView, "scaleY", 0.3f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator);
        set.setDuration(mAppearDuration);
        //之后开始贝塞尔运动
        ValueAnimator bethelAnimator = getLoveBethelAnimator(imageView);
        bethelAnimator.setInterpolator(mInterpolators[mRandom.nextInt(mInterpolators.length)]);
        bethelAnimator.setDuration(mBehtelDuration);
        allAnimatorSet.playSequentially(set, bethelAnimator);
        allAnimatorSet.start();
    }

    /**
     * 获取爱心图片贝瑟尔运动动画
     *
     * @param imageView
     * @return
     */
    private ValueAnimator getLoveBethelAnimator(final ImageView imageView) {
        PointF point0 = new PointF(mWidth / 2 - mDrawableWidth / 2, mHeight - mDrawableHeight);
        PointF point1 = new PointF(mRandom.nextInt(mWidth) - mDrawableWidth, mRandom.nextInt(mHeight / 2));
        PointF point2 = new PointF(mRandom.nextInt(mWidth) - mDrawableWidth, mRandom.nextInt(mHeight / 2) + mHeight / 2);
        PointF point3 = new PointF(mRandom.nextInt(mWidth) - mDrawableWidth, 0);
        ValueAnimator valueAnimator = ObjectAnimator.ofObject(new LoveBethelTypeEvalutor(point1, point2), point0, point3);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //修改图片位置
                PointF pointF = (PointF) animation.getAnimatedValue();
                imageView.setX(pointF.x);
                imageView.setY(pointF.y);
                //修改透明度 1f-0.3f
                float fraction = animation.getAnimatedFraction(); //0f-1f
                imageView.setAlpha(1f - 0.7f * fraction);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeView(imageView);
            }
        });
        return valueAnimator;
    }

}
