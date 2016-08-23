package zhy.com.highlight.shape;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import zhy.com.highlight.HighLight;

/**
 * Created by caizepeng on 16/8/20.
 */
public class CircleLightShape implements HighLight.LightShape {
    @Override
    public void shape(Bitmap bitmap, HighLight.ViewPosInfo viewPosInfo) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.SOLID));
        RectF rectF = viewPosInfo.rectF;
        canvas.drawCircle(rectF.left+(rectF.width()/2),rectF.top+(rectF.height()/2),
                Math.max(rectF.width(),rectF.height())/2,paint);
    }
}
