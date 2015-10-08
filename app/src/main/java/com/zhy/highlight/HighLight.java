package com.zhy.highlight;

import android.content.Context;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.zhy.highlight.util.ViewUtils;
import com.zhy.highlight.view.HightLightView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhy on 15/10/8.
 */
public class HighLight
{
    private View mAnchor;
    private List<RectF> mViewRects;
    private Context mContext;
    private HightLightView mHightLightView;

    public HighLight(Context context)
    {
        mContext = context;
        mViewRects = new ArrayList<RectF>();
    }

    public HighLight setHighLights(int... viewIds)
    {
        if (!(mAnchor instanceof ViewGroup))
        {
            throw new IllegalArgumentException("anchor should be ViewGroup instance!");
        }

        ViewGroup parent = (ViewGroup) mAnchor;
        RectF rect = null;
        for (int viewId : viewIds)
        {
            View view = parent.findViewById(viewId);
            rect = new RectF(ViewUtils.getLocationInView(parent, view));
            mViewRects.add(rect);
        }

        return this;
    }

    public void removeSelf()
    {
        if (mHightLightView == null) return;
        ViewGroup parent = (ViewGroup) mHightLightView.getParent();
        if (parent instanceof RelativeLayout || parent instanceof FrameLayout)
        {
            parent.removeView(mHightLightView);
        } else
        {
            parent.removeView(mHightLightView);
            View origin = parent.getChildAt(0);
            ViewGroup graParent = (ViewGroup) parent.getParent();
            graParent.removeView(parent);
            graParent.addView(origin, parent.getLayoutParams());
        }

        mHightLightView = null;
    }


    public HighLight setHighLights(View... views)
    {
        if (!(mAnchor instanceof ViewGroup))
        {
            throw new IllegalArgumentException("anchor should be ViewGroup instance!");
        }

        ViewGroup parent = (ViewGroup) mAnchor;
        RectF rect = null;
        for (View view : views)
        {
            rect = new RectF(ViewUtils.getLocationInView(parent, view));
            mViewRects.add(rect);
        }

        return this;
    }

    public HighLight setAnchor(View anchor)
    {
        mAnchor = anchor;
        return this;
    }

    public void build()
    {

        if (mHightLightView != null) return;

        HightLightView hightLightView = new HightLightView(mContext, mAnchor, mViewRects);
        if (mAnchor instanceof FrameLayout || mAnchor instanceof RelativeLayout)
        {
            ((ViewGroup) mAnchor).addView(hightLightView);
        } else
        {
            FrameLayout frameLayout = new FrameLayout(mContext);
            ViewGroup parent = (ViewGroup) mAnchor.getParent();
            parent.removeView(mAnchor);
            parent.addView(frameLayout, mAnchor.getLayoutParams());
            frameLayout.addView(mAnchor);
            frameLayout.addView(hightLightView);
        }

        mHightLightView = hightLightView;
    }

}
