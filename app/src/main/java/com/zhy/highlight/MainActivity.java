package com.zhy.highlight;

import android.graphics.Point;
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


        findViewById(R.id.id_btn_amazing).post(new Runnable()
        {
            @Override
            public void run()
            {
                mHightLight = new HighLight(MainActivity.this)//
                        .anchor(findViewById(R.id.id_container))//
                        .addHighLight(R.id.id_btn_important, R.layout.info_up, new HighLight.OnPosCallback()
                        {
                            @Override
                            public Point getPos(RectF rectF)
                            {
                                return new Point((int) (rectF.left + rectF.width()) / 2, (int) rectF.bottom);
                            }
                        }).intercept(true);

                mHightLight.build();
            }
        });

    }


    public void remove(View view)
    {
        mHightLight.remove();
    }

    public void add(View view)
    {
        mHightLight.build();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
