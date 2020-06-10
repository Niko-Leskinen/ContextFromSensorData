package com.example.insightsfromsensordata;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensorLight;
    private TextView mTextSensorLight;
    private Sensor mSensorProximity;
    private TextView mTextSensorProximity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextSensorLight = findViewById(R.id.label_light);
        mTextSensorProximity = (TextView) findViewById(R.id.label_proximity);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        mSensorProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        String sensor_error = getResources().getString(R.string.error_no_sensor);

        if (mSensorLight == null) {
            mTextSensorLight.setText(sensor_error);
        }
        if (mSensorProximity == null) {
            mTextSensorProximity.setText(sensor_error);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.viewSensors:
            Intent intent = new Intent(this, SensorList.class);
            startActivity(intent);
            return(true);
        case R.id.exit:
            finish();
            System.exit(0);
            return(true);
    }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float currentValue = event.values[0];
        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_LIGHT:
                mTextSensorLight.setText(getResources().getString(R.string.label_light, currentValue));
                break;
            case Sensor.TYPE_PROXIMITY:
                mTextSensorProximity.setText(getResources().getString(R.string.label_proximity, currentValue));
                break;
            default:
                // do nothing
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorLight != null) {
            mSensorManager.registerListener(this, mSensorLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorProximity != null) {
            mSensorManager.registerListener(this, mSensorProximity,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }
}
