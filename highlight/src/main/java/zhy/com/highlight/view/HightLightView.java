package zhy.com.highlight.view;

import android.content.Context;
import android.graphics.Bitmap;
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
    private Bitmap mLightBitmap;
    private Paint mPaint;
    private List<HighLight.ViewPosInfo> mViewRects;
    private HighLight mHighLight;
    private LayoutInflater mInflater;

    //some config
//    private boolean isBlur = true;
    private int maskColor = 0xCC000000;

    // added by isanwenyu@163.com
    private final boolean isNext;//next模式标志
    private int mPosition=-1;//当前显示的提示布局位置
    private HighLight.ViewPosInfo mViewPosInfo;//当前显示的高亮布局位置信息

    public HightLightView(Context context, HighLight highLight, int maskColor, List<HighLight.ViewPosInfo> viewRects,boolean isNext)
    {
        super(context);
        mHighLight = highLight;
        mInflater = LayoutInflater.from(context);
        mViewRects = viewRects;
        this.maskColor = maskColor;
//        this.isBlur = isBlur;
        this.isNext=isNext;
        setWillNotDraw(false);
        init();
    }

    private void init()
    {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
//        if (isBlur)
//            mPaint.setMaskFilter(new BlurMaskFilter(DEFAULT_WIDTH_BLUR, BlurMaskFilter.Blur.SOLID));
        mPaint.setStyle(Paint.Style.FILL);

    }

    public void addViewForTip()
    {
        if (isNext)
        {
            //校验mPosition
            if (mPosition < -1 || mPosition > mViewRects.size() - 1)
            {
                //重置位置
                mPosition = 0;
            } else if (mPosition == mViewRects.size() - 1)
            {
                //移除当前布局
                mHighLight.remove();
                return;
            } else
            {
                //mPosition++
                mPosition++;
            }
            mViewPosInfo = mViewRects.get(mPosition);
            //移除所有tip再添加当前位置的tip布局
            removeAllTips();
            addViewForEveryTip(mViewPosInfo);

            if (mHighLight != null) {
                mHighLight.sendNextMessage();
            }

        } else
        {
            for (HighLight.ViewPosInfo viewPosInfo : mViewRects)
            {
                addViewForEveryTip(viewPosInfo);
            }
        }
    }

    /**
     * 移除当前高亮布局的所有提示布局
     *
     */
    private void removeAllTips() {
        removeAllViews();
    }

    /**
     * 添加每个高亮布局
     * @param viewPosInfo 高亮布局信息
     * @author isanwenyu@163.com
     */
    private void addViewForEveryTip(HighLight.ViewPosInfo viewPosInfo)
    {
        View view = mInflater.inflate(viewPosInfo.layoutId, this, false);
        //设置id为layout id 供HighLight查找
        view.setId(viewPosInfo.layoutId);
        LayoutParams lp = buildTipLayoutParams(view, viewPosInfo);

        if (lp == null) return;

        lp.leftMargin = (int) viewPosInfo.marginInfo.leftMargin;
        lp.topMargin = (int) viewPosInfo.marginInfo.topMargin;
        lp.rightMargin = (int) viewPosInfo.marginInfo.rightMargin;
        lp.bottomMargin = (int) viewPosInfo.marginInfo.bottomMargin;

        //fix the bug can't set gravity  LEFT|BOTTOM  or RIGHT|TOP
//            if (lp.leftMargin == 0 && lp.topMargin == 0)
//            {
//                lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
//            }

        if(lp.rightMargin != 0){
            lp.gravity = Gravity.RIGHT;
        }else {
            lp.gravity = Gravity.LEFT;
        }

        if(lp.bottomMargin != 0){
            lp.gravity |= Gravity.BOTTOM;
        }else {
            lp.gravity |= Gravity.TOP;
        }
        addView(view, lp);
    }

    private void buildMask()
    {
        recycleBitmap(mMaskBitmap);
        mMaskBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(mMaskBitmap);
        canvas.drawColor(maskColor);
        mPaint.setXfermode(MODE_DST_OUT);
        mHighLight.updateInfo();

        recycleBitmap(mLightBitmap);
        mLightBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_4444);

        if(isNext)//如果是next模式添加每个提示布局的背景形状
        {
            //添加当前提示布局的高亮形状背景
            addViewEveryTipShape(mViewPosInfo);
        }else
        {
            for (HighLight.ViewPosInfo viewPosInfo : mViewRects)
            {
                addViewEveryTipShape(viewPosInfo);
            }
        }
        canvas.drawBitmap(mLightBitmap,0,0,mPaint);
    }

    /**
     * 主动回收之前创建的bitmap
     * @param bitmap
     */
    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled())
        {
            bitmap.recycle();
            bitmap = null;
            System.gc();
        }
    }

    /**
     * 添加提示布局的背景形状
     * @param viewPosInfo //提示布局的位置信息
     * @author isanwenyu@16.com
     */
    private void addViewEveryTipShape(HighLight.ViewPosInfo viewPosInfo)
    {
        viewPosInfo.lightShape.shape(mLightBitmap,viewPosInfo);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),//
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        if (changed || isNext) //edited by isanwenyu@163.com for next mode
        {
            buildMask();
            updateTipPos();
        }

    }

    private void updateTipPos()
    {
        if (isNext)//如果是next模式 只有一个子控件 刷新当前位置tip
        {
            View view = getChildAt(0);

            LayoutParams lp = buildTipLayoutParams(view, mViewPosInfo);
            if (lp == null) return;
            view.setLayoutParams(lp);

        }else
        {
            for (int i = 0, n = getChildCount(); i < n; i++)
            {
                View view = getChildAt(i);
                HighLight.ViewPosInfo viewPosInfo = mViewRects.get(i);

                LayoutParams lp = buildTipLayoutParams(view, viewPosInfo);
                if (lp == null) continue;
                view.setLayoutParams(lp);
            }
        }
    }

    private LayoutParams buildTipLayoutParams(View view, HighLight.ViewPosInfo viewPosInfo)
    {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp.leftMargin == (int) viewPosInfo.marginInfo.leftMargin &&
                lp.topMargin == (int) viewPosInfo.marginInfo.topMargin &&
                lp.rightMargin == (int) viewPosInfo.marginInfo.rightMargin &&
                lp.bottomMargin == (int) viewPosInfo.marginInfo.bottomMargin) return null;

        lp.leftMargin = (int) viewPosInfo.marginInfo.leftMargin;
        lp.topMargin = (int) viewPosInfo.marginInfo.topMargin;
        lp.rightMargin = (int) viewPosInfo.marginInfo.rightMargin;
        lp.bottomMargin = (int) viewPosInfo.marginInfo.bottomMargin;

        //fix the bug can't set gravity  LEFT|BOTTOM  or RIGHT|TOP
//        if (lp.leftMargin == 0 && lp.topMargin == 0)
//        {
//            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
//        }
        if(lp.rightMargin != 0){
            lp.gravity = Gravity.RIGHT;
        }else {
            lp.gravity = Gravity.LEFT;
        }

        if(lp.bottomMargin != 0){
            lp.gravity |= Gravity.BOTTOM;
        }else {
            lp.gravity |= Gravity.TOP;
        }
        return lp;
    }


    @Override
    protected void onDraw(Canvas canvas)
    {

        try {
            canvas.drawBitmap(mMaskBitmap, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDraw(canvas);

    }

    public boolean isNext() {
        return isNext;
    }

    /**
     * @return 当前高亮提示布局信息
     */
    public HighLight.ViewPosInfo getCurentViewPosInfo() {
        return mViewPosInfo;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //optimize recycle bitmap
        recycleBitmap(mLightBitmap);
        recycleBitmap(mMaskBitmap);
    }
}
