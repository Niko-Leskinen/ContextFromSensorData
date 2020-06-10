package com.example.insightsfromsensordata;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;

public class SensorList extends AppCompatActivity {

    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_sensor_list);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList  = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        StringBuilder sensorText = new StringBuilder();

        for (Sensor currentSensor : sensorList ) {
            sensorText.append(currentSensor.getName()).append(
                    System.getProperty("line.separator"));
        }
        TextView sensorTextView = findViewById(R.id.sensor_list);
        sensorTextView.setText(sensorText);
    }
    // Close this activity when user taps arrow in action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return false;
    }
}
