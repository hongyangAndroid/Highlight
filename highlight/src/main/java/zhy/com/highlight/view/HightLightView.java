package zhy.com.highlight.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

import zhy.com.highlight.HighLight;

/**
 * Created by zhy on 15/10/8.
 */
public class HightLightView extends FrameLayout
{
    private static final int DEFAULT_WIDTH_BLUR = 15;
    private static final int DEFAULT_RADIUS = 6;
    private static final PorterDuffXfermode MODE_DST_OUT = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

    private Bitmap mMaskBitmap;
    private Paint mPaint;
    private List<HighLight.ViewPosInfo> mViewRects;
    private View mAnchor;
    private LayoutInflater mInflater;

    //some config
    private boolean isBlur = true;
    private int maskColor = 0xCC000000;

    public HightLightView(Context context, View anchor, int maskColor, boolean isBlur, List<HighLight.ViewPosInfo> viewRects)
    {
        super(context);
        mInflater = LayoutInflater.from(context);
        mViewRects = viewRects;
        mAnchor = anchor;
        this.maskColor = maskColor;
        this.isBlur = isBlur;
        setWillNotDraw(false);
        init();
    }

    private void init()
    {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        if (isBlur)
            mPaint.setMaskFilter(new BlurMaskFilter(DEFAULT_WIDTH_BLUR, BlurMaskFilter.Blur.SOLID));
        mPaint.setStyle(Paint.Style.FILL);

        buildMask();
        buildInfoTip();
    }

    private void buildInfoTip()
    {
        for (HighLight.ViewPosInfo viewPosInfo : mViewRects)
        {
            View view = mInflater.inflate(viewPosInfo.layoutId, this, false);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
            lp.leftMargin = (int) viewPosInfo.marginInfo.leftMargin;
            lp.topMargin = (int) viewPosInfo.marginInfo.topMargin;
            lp.rightMargin = (int) viewPosInfo.marginInfo.rightMargin;
            lp.bottomMargin = (int) viewPosInfo.marginInfo.bottomMargin;

            if (lp.leftMargin == 0 && lp.topMargin == 0)
            {
                lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            }

            addView(view, lp);
        }
    }

    private void buildMask()
    {
        mMaskBitmap = Bitmap.createBitmap(mAnchor.getMeasuredWidth(), mAnchor.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mMaskBitmap);
        canvas.drawColor(maskColor);
        mPaint.setXfermode(MODE_DST_OUT);

        for (HighLight.ViewPosInfo viewPosInfo : mViewRects)
        {
            canvas.drawRoundRect(viewPosInfo.rectF, DEFAULT_RADIUS, DEFAULT_RADIUS, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = mAnchor.getMeasuredWidth();
        int height = mAnchor.getMeasuredHeight();

        measureChildren(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),//
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawBitmap(mMaskBitmap, 0, 0, null);
        super.onDraw(canvas);

    }
}
