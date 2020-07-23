package com.example.ContextFromSensors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
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

    // Fields to present sensor outputs and contexts
    private TextView TextSensorLight;
    private TextView TextSensorSpeed;
    private TextView TextSensorSound;
    private TextView TextSensorVelocity;

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
    during single fall event that could potentially result in false fall counter (too high). */
    static long lastTime = 0;
    static long interval = 250; // milliseconds

    private int speedM;
    private int speedKM;

    private PopupWindow myPopUpWindow;
    private TextView TextDescription;

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        contextLayout = findViewById(R.id.context_layout);
        sensorLayout = findViewById(R.id.sensor_layout);

        // Tab in the upper corner of the GUI used to switch between context and sensor reading layouts.
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

        // PopUp window.
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.activity_popup,null);
        myPopUpWindow = new PopupWindow(popupView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        TextDescription = myPopUpWindow.getContentView().findViewById(R.id.popup_description);

        // Context TextViews.
        TextUserLightContext = findViewById(R.id.label_user_light_context);
        TextUserLightContext.setText(getResources().getString(R.string.label_user_light_context, sensors.getLight()));
        TextUserLightContext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextDescription.setText(getResources().getString((R.string.label_popup_light)));
                myPopUpWindow.showAtLocation(contextLayout,Gravity.CENTER,0,0);
                return false;
            }
        });

        TextUserSpeedContext = findViewById(R.id.label_user_speed_context);
        TextUserSpeedContext.setText(getResources().getString(R.string.label_user_speed_context, sensors.getSpeed()));
        TextUserSpeedContext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextDescription.setText(getResources().getString((R.string.label_popup_speed)));
                myPopUpWindow.showAtLocation(contextLayout,Gravity.CENTER,0,0);
                return false;
            }
        });

        TextUserSoundContext = findViewById(R.id.label_sound_context);
        TextUserSoundContext.setText(getResources().getString(R.string.label_user_sound_context, sensors.getSound()));
        TextUserSoundContext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextDescription.setText(getResources().getString((R.string.label_popup_sound)));
                myPopUpWindow.showAtLocation(contextLayout,Gravity.CENTER,0,0);
                return false;
            }
        });

        // Sensor reading TextViews.
        TextSensorLight = findViewById(R.id.label_light);
        TextSensorLight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextDescription.setText(getResources().getString((R.string.label_popup_sensor_light)));
                myPopUpWindow.showAtLocation(contextLayout,Gravity.CENTER,0,0);
                return false;
            }
        });
        TextSensorSpeed = findViewById(R.id.label_speed);
        TextSensorSpeed.setText(getResources().getString(R.string.label_speed, sensors.getSpeed()));
        TextSensorSpeed.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextDescription.setText(getResources().getString((R.string.label_popup_sensor_speed)));
                myPopUpWindow.showAtLocation(contextLayout,Gravity.CENTER,0,0);
                return false;
            }
        });
        TextSensorSound = findViewById(R.id.label_sound);
        TextSensorSound.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextDescription.setText(getResources().getString((R.string.label_popup_sensor_sound)));
                myPopUpWindow.showAtLocation(contextLayout,Gravity.CENTER,0,0);
                return false;
            }
        });
        TextSensorVelocity = findViewById(R.id.label_velocity);
        TextSensorVelocity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextDescription.setText(getResources().getString((R.string.label_popup_sensor_velocity)));
                myPopUpWindow.showAtLocation(contextLayout,Gravity.CENTER,0,0);
                return false;
            }
        });

        // Event TextViews.
        TextUserFallEvent = findViewById(R.id.label_event_fall);
        TextUserFallEvent.setText(getResources().getString(R.string.label_event_fall, sensors.getFalls()));
        TextUserFallEvent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextDescription.setText(getResources().getString((R.string.label_popup_fall)));
                myPopUpWindow.showAtLocation(contextLayout,Gravity.CENTER,0,0);
                return false;
            }
        });

        TextUserStepsEvent = findViewById(R.id.label_event_steps);
        TextUserStepsEvent.setText(getResources().getString(R.string.label_event_steps, sensors.getSteps()));
        TextUserStepsEvent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextDescription.setText(getResources().getString((R.string.label_popup_steps)));
                myPopUpWindow.showAtLocation(contextLayout,Gravity.CENTER,0,0);
                return false;
            }
        });

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACTIVITY_RECOGNITION
        };
        
        hasPermissions(this, PERMISSION_ALL, PERMISSIONS);
        String error = getResources().getString(R.string.error_no_permission);

        // Check permissions, location will be checked later.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            TextUserSoundContext.setText(error);
            TextSensorSound.setText(error);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            TextUserStepsEvent.setText(error);
            return;
        }

        // Initialize the managers.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Check location permission and request location updates.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            TextSensorSpeed.setText(error);
            TextUserSpeedContext.setText(error);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this);

        mediaRecorder = new MediaRecorder();
        // Start media recorder.
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // Do not save the recordings.
        mediaRecorder.setOutputFile("/dev/null");
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();

        // Initialize the sensors.
        SensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        SensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }

    public void closePopup(View view) {
        myPopUpWindow.dismiss();
    }

    public void hasPermissions(Context context, int requestCode, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, requestCode);
                }
            }
        }
    }

    // Inflate the menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Menu actions.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.viewSensors:
                Intent intent = new Intent(this, SensorList.class);
                startActivity(intent);
                return (true);
            case R.id.exit:
                finishAffinity();
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    // This method runs on sensor reading change.
    @SuppressLint("StringFormatMatches")
    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        switch (sensorType) {
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
                TextSensorVelocity.setText(getResources().getString(R.string.label_velocity, event.values[0], event.values[1], event.values[2]));
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

    // Register all the services.
    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();

        // Register listeners for sensors if available.
        String sensor_error = getResources().getString(R.string.error_no_sensor);
        if (SensorLight != null) {
            sensorManager.registerListener(this, SensorLight, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            TextSensorLight.setText(sensor_error);
            TextUserLightContext.setText(sensor_error);
        }

        if (SensorAccelerometer != null) {
            sensorManager.registerListener(this, SensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            TextSensorVelocity.setText(sensor_error);
            TextUserFallEvent.setText(sensor_error);
        }

        if (SensorStepDetector != null) {
            sensorManager.registerListener(this, SensorStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            TextUserStepsEvent.setText(sensor_error);
        }

        if (mediaRecorder != null) {
            // Start soundTask.
            handler.postDelayed(soundTask,500);
        } else {
            TextUserSoundContext.setText(sensor_error);
            TextSensorSound.setText(sensor_error);
        }
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this);
        } else {
            TextUserSpeedContext.setText(sensor_error);
            TextSensorSpeed.setText(sensor_error);
        }
    }

    // Runnable that is used to monitor environment sound levels with mic.
    private Runnable soundTask = new Runnable() {
        @SuppressLint("StringFormatMatches")
        @Override
        public void run() {
            double amplitude = mediaRecorder.getMaxAmplitude();
            int db = (int) (20 * Math.log10(amplitude));

            TextSensorSound.setText(getResources().getString(R.string.label_sound, db));
            TextUserSoundContext.setText(getResources().getString(R.string.label_user_sound_context, sensors.sound_context(db)));
            handler.postDelayed(soundTask, 500);
        }
    };

    // When app is no longer in foreground of device screen stop the listeners etc. to not consume
    // power in background.
    @Override
    protected void onStop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(soundTask);
        }
        super.onStop();
    }

    // Method used to get user speed when location changes.
    @Override
    public void onLocationChanged(Location location) {
        speedM = (int) location.getSpeed();
        speedKM = speedM * (60*60)/1000;
        TextSensorSpeed.setText((getResources().getString(R.string.label_speed, String.valueOf(speedM + " m/s or " + speedKM + " km/h"))));
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

    public void restart() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}