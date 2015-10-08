package com.zhy.highlight.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.View;

import java.util.List;

/**
 * Created by zhy on 15/10/8.
 */
public class HightLightView extends View
{
    private static final int DEFAULT_WIDTH_BLUR = 15;
    private static final int DEFAULT_RADIUS = 6;
    private static final PorterDuffXfermode MODE_DST_OUT = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);


    private Bitmap mMaskBitmap;
    private Paint mPaint;
    private List<RectF> mViewRects;
    private View mAnchor;

    public HightLightView(Context context, View anchor, List<RectF> viewRects)
    {
        super(context);
        mViewRects = viewRects;
        mAnchor = anchor;

        init();

    }

    private void init()
    {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setMaskFilter(new BlurMaskFilter(DEFAULT_WIDTH_BLUR, BlurMaskFilter.Blur.SOLID));
        mPaint.setStyle(Paint.Style.FILL);

        buildMask();
    }

    private void buildMask()
    {
        mMaskBitmap = Bitmap.createBitmap(mAnchor.getMeasuredWidth(), mAnchor.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mMaskBitmap);
        canvas.drawColor(0xAA000000);
        mPaint.setXfermode(MODE_DST_OUT);

        for (RectF rect : mViewRects)
        {
            canvas.drawRoundRect(rect, DEFAULT_RADIUS, DEFAULT_RADIUS, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        setMeasuredDimension(mAnchor.getMeasuredWidth(), mAnchor.getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawBitmap(mMaskBitmap, 0, 0, null);
    }
}
