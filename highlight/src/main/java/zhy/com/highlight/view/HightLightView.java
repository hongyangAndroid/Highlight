package zhy.com.highlight.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public HightLightView(Context context, View anchor, List<HighLight.ViewPosInfo> viewRects)
    {
        super(context);
        mInflater = LayoutInflater.from(context);
        mViewRects = viewRects;
        mAnchor = anchor;
        setWillNotDraw(false);
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
        buildInfoTip();
    }

    private void buildInfoTip()
    {
        for (HighLight.ViewPosInfo viewPosInfo : mViewRects)
        {
            View view = mInflater.inflate(viewPosInfo.layoutId, this, false);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            lp.leftMargin = viewPosInfo.left;
            lp.topMargin = viewPosInfo.top;
            addView(view, lp);
        }
    }

    private void buildMask()
    {
        mMaskBitmap = Bitmap.createBitmap(mAnchor.getMeasuredWidth(), mAnchor.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mMaskBitmap);
        canvas.drawColor(0xCC000000);
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
