package com.fei.bethel;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

/**
 * @ClassName: StatusBarUtil
 * @Description: 状态栏工具类
 * @Author: Fei
 * @CreateDate: 2021/1/20 10:55
 * @UpdateUser: Fei
 * @UpdateDate: 2021/1/20 10:55
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class StatusBarUtil {

    /**
     * 设置状态栏颜色，支持沉浸式效果
     *
     * @param activity
     * @param color
     */
    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //注意要清除 FLAG_TRANSLUCENT_STATUS flag
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //5.0直接设置状态栏颜色
            activity.getWindow().setStatusBarColor(color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.0-5.0，图片延伸到状态栏只需要设置FLAG_TRANSLUCENT_STATUS就OK，不用再添加statusBarView
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //创建一个statusBarView
            View statusBarView = new View(activity);
            statusBarView.setBackgroundColor(color);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(statusBarView, layoutParams);

            ViewGroup contentView = activity.findViewById(android.R.id.content);
            contentView.setFitsSystemWindows(true);
            contentView.setClipToPadding(true);
        }
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     */
    public static int getStatusBarHeight(Context context) {
        int id = context.getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (id > 0) {
            return context.getResources().getDimensionPixelOffset(id);
        }
        return dp2px(context, 25);
    }

    private static int dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * 设置activity全屏，设置状态栏为透明，可以将图片延伸到状态栏
     *
     * @param activity
     */
    public static void setActivityTranslucent(Activity activity) {
        // 5.0 以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 4.4 - 5.0 之间
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
