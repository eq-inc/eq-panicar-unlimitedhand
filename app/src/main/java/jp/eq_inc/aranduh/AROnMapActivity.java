package jp.eq_inc.aranduh;

import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import jp.co.thcomp.util.DeviceDirectionUpdater;
import jp.co.thcomp.util.LocationUpdater;
import jp.co.thcomp.util.RuntimePermissionUtil;
import jp.co.thcomp.util.ToastUtil;
import jp.eq_inc.aranduh.fragment.AROnCameraFragment;

public class AROnMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, DeviceDirectionUpdater.OnDeviceDirectionChangedListener, CompoundButton.OnCheckedChangeListener {
    private static final int MINIMUM_LOCATION_UPDATE_INTERVAL_MS = 5000;
    private GoogleMap mGoogleMap;
    private LocationUpdater mLocationUpdater;
    private Location mCurrentLocation;
    private SwitchCompat mSwtChangeMode;
    private MapFragment mMapFragment;
    private AROnCameraFragment mAROnCameraFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RuntimePermissionUtil.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA},
                mRuntimePermissionResultListener);

        DeviceDirectionUpdater.getInstance(this).start(this, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationUpdater != null) {
            mLocationUpdater.stopPollingLocation();
            mLocationUpdater = null;
        }
        DeviceDirectionUpdater.getInstance(this).stop(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RuntimePermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Google Mapの初期設定
        initializeGoogleMap();

        // 位置情報を更新
        updateMapLocation(mCurrentLocation);
    }

    // LocationListener START
    @Override
    public void onLocationChanged(Location location) {
        // 位置情報を更新
        mCurrentLocation = location;
        updateMapLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // 処理なし
    }

    @Override
    public void onProviderEnabled(String provider) {
        // 処理なし
    }

    @Override
    public void onProviderDisabled(String provider) {
        // 処理なし
    }
    // LocationListener END

    // DeviceDirectionUpdater.OnDeviceDirectionChangedListener START
    @Override
    public void OnDeviceDirectionChanged(float azimuthDegree, float pitchDegree, float rollDegree) {
        if (mGoogleMap != null && mCurrentLocation != null) {
            CameraPosition currentCameraPosition = mGoogleMap.getCameraPosition();
            if (currentCameraPosition != null) {
                CameraPosition cameraPosition = CameraPosition.builder().target(currentCameraPosition.target).tilt(currentCameraPosition.tilt).bearing(azimuthDegree).zoom(mGoogleMap.getMaxZoomLevel()).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }
    // DeviceDirectionUpdater.OnDeviceDirectionChangedListener END

    // CompoundButton.OnCheckedChangeListener START
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            // ARを有効にする
            mAROnCameraFragment.setCameraViewVisibility(View.VISIBLE);
            mMapFragment.getView().setVisibility(View.GONE);
        } else {
            // Mapを有効にする
            mAROnCameraFragment.setCameraViewVisibility(View.GONE);
            mMapFragment.getView().setVisibility(View.VISIBLE);
        }
    }
    // CompoundButton.OnCheckedChangeListener END

    private void initializeGoogleMap() {
        mGoogleMap.setBuildingsEnabled(false);
        mGoogleMap.setIndoorEnabled(false);
        mGoogleMap.setTrafficEnabled(false);

        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    private void updateMapLocation(Location location) {
        if ((location != null) && (mGoogleMap != null)) {
            CameraPosition cameraPosition = CameraPosition.builder().target(new LatLng(location.getLatitude(), location.getLongitude())).tilt(90f).zoom(mGoogleMap.getMaxZoomLevel()).build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private RuntimePermissionUtil.OnRequestPermissionsResultListener mRuntimePermissionResultListener = new RuntimePermissionUtil.OnRequestPermissionsResultListener() {
        @Override
        public void onRequestPermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
            boolean enableLaunch = false;

            if (grantResults != null && grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        enableLaunch = true;
                    } else {
                        enableLaunch = false;
                        break;
                    }
                }
            }

            if (enableLaunch) {
                setContentView(R.layout.activity_ar_on_map);

                mSwtChangeMode = (SwitchCompat) findViewById(R.id.swtChangeMapAndAR);
                mSwtChangeMode.setOnCheckedChangeListener(AROnMapActivity.this);
                FragmentManager manager = getFragmentManager();
                mMapFragment = (MapFragment) manager.findFragmentByTag(MapFragment.class.getName());
                if (mMapFragment == null) {
                    mMapFragment = MapFragment.newInstance();
                    manager.beginTransaction().add(R.id.container, mMapFragment, MapFragment.class.getName()).commitAllowingStateLoss();
                }
                mMapFragment.getMapAsync(AROnMapActivity.this);

                mAROnCameraFragment = (AROnCameraFragment) manager.findFragmentByTag(AROnCameraFragment.class.getName());
                if (mAROnCameraFragment == null) {
                    mAROnCameraFragment = AROnCameraFragment.newInstance(View.GONE);
                    manager.beginTransaction().add(R.id.container, mAROnCameraFragment, AROnCameraFragment.class.getName()).commitAllowingStateLoss();
                }

                mLocationUpdater = new LocationUpdater(AROnMapActivity.this);
                mLocationUpdater.setLocationUpdateAccuracy(Criteria.ACCURACY_FINE);
                mLocationUpdater.setMinLocationDistance(1f);
                mLocationUpdater.setMinLocationUpdateIntervalMs(MINIMUM_LOCATION_UPDATE_INTERVAL_MS);
                mLocationUpdater.setLocationListener(AROnMapActivity.this);
                mLocationUpdater.startPollingLocation(null);
            } else {
                ToastUtil.showToast(AROnMapActivity.this, "cannot launch by not allowed permissions", Toast.LENGTH_LONG);
                finish();
            }
        }
    };
}
