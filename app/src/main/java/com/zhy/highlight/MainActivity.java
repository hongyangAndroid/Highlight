package com.zhy.highlight;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import zhy.com.highlight.HighLight;

public class MainActivity extends AppCompatActivity
{

    private HighLight mHightLight;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.id_btn_amazing).post(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        showTipMask();
                    }
                }

        );

    }

    private void showTipMask()
    {
        mHightLight = new HighLight(MainActivity.this)//
                .anchor(findViewById(R.id.id_container))//
                .addHighLight(R.id.id_btn_important, R.layout.info_up,
                        new HighLight.OnPosCallback()
                        {
                            @Override
                            public void getPos(float rightMargin, float bottomMargin, RectF rectF, HighLight.MarginInfo marginInfo)
                            {
                                marginInfo.leftMargin = rectF.right - rectF.width() / 2;
                                marginInfo.topMargin = rectF.bottom;
                            }
                        })//
                .addHighLight(R.id.id_btn_amazing, R.layout.info_down, new HighLight.OnPosCallback()
                {
                    /**
                     * @param rightMargin 高亮view在anchor中的右边距
                     * @param bottomMargin 高亮view在anchor中的下边距
                     * @param rectF 高亮view的l,t,r,b,w,h都有
                     * @param marginInfo 设置你的布局的位置，一般设置l,t或者r,b
                     */
                    @Override
                    public void getPos(float rightMargin, float bottomMargin, RectF rectF, HighLight.MarginInfo marginInfo)
                    {
                        marginInfo.rightMargin = rightMargin + rectF.width() / 2;
                        marginInfo.bottomMargin = bottomMargin + rectF.height();
                    }

                });

        mHightLight.show();
    }


    public void remove(View view)
    {
        mHightLight.remove();
    }

    public void add(View view)
    {
        mHightLight.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            //test  container change
//            View container = findViewById(R.id.id_container);
//            ViewGroup.LayoutParams lp =
//                    container.getLayoutParams();
//            lp.height = container.getMeasuredHeight() + 100;
//            findViewById(R.id.id_container).setLayoutParams(lp);
        }
        return super.onOptionsItemSelected(item);
    }
}
