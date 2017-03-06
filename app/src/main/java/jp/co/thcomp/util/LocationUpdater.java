package jp.co.thcomp.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;

public class LocationUpdater {
    private static final long DEFAULT_MIN_LOCATION_UPDATE_INTERVAL_MS = 10 * 1000;
    private static final float DEFAULT_MIN_LOCATION_DISTANCE_METER = 5f;
    private static Location sLastCurrentLocation;

    public interface OnPollingStatusListener{
        void onChangePollingStatus(LocationUpdateStatus status);
    }

    public enum LocationUpdateStatus {
        Init,
        RequestingPermission,
        NotAllowedPermission,
        UpdatingLocation,
    }

    private Activity mActivity;
    private LocationManager mLocationManager;
    private LocationListener mUserLocationListener = null;
    private OnPollingStatusListener mPollingStatusListener = null;
    private LocationUpdateStatus mUpdateStatus = LocationUpdateStatus.Init;
    private Criteria mLocationUpdateCriteria = null;
    private long mMinLocationUpdateIntervalMS = DEFAULT_MIN_LOCATION_UPDATE_INTERVAL_MS;
    private float mMinLocationDistanceMeter = DEFAULT_MIN_LOCATION_DISTANCE_METER;
    private HandlerThread mLocationUpdateHandlerThread = null;

    public LocationUpdater(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("activity == null");
        }
        mActivity = activity;
        mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        mLocationUpdateCriteria = new Criteria();
        mLocationUpdateCriteria.setAccuracy(Criteria.NO_REQUIREMENT);
        mLocationUpdateCriteria.setPowerRequirement(Criteria.POWER_LOW);
    }

    public void setMinLocationUpdateIntervalMs(long minLocationUpdateIntervalMs) {
        mMinLocationUpdateIntervalMS = minLocationUpdateIntervalMs;
    }

    public void setMinLocationDistance(float minLocationDistanceMeter) {
        mMinLocationDistanceMeter = minLocationDistanceMeter;
    }

    public void setLocationUpdateCriteria(Criteria criteria) {
        mLocationUpdateCriteria.setAccuracy(criteria.getAccuracy());
        mLocationUpdateCriteria.setBearingAccuracy(criteria.getBearingAccuracy());
        mLocationUpdateCriteria.setHorizontalAccuracy(criteria.getHorizontalAccuracy());
        mLocationUpdateCriteria.setPowerRequirement(criteria.getPowerRequirement());
        mLocationUpdateCriteria.setSpeedAccuracy(criteria.getSpeedAccuracy());
        mLocationUpdateCriteria.setVerticalAccuracy(criteria.getVerticalAccuracy());
    }

    public long getMinLocationUpdateIntervalMs() {
        return mMinLocationUpdateIntervalMS;
    }

    public float getMinLocationDistance() {
        return mMinLocationDistanceMeter;
    }

    public void startPollingLocation(OnPollingStatusListener listener) {
        if (mUpdateStatus == LocationUpdateStatus.Init) {
            mPollingStatusListener = listener;
            mUpdateStatus = LocationUpdateStatus.RequestingPermission;
            if(mPollingStatusListener != null) {
                ThreadUtil.runOnMainThread(mActivity, new Runnable() {
                    @Override
                    public void run() {
                        mPollingStatusListener.onChangePollingStatus(LocationUpdateStatus.RequestingPermission);
                    }
                });
            }

            RuntimePermissionUtil.requestPermissions(
                    mActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    new RuntimePermissionUtil.OnRequestPermissionsResultListener() {
                        @Override
                        public void onRequestPermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
                            boolean allGetPermissions = false;

                            if (grantResults != null && grantResults.length > 0) {
                                for (int result : grantResults) {
                                    if (result == PackageManager.PERMISSION_GRANTED) {
                                        allGetPermissions = true;
                                    } else {
                                        allGetPermissions = false;
                                        break;
                                    }
                                }
                            }

                            if (allGetPermissions) {
                                mUpdateStatus = LocationUpdateStatus.UpdatingLocation;

                                // 一旦停止させて、意図せずして残っているリソースを解放する
                                stopPollingLocation();
                                mLocationUpdateHandlerThread = new HandlerThread(LocationUpdater.class.getSimpleName(), Thread.MIN_PRIORITY);
                                mLocationUpdateHandlerThread.start();

                                try {
                                    mLocationManager.requestLocationUpdates(mMinLocationUpdateIntervalMS, mMinLocationDistanceMeter, mLocationUpdateCriteria, mUpdateLocationListener, mLocationUpdateHandlerThread.getLooper());
                                } catch (SecurityException e) {
                                    mUpdateStatus = LocationUpdateStatus.Init;
                                    if(mPollingStatusListener != null) {
                                        ThreadUtil.runOnMainThread(mActivity, new Runnable() {
                                            @Override
                                            public void run() {
                                                mPollingStatusListener.onChangePollingStatus(LocationUpdateStatus.NotAllowedPermission);
                                            }
                                        });
                                    }
                                }
                            } else {
                                mUpdateStatus = LocationUpdateStatus.Init;
                                if(mPollingStatusListener != null) {
                                    ThreadUtil.runOnMainThread(mActivity, new Runnable() {
                                        @Override
                                        public void run() {
                                            mPollingStatusListener.onChangePollingStatus(LocationUpdateStatus.NotAllowedPermission);
                                        }
                                    });
                                }
                            }
                        }
                    }
            );
        }
    }

    public void stopPollingLocation() {
        if(mLocationUpdateHandlerThread != null){
            try {
                mLocationManager.removeUpdates(mUpdateLocationListener);
            }catch (SecurityException e){
            }

            mLocationUpdateHandlerThread.quit();
            mLocationUpdateHandlerThread = null;

            mUpdateStatus = LocationUpdateStatus.Init;
        }
    }

    public void setLocationListener(LocationListener listener) {
        mUserLocationListener = listener;
    }

    public void removeLocationListener(LocationListener listener) {
        mUserLocationListener = null;
    }

    public Location getCurrentLocation() {
        return sLastCurrentLocation;
    }

    private LocationListener mUpdateLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            sLastCurrentLocation = location;
            if (mUserLocationListener != null) {
                mUserLocationListener.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (mUserLocationListener != null) {
                mUserLocationListener.onStatusChanged(provider, status, extras);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (mUserLocationListener != null) {
                mUserLocationListener.onProviderEnabled(provider);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (mUserLocationListener != null) {
                mUserLocationListener.onProviderDisabled(provider);
            }
        }
    };
}
