package com.example.ContextFromSensors;

import android.hardware.SensorEvent;

public class SensorActivity  {

    private static String initial_sensor_status = "{INITIALIZING}";
    // Stored variables
    static String speed_context = initial_sensor_status;
    static String sound_context = initial_sensor_status;
    static String light_context = initial_sensor_status;
    static int falls = 0;
    static int steps = 0;

    /*
    Turn lux-value into information about the users lightning context
    using following data set
    https://docs.microsoft.com/en-us/windows/win32/sensorsapi/understanding-and-interpreting-lux-values
     */

    static String light_context(int value) {
        light_context = "Value not in range";
        // do nothing
        if (value <= 10) {
            light_context = "pitch black";
        } else if (value <= 50) {
            light_context = "very dark";
        } else if (value <= 200) {
            light_context = "dark indoors";
        } else if (value <= 400) {
            light_context = "dim indoors";
        } else if (value <= 1000) {
            light_context = "normal indoors";
        } else if (value <= 5000) {
            light_context = "bright indoors";
        } else if (value <= 10000) {
            light_context = "dim outdoors";
        } else if (value <= 30000) {
            light_context = "cloudy outdoors";
        } else if (value <= 100000) {
            light_context = "direct sunlight";
        }
        return light_context;
    }

    static String getLight() {
        return light_context;
    }

    /*
    X, Y, Z axis and the linear acceleration in each axis is in event values as m/s^2
     */
    String velocity_context(SensorEvent event) {
        float linear_accelerationX = event.values[0];
        float linear_accelerationY = event.values[1];
        float linear_accelerationZ = event.values[2];

        // If any axis is negative (decreasing velocity), make it positive
        if (linear_accelerationY < 0) {
            linear_accelerationY = linear_accelerationY*-1;
        } else if (linear_accelerationX < 0) {
            linear_accelerationX = linear_accelerationX*-1;
        } else if (linear_accelerationZ < 0) {
            linear_accelerationZ = linear_accelerationZ*-1;
        }

        if (linear_accelerationY >= 0.5 && linear_accelerationY < 1.5 ||
                linear_accelerationX >= 0.5 && linear_accelerationX < 1.5 ||
                linear_accelerationZ >= 0.5 && linear_accelerationZ < 1.5) {
        } else if (linear_accelerationY >= 1.5 && linear_accelerationY < 6 ||
                linear_accelerationX >= 1.5 && linear_accelerationX < 6 ||
                linear_accelerationZ >= 1.5 && linear_accelerationZ < 6) {
        } else {
        }

        return "y";
        // String.valueOf(linear_accelerationY)
    }
    /*
    Here the speed in m/s is turned into information of users movement, such as walking or being in
    a motored vehicle by using the following list
    https://en.wikipedia.org/wiki/Orders_of_magnitude_(speed)
     */
    static String speed_context(int speed) {

        if (speed > 0 && speed < 2) {
            speed_context = "walking";
        } else if (speed >= 2 && speed < 6) {
            speed_context = "running";
        } else if (speed >= 6 && speed < 8) {
            speed_context = "riding a bike";
        } else if (speed > 8) {
            speed_context = "in a motored vehicle";
        } else {
            speed_context = "standing still, if you are not, you might have a weak GPS connection";
        }
        return speed_context;
    }
    static String getSpeed() {
        return speed_context;
    }

    static String sound_context (int db) {

        if (db <= 20) {
            sound_context = "faint";
        } else if (db < 20 && db <= 40) {
            sound_context = "soft";
        } else if (db < 40 && db <= 60) {
            sound_context = "moderate";
        } else if (db < 60 && db <= 80) {
            sound_context = "loud";
        } else if (db < 80 && db <= 110) {
            sound_context = "very loud";
        } else if (db < 110 && db <= 120) {
            sound_context = "uncomfortable";
        } else {
            sound_context = "painful & dangerous";
        }
        return sound_context;
    }
    static String getSound() {
        return sound_context;
    }

    static int fall_event(double X, double Y, double Z) {
        double loAccelerationReader = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2) + Math.pow(Z, 2));
        if (loAccelerationReader > 0.3d && loAccelerationReader < 0.5d) {
            falls++;
        }
        return falls;
    }
    static int getFalls() {
        return falls;
    }

    static int step_event() {
        steps++;
        return steps;
    }
    static int getSteps() {
        return steps;
    }
}