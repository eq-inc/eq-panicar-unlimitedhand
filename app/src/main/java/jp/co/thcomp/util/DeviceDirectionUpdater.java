package jp.co.thcomp.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DeviceDirectionUpdater {
    private static DeviceDirectionUpdater sInstance = null;

    public static synchronized DeviceDirectionUpdater getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DeviceDirectionUpdater(context);
        }

        return sInstance;
    }

    public interface OnDeviceDirectionChangedListener {
        void OnDeviceDirectionChanged(float azimuthDegree, float pitchDegree, float rollDegree);
    }

    private final static double RAD2DEG = 180 / Math.PI;
    private final static double DEFAULT_MINIMUM_THRESHOLD = 2 * Math.PI / 36;   // 10degree

    private static final int STATUS_INIT = 0;
    private static final int STATUS_CHECKING = 1;
    private static final int DEVICE_DIMENSION = 3;

    private static final int INDEX_AZIMUTH = 0;
    private static final int INDEX_PITCH = 1;
    private static final int INDEX_ROLL = 2;

    private Context mContext;
    private HashMap<OnDeviceDirectionChangedListener, Integer> mDeviceDirectionChangedListenerMap = new HashMap<OnDeviceDirectionChangedListener, Integer>();
    private int mCurrentSensorDelay = Integer.MAX_VALUE;
    private int mStatus = STATUS_INIT;
    private SensorManager mSensorManager;
    private float[] mRotationMatrix = new float[DEVICE_DIMENSION * DEVICE_DIMENSION];
    private float[] mGravity = null;
    private float[] mGeomagnetic = null;
    private float[] mAttitude = null;

    public DeviceDirectionUpdater(Context context) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }
        mContext = context;
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
            } else {
                int newSensorDelay = getFastestSensorDelay();
                if (newSensorDelay != mCurrentSensorDelay) {
                    // ユーザ要求が一致しないので、取得頻度を変更（取得する頻度が上がることがないはずだが、念のため不一致で判断）
                    stopSensing();
                    startSensing();
                }
            }
        }
    }

    private void startSensing() {
        Sensor magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor acceleroMeterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mCurrentSensorDelay = getFastestSensorDelay();
        mSensorManager.registerListener(mSensorEventListener, magneticSensor, mCurrentSensorDelay);
        mSensorManager.registerListener(mSensorEventListener, acceleroMeterSensor, mCurrentSensorDelay);

        mStatus = STATUS_CHECKING;
    }

    private void stopSensing() {
        mSensorManager.unregisterListener(mSensorEventListener);
        mStatus = STATUS_INIT;
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
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    mGravity = event.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mGeomagnetic = event.values.clone();
                    break;
            }

            if ((mGravity != null) && (mGeomagnetic != null)) {
                float[] tempAttitude = new float[DEVICE_DIMENSION];
                SensorManager.getRotationMatrix(mRotationMatrix, null, mGravity, mGeomagnetic);
                SensorManager.getOrientation(mRotationMatrix, tempAttitude);

                if ((mAttitude == null) || Math.abs(mAttitude[0] - tempAttitude[0]) > DEFAULT_MINIMUM_THRESHOLD) {
                    mAttitude = tempAttitude;
                    ThreadUtil.runOnMainThread(mContext, mNotifyDeviceDirectionRunnable);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //  処理なし
        }
    };

    private Runnable mNotifyDeviceDirectionRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mDeviceDirectionChangedListenerMap) {
                float[] tempAttitude = mAttitude.clone();
                for (int i = 0, size = tempAttitude.length; i < size; i++) {
                    tempAttitude[i] = (float) (tempAttitude[i] * RAD2DEG);
                }

                for (OnDeviceDirectionChangedListener listener : mDeviceDirectionChangedListenerMap.keySet()) {
                    listener.OnDeviceDirectionChanged(tempAttitude[INDEX_AZIMUTH], tempAttitude[INDEX_PITCH], tempAttitude[INDEX_ROLL]);
                }
            }
        }
    };
}
