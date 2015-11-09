package zhy.com.highlight;

import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import zhy.com.highlight.util.ViewUtils;
import zhy.com.highlight.view.HightLightView;

/**
 * Created by zhy on 15/10/8.
 */
public class HighLight
{
    public static class ViewPosInfo
    {
        public int layoutId = -1;
        public RectF rectF;
        public int left;
        public int top;
    }

    private View mAnchor;
    private List<ViewPosInfo> mViewRects;
    private Context mContext;
    private HightLightView mHightLightView;
    private boolean intercept ;

    public HighLight(Context context)
    {
        mContext = context;
        mViewRects = new ArrayList<ViewPosInfo>();
    }

    public HighLight anchor(View anchor)
    {
        mAnchor = anchor;
        return this;
    }

    public HighLight intercept(boolean intercept)
    {
        this.intercept = intercept;
        return this;
    }



    public static interface OnPosCallback
    {
        Point getPos(RectF rectF);
    }

    public HighLight addHighLight(int viewId, int decorLayoutId, OnPosCallback onPosCallback)
    {
        ViewGroup parent = (ViewGroup) mAnchor;
        View view = parent.findViewById(viewId);
        addHighLight(view, decorLayoutId, onPosCallback);
        return this;
    }


    public HighLight addHighLight(View view, int decorLayoutId, OnPosCallback onPosCallback)
    {
        ViewGroup parent = (ViewGroup) mAnchor;
        RectF rect = new RectF(ViewUtils.getLocationInView(parent, view));
        ViewPosInfo viewPosInfo = new ViewPosInfo();
        viewPosInfo.layoutId = decorLayoutId;
        viewPosInfo.rectF = rect;
        if (onPosCallback == null && decorLayoutId != -1)
        {
            throw new IllegalArgumentException("onPosCallback can not be null.");
        }
        Point pos = onPosCallback.getPos(rect);
        if (pos == null && decorLayoutId != -1)
        {
            throw new IllegalArgumentException("onPosCallback.getPos() can not be null.");
        }
        viewPosInfo.left = pos.x;
        viewPosInfo.top = pos.y;
        mViewRects.add(viewPosInfo);

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

        if(intercept)
        {
            hightLightView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    remove();
                }
            });
        }

        mHightLightView = hightLightView;
    }

    public void remove()
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


}
