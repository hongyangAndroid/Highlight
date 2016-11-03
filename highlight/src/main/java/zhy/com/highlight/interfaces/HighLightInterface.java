package zhy.com.highlight.interfaces;
/**
 * <pre>
 * 控制高亮控件的接口
 * Created by isanwenyu on 2016/11/01.
 * Copyright (c) 2016 isanwenyu@163.com. All rights reserved.
 * </pre>
 */
public interface HighLightInterface{

    /**
     * 移除
     */
    void remove();

    /**
     * 显示
     */
    void show();


    public static interface OnClickCallback
    {
        void onClick();
    }

    /**
     * 显示回调监听
     */
    public static interface OnShowCallback
    {
        void onShow();
    }

    /**
     * 移除回调监听
     */
    public static interface OnRemoveCallback
    {
        void onRemove();
    }

}