package com.fei.lovebethelview;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * @ClassName: LoveBethelTypeValue
 * @Description: 贝瑟尔估值器
 * @Author: Fei
 * @CreateDate: 2021-01-20 21:17
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-01-20 21:17
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class LoveBethelTypeEvalutor implements TypeEvaluator<PointF> {

    private final PointF mPoint1;
    private final PointF mPoint2;

    public LoveBethelTypeEvalutor(PointF mPoint1, PointF mPoint2) {
        this.mPoint1 = mPoint1;
        this.mPoint2 = mPoint2;
    }

    @Override
    public PointF evaluate(float fraction, PointF startPoint, PointF endPoint) {

        PointF pointF = new PointF();
        pointF.x = startPoint.x * (1 - fraction) * (1 - fraction) * (1 - fraction)
                + 3 * mPoint1.x * fraction * (1 - fraction) * (1 - fraction)
                + 3 * mPoint2.x * fraction * fraction * (1 - fraction)
                + endPoint.x * fraction * fraction * fraction;
        pointF.y = startPoint.y * (1 - fraction) * (1 - fraction) * (1 - fraction)
                + 3 * mPoint1.y * fraction * (1 - fraction) * (1 - fraction)
                + 3 * mPoint2.y * fraction * fraction * (1 - fraction)
                + endPoint.y * fraction * fraction * fraction;
        return pointF;
    }
}
