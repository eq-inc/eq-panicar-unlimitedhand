package jp.co.thcomp.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.HashMap;

public class DeviceDirectionUpdater {
    private static DeviceDirectionUpdater sInstance = null;

    public static synchronized DeviceDirectionUpdater getInstance(Context context) {
        if (sInstance == null) {

        }

        return sInstance;
    }

    public interface OnDeviceDirectionChangedListener {
        void OnDeviceDirectionChanged(float azimuthDegree, float pitchDegree, float rollDegree);
    }

    private static final int STATUS_INIT = 0;
    private static final int STATUS_CHECKING = 1;

    private HashMap<OnDeviceDirectionChangedListener, Integer> mDeviceDirectionChangedListenerMap = new HashMap<OnDeviceDirectionChangedListener, Integer>();
    private int mCurrentSensorDelay = Integer.MAX_VALUE;
    private int mStatus = STATUS_INIT;
    private SensorManager mSensorManager;

    public DeviceDirectionUpdater(Context context) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void start(OnDeviceDirectionChangedListener listener, int sensorDelay) {
        synchronized (mDeviceDirectionChangedListenerMap) {
            mDeviceDirectionChangedListenerMap.put(listener, sensorDelay);

            if (mStatus == STATUS_INIT) {
                // センシング開始
                startSensing();
            } else {
                int newSensorDelay = getFastestSensorDelay();
                if (newSensorDelay < mCurrentSensorDelay) {
                    // センシング間隔を変更
                    stopSensing();
                    startSensing();
                }
            }
        }
    }

    public void stop(OnDeviceDirectionChangedListener listener) {
        synchronized (mDeviceDirectionChangedListenerMap) {
            mDeviceDirectionChangedListenerMap.remove(listener);

            if (mDeviceDirectionChangedListenerMap.size() == 0) {
                // センシング停止
                stopSensing();
            }
        }
    }

    private void startSensing() {
        Sensor magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor acceleroMeterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mCurrentSensorDelay = getFastestSensorDelay();
        mSensorManager.registerListener(mSensorEventListener, magneticSensor, mCurrentSensorDelay);
        mSensorManager.registerListener(mSensorEventListener, acceleroMeterSensor, mCurrentSensorDelay);
    }

    private void stopSensing() {
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    private int getFastestSensorDelay() {
        int ret = Integer.MAX_VALUE;

        if (mDeviceDirectionChangedListenerMap.size() > 0) {
            for (Integer sensorDelay : mDeviceDirectionChangedListenerMap.values()) {
                if (sensorDelay < ret) {
                    ret = sensorDelay;
                }
            }
        }

        return ret;
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
