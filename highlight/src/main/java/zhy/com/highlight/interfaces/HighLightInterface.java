package zhy.com.highlight.interfaces;

import android.view.View;

import zhy.com.highlight.HighLight;
import zhy.com.highlight.view.HightLightView;

/**
 * <pre>
 * 控制高亮控件的接口
 * Created by isanwenyu on 2016/11/01.
 * Copyright (c) 2016 isanwenyu@163.com. All rights reserved.
 * </pre>
 */
public interface HighLightInterface {

    /**
     * 移除
     */
    HighLight remove();

    /**
     * 显示
     */
    HighLight show();

    /**
     * 显示下一个布局
     *
     * @return
     */
    HighLight next();

    /**
     * @return 锚点布局
     */
    View getAnchor();

    /**
     * @return 高亮布局控件
     */
    HightLightView getHightLightView();

    public static interface OnClickCallback {
        void onClick();
    }

    /**
     * 显示回调监听
     */
    public static interface OnShowCallback {
        /**
         * @param hightLightView 高亮布局控件
         */
        void onShow(HightLightView hightLightView);
    }

    /**
     * 移除回调监听
     */
    public static interface OnRemoveCallback {
        /**
         * 移除高亮布局
         */
        void onRemove();
    }

    /**
     * 下一个回调监听 只有Next模式下生效
     */
    public static interface OnNextCallback {
        /**
         * 监听下一步动作
         *
         * @param hightLightView 高亮布局控件
         * @param targetView     高亮目标控件
         * @param tipView        高亮提示控件
         */
        void onNext(HightLightView hightLightView, View targetView, View tipView);
    }

    /**
     * mAnchor全局布局监听器
     */
    public static interface OnLayoutCallback {
        /**
         * 布局结束
         */
        void onLayouted();
    }
}