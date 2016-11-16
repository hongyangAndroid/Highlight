package zhy.com.highlight.shape;

import android.graphics.Bitmap;
import android.graphics.RectF;

import zhy.com.highlight.HighLight;
import zhy.com.highlight.view.HightLightView;

/**
 * <pre>
 * 高亮形状的超类
 * Created by isanwenyu on 2016/10/26.
 * Copyright (c) 2016 isanwenyu@163.com. All rights reserved.
 * </pre>
 */
public abstract class BaseLightShape implements HighLight.LightShape{
    protected float dx;//水平方向偏移
    protected float dy;//垂直方向偏移
    protected float blurRadius=15;//模糊半径 默认15

    public BaseLightShape() {
    }

    /**
     * @param dx 水平方向偏移
     * @param dy 垂直方向偏移
     */
    public BaseLightShape(float dx,float dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * @param dx 水平方向偏移
     * @param dy 垂直方向偏移
     * @param blurRadius 模糊半径 默认15px 0不模糊
     */
    public BaseLightShape(float dx, float dy, float blurRadius) {
        this.dx = dx;
        this.dy = dy;
        this.blurRadius = blurRadius;
    }

    @Override
    public void shape(Bitmap bitmap, HighLight.ViewPosInfo viewPosInfo) {
        resetRectF4Shape(viewPosInfo.rectF,dx,dy);
        drawShape(bitmap,viewPosInfo);
    }

    /**
     * reset RectF for Shape by dx and dy.
     * @param viewPosInfoRectF
     * @param dx
     * @param dy
     */
    protected abstract void resetRectF4Shape(RectF viewPosInfoRectF, float dx, float dy);

    /**
     * draw shape into bitmap
     * @param bitmap
     * @param viewPosInfo
     * @see zhy.com.highlight.view.HightLightView#addViewForEveryTip(HighLight.ViewPosInfo)
     * @see HightLightView#buildMask()
     */
    protected abstract void drawShape(Bitmap bitmap, HighLight.ViewPosInfo viewPosInfo);

}
