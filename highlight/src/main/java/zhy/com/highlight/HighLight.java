package zhy.com.highlight;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import zhy.com.highlight.interfaces.HighLightInterface;
import zhy.com.highlight.shape.RectLightShape;
import zhy.com.highlight.util.ViewUtils;
import zhy.com.highlight.view.HightLightView;

/**
 * Created by zhy on 15/10/8.
 */
public class HighLight implements HighLightInterface, ViewTreeObserver.OnGlobalLayoutListener {
    public static class ViewPosInfo {
        public int layoutId = -1;
        public RectF rectF;
        public MarginInfo marginInfo;
        public View view;
        public OnPosCallback onPosCallback;
        public LightShape lightShape;
    }

    public interface LightShape {
        public void shape(Bitmap bitmap, ViewPosInfo viewPosInfo);
    }

    public static class MarginInfo {
        public float topMargin;
        public float leftMargin;
        public float rightMargin;
        public float bottomMargin;

    }

    public static interface OnPosCallback {
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
    private boolean isNext = false;//next模式标志 默认为false
    private boolean mShowing;//是否显示
    private Message mShowMessage;
    private Message mRemoveMessage;
    private Message mClickMessage;
    private Message mNextMessage;
    private Message mLayoutMessage;
    private ListenersHandler mListenersHandler;

    private static final int CLICK = 0x40;
    private static final int REMOVE = 0x41;
    private static final int SHOW = 0x42;
    private static final int NEXT = 0x43;
    private static final int LAYOUT = 0x44;

    public HighLight(Context context) {
        mContext = context;
        mViewRects = new ArrayList<ViewPosInfo>();
        mAnchor = ((Activity) mContext).findViewById(android.R.id.content);
        mListenersHandler = new ListenersHandler(this);
        registerGlobalLayoutListener();
    }

    public HighLight anchor(View anchor) {
        mAnchor = anchor;
        registerGlobalLayoutListener();
        return this;
    }

    @Override
    public View getAnchor() {
        return mAnchor;
    }

    public HighLight intercept(boolean intercept) {
        this.intercept = intercept;
        return this;
    }

//    public HighLight shadow(boolean shadow)
//    {
//        this.shadow = shadow;
//        return this;
//    }

    public HighLight maskColor(int maskColor) {
        this.maskColor = maskColor;
        return this;
    }


    public HighLight addHighLight(int viewId, int decorLayoutId, OnPosCallback onPosCallback, LightShape lightShape) {
        ViewGroup parent = (ViewGroup) mAnchor;
        View view = parent.findViewById(viewId);
        addHighLight(view, decorLayoutId, onPosCallback, lightShape);
        return this;
    }

    public void updateInfo() {
        ViewGroup parent = (ViewGroup) mAnchor;
        for (HighLight.ViewPosInfo viewPosInfo : mViewRects) {

            RectF rect = new RectF(ViewUtils.getLocationInView(parent, viewPosInfo.view));
//            if (!rect.equals(viewPosInfo.rectF))//TODO bug dismissed...fc...
            {
                viewPosInfo.rectF = rect;
                viewPosInfo.onPosCallback.getPos(parent.getWidth() - rect.right, parent.getHeight() - rect.bottom, rect, viewPosInfo.marginInfo);
            }
        }

    }


    public HighLight addHighLight(View view, int decorLayoutId, OnPosCallback onPosCallback, LightShape lightShape) {
        if (onPosCallback == null && decorLayoutId != -1) {
            throw new IllegalArgumentException("onPosCallback can not be null.");
        }
        ViewGroup parent = (ViewGroup) mAnchor;
        RectF rect = new RectF(ViewUtils.getLocationInView(parent, view));
        //if RectF is empty return  added by isanwenyu 2016/10/26.
        if (rect.isEmpty()) return this;
        ViewPosInfo viewPosInfo = new ViewPosInfo();
        viewPosInfo.layoutId = decorLayoutId;
        viewPosInfo.rectF = rect;
        viewPosInfo.view = view;
        MarginInfo marginInfo = new MarginInfo();
        onPosCallback.getPos(parent.getWidth() - rect.right, parent.getHeight() - rect.bottom, rect, marginInfo);
        viewPosInfo.marginInfo = marginInfo;
        viewPosInfo.onPosCallback = onPosCallback;
        viewPosInfo.lightShape = lightShape == null ? new RectLightShape() : lightShape;
        mViewRects.add(viewPosInfo);

        return this;
    }

    // 一个场景可能有多个步骤的高亮。一个步骤完成之后再进行下一个步骤的高亮
    // 添加点击事件，将每次点击传给应用逻辑
    public HighLight setClickCallback(HighLightInterface.OnClickCallback clickCallback) {
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

    public HighLight setOnNextCallback(HighLightInterface.OnNextCallback onNextCallback) {
        if (onNextCallback != null) {
            mNextMessage = mListenersHandler.obtainMessage(NEXT, onNextCallback);
        } else {
            mNextMessage = null;
        }
        return this;
    }

    /**
     * 设置根布局mAnchor全局布局监听器
     * @see #registerGlobalLayoutListener()
     * @param onLayoutCallback
     * @return
     */
    public HighLight setOnLayoutCallback(HighLightInterface.OnLayoutCallback onLayoutCallback) {
        if (onLayoutCallback != null) {
            mLayoutMessage = mListenersHandler.obtainMessage(LAYOUT, onLayoutCallback);
        } else {
            mLayoutMessage = null;
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
     *
     * @return 链式接口 返回自身
     * @author isanwenyu@163.com
     * @see #show()
     * @see #remove()
     */
    public HighLight autoRemove(boolean autoRemove) {
        this.autoRemove = autoRemove;
        return this;
    }

    /**
     * 获取高亮布局 如果要获取decorLayout中布局请在{@link #show()}后调用
     * <p>
     * 高亮布局的id在{@link #show()}中hightLightView.setId(R.id.high_light_view)设置
     *
     * @return 返回id为R.id.high_light_view的高亮布局对象
     * @author isanwenyu@163.com
     * @see #show()
     */
    @Override
    public HightLightView getHightLightView() {
        if (mHightLightView != null) return mHightLightView;
        return mHightLightView = (HightLightView) ((Activity) mContext).findViewById(R.id.high_light_view);

    }

    /**
     * 开启next模式
     *
     * @return 链式接口 返回自身
     * @author isanwenyu@163.com
     * @see #show()
     */
    public HighLight enableNext() {
        this.isNext = true;
        return this;
    }

    /**
     * 返回是否是next模式
     *
     * @return
     * @author isanwenyu@163.com
     */
    public boolean isNext() {
        return isNext;
    }

    /**
     * 切换到下个提示布局
     *
     * @return HighLight自身对象
     * @author isanwenyu@163.com
     */
    @Override
    public HighLight next() {
        if (getHightLightView() != null) getHightLightView().addViewForTip();
        else
            throw new NullPointerException("The HightLightView is null,you must invoke show() before this!");
        return this;
    }

    @Override
    public HighLight show() {

        if (getHightLightView() != null) {
            mHightLightView = getHightLightView();
            //重置当前HighLight对象属性
            mShowing = true;
            isNext = mHightLightView.isNext();
            // TODO: 2016/11/16 按需重置其他属性
            return this;
        }
        //如果View rect 容器为空 直接返回 added by isanwenyu 2016/10/26.
        if (mViewRects.isEmpty()) return this;
        HightLightView hightLightView = new HightLightView(mContext, this, maskColor, mViewRects, isNext);
        //add high light view unique id by isanwenyu@163.com  on 2016/9/28.
        hightLightView.setId(R.id.high_light_view);
        //compatible with AutoFrameLayout ect.
        if (mAnchor instanceof FrameLayout) {
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ((ViewGroup) mAnchor).addView(hightLightView, ((ViewGroup) mAnchor).getChildCount(), lp);

        } else {
            FrameLayout frameLayout = new FrameLayout(mContext);
            ViewGroup parent = (ViewGroup) mAnchor.getParent();
            parent.removeView(mAnchor);
            parent.addView(frameLayout, mAnchor.getLayoutParams());
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            frameLayout.addView(mAnchor, lp);

            frameLayout.addView(hightLightView);
        }

        if (intercept) {
            hightLightView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //added autoRemove by isanwenyu@163.com
                    if (autoRemove) remove();

                    sendClickMessage();
                }
            });
        }
        //延迟添加提示布局
        hightLightView.addViewForTip();
        mHightLightView = hightLightView;
        mShowing = true;
        //响应显示回调
        sendShowMessage();
        return this;
    }

    @Override
    public HighLight remove() {
        if (getHightLightView() == null) return this;
        ViewGroup parent = (ViewGroup) mHightLightView.getParent();
        if (parent instanceof RelativeLayout || parent instanceof FrameLayout) {
            parent.removeView(mHightLightView);
        } else {
            parent.removeView(mHightLightView);
            View origin = parent.getChildAt(0);
            ViewGroup graParent = (ViewGroup) parent.getParent();
            graParent.removeView(parent);
            graParent.addView(origin, parent.getLayoutParams());
        }
        mHightLightView = null;

        sendRemoveMessage();
        mShowing = false;
        return this;
    }


    private void sendClickMessage() {
        if (mClickMessage != null) {
            // Obtain a new message so this highlight can be re-used
            Message.obtain(mClickMessage).sendToTarget();
        }
    }

    private void sendRemoveMessage() {
        if (mRemoveMessage != null) {
            // Obtain a new message so this highlight can be re-used
            Message.obtain(mRemoveMessage).sendToTarget();
        }
    }

    private void sendShowMessage() {
        if (mShowMessage != null) {
            // Obtain a new message so this highlight can be re-used
            Message.obtain(mShowMessage).sendToTarget();
        }
    }

    private void sendLayoutMessage() {
        if (mLayoutMessage != null) {
            // Obtain a new message so this highlight can be re-used
            Message.obtain(mLayoutMessage).sendToTarget();
        }
    }

    public void sendNextMessage() {

        if (!isNext)
            throw new IllegalArgumentException("only for isNext mode,please invoke enableNext() first");

        if (getHightLightView() == null) {
            return;
        }
        //发送下一步消息事件
        ViewPosInfo viewPosInfo = getHightLightView().getCurentViewPosInfo();
        if (mNextMessage != null && viewPosInfo != null) {
            mNextMessage.arg1 = viewPosInfo.view == null ? -1 : viewPosInfo.view.getId();
            mNextMessage.arg2 = viewPosInfo.layoutId;
            Message.obtain(mNextMessage).sendToTarget();
        }
    }

    /**
     * 为mAnchor注册全局布局监听器
     */
    private void registerGlobalLayoutListener() {
        mAnchor.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }


    /**
     * mAnchor反注册全局布局监听器
     */
    private void unRegisterGlobalLayoutListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mAnchor.getViewTreeObserver()
                    .removeOnGlobalLayoutListener(this);
        }
    }

    @Override
    public void onGlobalLayout() {
        unRegisterGlobalLayoutListener();
        sendLayoutMessage();

    }


    /**
     * @see android.app.Dialog.ListenersHandler
     */
    private static final class ListenersHandler extends Handler {
        private WeakReference<HighLightInterface> mHighLightInterface;
        private HightLightView hightLightView;
        private View anchorView;

        public ListenersHandler(HighLight highLight) {
            mHighLightInterface = new WeakReference<HighLightInterface>(highLight);
        }

        @Override
        public void handleMessage(Message msg) {
            hightLightView = mHighLightInterface.get() == null ? null : mHighLightInterface.get().getHightLightView();
            anchorView = mHighLightInterface.get() == null ? null : mHighLightInterface.get().getAnchor();
            switch (msg.what) {
                case CLICK:
                    ((HighLightInterface.OnClickCallback) msg.obj).onClick();
                    break;
                case REMOVE:
                    ((HighLightInterface.OnRemoveCallback) msg.obj).onRemove();
                    break;
                case SHOW:
                    ((HighLightInterface.OnShowCallback) msg.obj).onShow(hightLightView);
                    break;
                case NEXT:
                    View targetView = anchorView != null ? anchorView.findViewById(msg.arg1) : null;
                    View tipView = hightLightView != null ? hightLightView.findViewById(msg.arg2) : null;
                    ((HighLightInterface.OnNextCallback) msg.obj).onNext(hightLightView, targetView, tipView);
                    break;
                case LAYOUT:
                    ((HighLightInterface.OnLayoutCallback) msg.obj).onLayouted();
                    break;
            }
        }
    }
}
