package zhy.com.highlight.position;

import android.graphics.RectF;

import zhy.com.highlight.HighLight;

/**
 * Created by caizepeng on 16/8/20.
 */
public class OnLeftPosCallback extends OnBaseCallback {
    public OnLeftPosCallback() {
    }

    public OnLeftPosCallback(float offset) {
        super(offset);
    }

    @Override
    public void getPosition(float rightMargin, float bottomMargin, RectF rectF, HighLight.MarginInfo marginInfo) {
        marginInfo.rightMargin = rightMargin+rectF.width()+offset;
        marginInfo.topMargin = rectF.top;
    }
}
