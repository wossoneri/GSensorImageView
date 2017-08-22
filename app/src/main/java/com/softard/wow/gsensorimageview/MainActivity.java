package com.softard.wow.gsensorimageview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    GSensorView mGSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGSensor = (GSensorView) findViewById(R.id.gsensor);
        mGSensor.setContext(this);
    }

    @Override
    protected void onPause() {
        mGSensor.finish();
        super.onPause();
    }
}
