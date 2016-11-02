package zhy.com.highlight;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import zhy.com.highlight.interfaces.HighLightInterface;
import zhy.com.highlight.shape.RectLightShape;
import zhy.com.highlight.util.ViewUtils;
import zhy.com.highlight.view.HightLightView;

/**
 * Created by zhy on 15/10/8.
 */
public class HighLight implements HighLightInterface
{
    public static class ViewPosInfo
    {
        public int layoutId = -1;
        public RectF rectF;
        public MarginInfo marginInfo;
        public View view;
        public OnPosCallback onPosCallback;
        public LightShape lightShape;
    }
    public  interface LightShape{
        public void shape(Bitmap bitmap, ViewPosInfo viewPosInfo);
    }
    public static class MarginInfo
    {
        public float topMargin;
        public float leftMargin;
        public float rightMargin;
        public float bottomMargin;

    }

    public static interface OnPosCallback
    {
        void getPos(float rightMargin, float bottomMargin, RectF rectF, MarginInfo marginInfo);
    }


    private View mAnchor;
    private List<ViewPosInfo> mViewRects;
    private Context mContext;
    private HightLightView mHightLightView;
    private HighLightInterface.OnClickCallback clickCallback;

    private boolean intercept = true;
//    private boolean shadow = true;
    private int maskColor = 0xCC000000;

    //added by isanwenyu@163.com
    private boolean autoRemove = true;//点击是否自动移除 默认为true
    private boolean next = false;//next模式标志 默认为false
    private boolean mShowing;//是否显示
    private Message mShowMessage;
    private Message mRemoveMessage;
    private Message mClickMessage;
    private ListenersHandler mListenersHandler;

    private static final int CLICK = 0x40;
    private static final int REMOVE = 0x41;
    private static final int SHOW = 0x42;

    public HighLight(Context context)
    {
        mContext = context;
        mViewRects = new ArrayList<ViewPosInfo>();
        mAnchor = ((Activity) mContext).findViewById(android.R.id.content);
        mListenersHandler = new ListenersHandler();
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

//    public HighLight shadow(boolean shadow)
//    {
//        this.shadow = shadow;
//        return this;
//    }

    public HighLight maskColor(int maskColor)
    {
        this.maskColor = maskColor;
        return this;
    }


    public HighLight addHighLight(int viewId, int decorLayoutId, OnPosCallback onPosCallback,LightShape lightShape)
    {
        ViewGroup parent = (ViewGroup) mAnchor;
        View view = parent.findViewById(viewId);
        addHighLight(view, decorLayoutId, onPosCallback,lightShape);
        return this;
    }

    public void updateInfo()
    {
        ViewGroup parent = (ViewGroup) mAnchor;
        for (HighLight.ViewPosInfo viewPosInfo : mViewRects)
        {

            RectF rect = new RectF(ViewUtils.getLocationInView(parent, viewPosInfo.view));
//            if (!rect.equals(viewPosInfo.rectF))//TODO bug dismissed...fc...
            {
                viewPosInfo.rectF = rect;
                viewPosInfo.onPosCallback.getPos(parent.getWidth() - rect.right, parent.getHeight() - rect.bottom, rect, viewPosInfo.marginInfo);
            }
        }

    }


    public HighLight addHighLight(View view, int decorLayoutId, OnPosCallback onPosCallback,LightShape lightShape)
    {
        if (onPosCallback == null && decorLayoutId != -1)
        {
            throw new IllegalArgumentException("onPosCallback can not be null.");
        }
        ViewGroup parent = (ViewGroup) mAnchor;
        RectF rect = new RectF(ViewUtils.getLocationInView(parent, view));
        //if RectF is empty return  added by isanwenyu 2016/10/26.
        if(rect.isEmpty()) return this;
        ViewPosInfo viewPosInfo = new ViewPosInfo();
        viewPosInfo.layoutId = decorLayoutId;
        viewPosInfo.rectF = rect;
        viewPosInfo.view = view;
        MarginInfo marginInfo = new MarginInfo();
        onPosCallback.getPos(parent.getWidth() - rect.right, parent.getHeight() - rect.bottom, rect, marginInfo);
        viewPosInfo.marginInfo = marginInfo;
        viewPosInfo.onPosCallback = onPosCallback;
        viewPosInfo.lightShape = lightShape == null?new RectLightShape():lightShape;
        mViewRects.add(viewPosInfo);

        return this;
    }

    // 一个场景可能有多个步骤的高亮。一个步骤完成之后再进行下一个步骤的高亮
    // 添加点击事件，将每次点击传给应用逻辑
    public HighLight setClickCallback(HighLightInterface.OnClickCallback clickCallback){
        if (clickCallback != null) {
            mClickMessage = mListenersHandler.obtainMessage(CLICK, clickCallback);
        } else {
            mClickMessage = null;
        }
        return this;
    }

    public HighLight setOnShowCallback(HighLightInterface.OnShowCallback onShowCallback) {
        if (onShowCallback != null) {
            mShowMessage = mListenersHandler.obtainMessage(SHOW, onShowCallback);
        } else {
            mShowMessage = null;
        }
        return this;
    }
    public HighLight setOnRemoveCallback(HighLightInterface.OnRemoveCallback onRemoveCallback) {
        if (onRemoveCallback != null) {
            mRemoveMessage = mListenersHandler.obtainMessage(REMOVE, onRemoveCallback);
        } else {
            mRemoveMessage = null;
        }
        return this;
    }

    /**
     * @return Whether the dialog is currently showing.
     * @author isanwenyu@163.com
     */
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * 点击后是否自动移除
     * @see #show()
     * @see #remove()
     * @return 链式接口 返回自身
     * @author isanwenyu@163.com
     */
    public HighLight autoRemove(boolean autoRemove)
    {
        this.autoRemove=autoRemove;
        return this;
    }

    /**
     * 获取高亮布局 如果要获取decorLayout中布局请在{@link #show()}后调用
     * <p>
     * 高亮布局的id在{@link #show()}中hightLightView.setId(R.id.high_light_view)设置
     * @return 返回id为R.id.high_light_view的高亮布局对象
     * @see #show()
     * @author isanwenyu@163.com
     */
    public HightLightView getHightLightView()
    {
        if (mHightLightView != null) return mHightLightView;
        if (((Activity)mContext).findViewById(R.id.high_light_view) != null)
            return  mHightLightView= (HightLightView) ((Activity)mContext).findViewById(R.id.high_light_view);
        else
            return null;
    }

    /**
     * 开启next模式
     * @see #show()
     * @return 链式接口 返回自身
     * @author isanwenyu@163.com
     */
    public HighLight enableNext()
    {
        this.next=true;
        return this;
    }

    /**
     * 返回是否是next模式
     *
     * @return
     * @author isanwenyu@163.com
     */
    public boolean isNext() {
        return next;
    }

    /**
     * 切换到下个提示布局
     * @return HighLight自身对象
     * @author isanwenyu@163.com
     */
    public HighLight next() {
        if (getHightLightView() != null) getHightLightView().next();
        else throw new NullPointerException("The HightLightView is null,you must invoke show() before this!");
        return this;
    }

    @Override
    public void show()
    {

        if (isShowing()&&getHightLightView() != null)
        {
            mHightLightView= getHightLightView();
            return;
        }else
        {   //如果View rect 容器为空 直接返回 added by isanwenyu 2016/10/26.
            if(mViewRects.isEmpty()) return;
            HightLightView hightLightView = new HightLightView(mContext, this, maskColor, mViewRects,next);
            //add high light view unique id by isanwenyu@163.com  on 2016/9/28.
            hightLightView.setId(R.id.high_light_view);
            //compatible with AutoFrameLayout ect.
            if (mAnchor instanceof FrameLayout) {
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                ((ViewGroup) mAnchor).addView(hightLightView, ((ViewGroup) mAnchor).getChildCount(), lp);

            } else
            {
                FrameLayout frameLayout = new FrameLayout(mContext);
                ViewGroup parent = (ViewGroup) mAnchor.getParent();
                parent.removeView(mAnchor);
                parent.addView(frameLayout, mAnchor.getLayoutParams());
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                frameLayout.addView(mAnchor, lp);

                frameLayout.addView(hightLightView);
            }

            if (intercept)
            {
                hightLightView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //added autoRemove by isanwenyu@163.com
                        if (autoRemove)  remove();

                        sendClickMessage();
                    }
                });
                //如果拦截才响应显示回调
                sendShowMessage();
            }

            mHightLightView = hightLightView;
            mShowing = true;

        }
    }
    @Override
    public void remove()
    {
        if (mHightLightView == null || !mShowing) return;
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
        if (intercept)
        {   //如果拦截才响应移除回调
           sendRemoveMessage();
        }
        mShowing = false;
    }


    private void sendClickMessage() {
        if (mClickMessage != null) {
            // Obtain a new message so this dialog can be re-used
            Message.obtain(mClickMessage).sendToTarget();
        }
    }
    private void sendRemoveMessage() {
        if (mRemoveMessage != null) {
            // Obtain a new message so this dialog can be re-used
            Message.obtain(mRemoveMessage).sendToTarget();
        }
    }

    private void sendShowMessage() {
        if (mShowMessage != null) {
            // Obtain a new message so this dialog can be re-used
            Message.obtain(mShowMessage).sendToTarget();
        }
    }

    /**
     * @see android.app.Dialog.ListenersHandler
     */
    private static final class ListenersHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CLICK:
                    ((HighLightInterface.OnClickCallback) msg.obj).onClick();
                    break;
                case REMOVE:
                    ((HighLightInterface.OnRemoveCallback) msg.obj).onRemove();
                    break;
                case SHOW:
                    ((HighLightInterface.OnShowCallback) msg.obj).onShow();
                    break;
            }
        }
    }
}
