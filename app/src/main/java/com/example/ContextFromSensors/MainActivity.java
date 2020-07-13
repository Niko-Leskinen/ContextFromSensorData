package com.example.ContextFromSensors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener, GpsStatus.Listener {

    // Managers are a system services that are used to access the sensors in the device
    private SensorManager sensorManager;

    private LocationManager locationManager;

    private android.media.MediaRecorder mediaRecorder;

    // SensorActivity is the class in which sensor readings are turned into information of context
    private SensorActivity sensors = new SensorActivity();

    // Sensors
    private Sensor SensorLight;
    private Sensor SensorAccelerometer;
    private Sensor SensorStepDetector;

    // Fields to present sensor outputs and insights
    private TextView TextSensorLight;
    private TextView TextUserSpeed;
    private TextView TextUserSound;
    private TextView TextUserVelocity;

    private TextView TextUserLightContext;
    private TextView TextUserSpeedContext;
    private TextView TextUserSoundContext;

    private TextView TextUserFallEvent;
    private TextView TextUserStepsEvent;


    // TabLayout
    private TabLayout tabLayout;

    // Layouts
    private View contextLayout;
    private View sensorLayout;

    private Handler handler = new Handler();

    /* This interval is used to to not call fall_event method multiple times
    during single fall event that could potentially result in false fall counter. */
    static long lastTime = 0;
    static long interval = 250; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contextLayout = findViewById(R.id.context_layout);
        sensorLayout = findViewById(R.id.sensor_layout);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        sensorLayout.setVisibility(View.GONE);
                        contextLayout.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        contextLayout.setVisibility(View.GONE);
                        sensorLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        TextSensorLight = findViewById(R.id.label_light);
        TextUserLightContext = findViewById(R.id.label_user_light_context);
        TextUserSpeed = findViewById(R.id.label_speed);
        TextUserSpeedContext = findViewById(R.id.label_user_speed_context);
        TextUserSound = findViewById(R.id.label_sound);
        TextUserSoundContext = findViewById(R.id.label_sound_context);
        TextUserVelocity = findViewById(R.id.label_velocity);

        TextUserFallEvent = findViewById(R.id.label_event_fall);
        TextUserStepsEvent = findViewById(R.id.label_event_steps);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mediaRecorder = new MediaRecorder();

        SensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        SensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        String sensor_error = getResources().getString(R.string.error_no_sensor);

        if (SensorLight == null) {
            TextSensorLight.setText(sensor_error);
        } else if (SensorAccelerometer == null) {
            TextUserSpeed.setText(sensor_error);
        }

        // Check permissions
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 1);
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, 2);
        checkPermission(Manifest.permission.RECORD_AUDIO, 3);
        checkPermission(Manifest.permission.ACTIVITY_RECOGNITION, 4);
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { permission },
                    requestCode);
        }
    }


            @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.viewSensors:
                Intent intent = new Intent(this, SensorList.class);
                startActivity(intent);
                return (true);
            case R.id.exit:
                finish();
                System.exit(0);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_LIGHT:
                int currentValue = (int) event.values[0];
                TextSensorLight.setText(getResources().getString(R.string.label_light, event.values[0]));
                TextUserLightContext.setText(getResources().getString(R.string.label_user_light_context, sensors.light_context(currentValue)));
                break;
            case Sensor.TYPE_ACCELEROMETER:
                long time = System.currentTimeMillis();
                if (lastTime + interval < time) {
                    lastTime = time;
                    TextUserFallEvent.setText(getResources().getString(R.string.label_event_fall, sensors.fall_event(event.values[0], event.values[1], event.values[2])));
                }
                TextUserVelocity.setText(getResources().getString(R.string.label_velocity, event.values[0], event.values[1], event.values[2]));
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                TextUserStepsEvent.setText(getResources().getString(R.string.label_event_steps, sensors.step_event()));
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
        if (SensorLight != null) {
            sensorManager.registerListener(this, SensorLight, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, SensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, SensorStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            TextUserSpeed.setText((getResources().getString(R.string.label_speed, "No permission granted for location")));
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            TextUserLightContext.setText("No permission to use mic lol");
            return;
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // Do not save the recordings
        mediaRecorder.setOutputFile("/dev/null");
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
        handler.postDelayed(pollTask,500);
    }

    private Runnable pollTask = new Runnable() {
        @SuppressLint("StringFormatMatches")
        @Override
        public void run() {
            double amplitude = mediaRecorder.getMaxAmplitude();
            // Uncalibrated mic
            int db = (int) (20 * Math.log10(amplitude));
            TextUserSound.setText(getResources().getString(R.string.label_sound, db));
            TextUserSoundContext.setText(getResources().getString(R.string.label_user_sound_context, sensors.sound_context(db)));
            handler.postDelayed(pollTask, 500);
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
        mediaRecorder.stop();
        handler.removeCallbacksAndMessages(pollTask);
    }

    @Override
    public void onLocationChanged(Location location) {
        int speedM = (int) location.getSpeed();
        int speedKM = speedM * (60*60)/1000;
        TextUserSpeed.setText((getResources().getString(R.string.label_speed, String.valueOf(speedM + " m/s or " + speedKM + " km/h"))));
        TextUserSpeedContext.setText((getResources().getString(R.string.label_user_speed_context, SensorActivity.speed_context(speedM))));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

}
