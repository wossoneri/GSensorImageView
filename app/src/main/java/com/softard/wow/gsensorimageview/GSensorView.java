package com.softard.wow.gsensorimageview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.io.InputStream;

/**
 * Created by wow on 17-8-21.
 */

public class GSensorView extends android.support.v7.widget.AppCompatImageView {

    private final static String TAG = "DBW";
    private final static int NUM = 5;
    Context mContext;
    Activity mActivity;
    SensorManager mSM;
    Handler mHandler;
    int mBmpWidth, mBmpHeight, mScWidth, mScHeight;
    int mDefL, mDefT, mDefR, mDefB;
    float mStart_x, mStart_y, mCurrent_x, mCurrent_y;

    public GSensorView(Context context) {
        super(context);
    }

    public GSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GSensorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setContext(Context context) {

        this.mContext = context;
        mActivity = (Activity) context;

        setImmersive();

        mSM = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        int sensorType = Sensor.TYPE_ACCELEROMETER;
        mSM.registerListener(myAccelerometerListener, mSM.getDefaultSensor(sensorType), SensorManager.SENSOR_DELAY_NORMAL);

        Bitmap bmp = getBitmapById(context, R.drawable.mega1);
        mBmpWidth = bmp.getWidth();
        mBmpHeight = bmp.getHeight();


        Log.d("DBW", "bmp w " + mBmpWidth + "   bmp h " + mBmpHeight);


        DisplayMetrics dm = new DisplayMetrics();
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        mScHeight = p.y;
        mScWidth = p.x;
        Log.d("DBW", "sc w " + mScWidth + "   sc h " + mScHeight);


        mDefL = (mScWidth - mBmpWidth) / 2;
        mDefT = (mScHeight - mBmpHeight) / 2;
        mDefR = mDefL + mBmpWidth;
        mDefB = mDefT + mBmpHeight;

//        Timer updateTimer = new Timer("gForceUpdate");
//        updateTimer.scheduleAtFixedRate(new TimerTask() {
//            public void run() {
//                mActivity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        layout(mDefL + (int)X_lateral * NUM,
//                                mDefT + (int)Y_longitudinal * NUM,
//                                mDefR + (int)X_lateral * NUM,
//                                mDefB + (int)Y_longitudinal * NUM);
//                    }
//                });
//            }
//        }, 0, 100);


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Log.d(TAG, "handler msg");
                        layout(mDefL + msg.arg1 * NUM, mDefT + msg.arg2 * NUM, mDefR + msg.arg1 * NUM, mDefB + msg.arg2 * NUM);
                        break;
                    default:
                        break;
                }
            }
        };

        this.setImageBitmap(bmp);
        invalidate();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if (mBmpWidth != 0 && mBmpHeight != 0) {
            left = mDefL;
            top = mDefT;
            right = mDefR;
            bottom = mDefB;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCurrent_x = event.getRawX();
        mCurrent_y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStart_x = mCurrent_x;
                mStart_y = mCurrent_y;
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mCurrent_x - mStart_x);
                mStart_x = mCurrent_x;

                int deltaY = (int) (mCurrent_y - mStart_y);
                mStart_y = mCurrent_y;

                mDefL += deltaX;
                mDefR += deltaX;
                mDefT += deltaY;
                mDefB += deltaY;
                layout(mDefL, mDefT, mDefR, mDefB);
                break;
            case MotionEvent.ACTION_UP:

                break;
        }


        return true;
    }

    float X_lateral;
    float Y_longitudinal;
    float last_x = 0, last_y = 0;
    float delta_x, delta_y;

    /*
     * SensorEventListener接口的实现，需要实现两个方法
     * 方法1 onSensorChanged 当数据变化的时候被触发调用
     * 方法2 onAccuracyChanged 当获得数据的精度发生变化的时候被调用，比如突然无法获得数据时
     * */
    final SensorEventListener myAccelerometerListener = new SensorEventListener() {

        //复写onSensorChanged方法
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.i(TAG, "onSensorChanged");

                //图解中已经解释三个值的含义
                X_lateral = sensorEvent.values[0];
                Y_longitudinal = sensorEvent.values[1];


                delta_x = X_lateral - last_x;
                last_x = X_lateral;
                delta_y = Y_longitudinal - last_y;
                last_y = Y_longitudinal;

                //float Z_vertical = sensorEvent.values[2];
                //Log.d(TAG,"\n heading "+X_lateral);
                //Log.d(TAG,"\n pitch "+Y_longitudinal);
                //Log.d(TAG,"\n roll "+Z_vertical);


                Log.d(TAG, "send x " + delta_x + "  y " + delta_y);
                Message msg = Message.obtain();
                msg.arg1 = (int) X_lateral;
                msg.arg2 = (int) Y_longitudinal;
                msg.what = 1;
                mHandler.sendMessage(msg);


            }
        }

        //复写onAccuracyChanged方法
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "onAccuracyChanged");
        }
    };

    public void finish() {
        mSM.unregisterListener(myAccelerometerListener);
    }

    private Bitmap getBitmapById(Context context, @DrawableRes int resId) {
        InputStream is = context.getResources().openRawResource(resId);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(is, null, opts);
    }

    private void setImmersive() {
        View decorView = mActivity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


}
