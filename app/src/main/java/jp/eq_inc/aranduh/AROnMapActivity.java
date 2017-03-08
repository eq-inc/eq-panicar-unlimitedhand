package jp.eq_inc.aranduh;

import android.app.FragmentManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import jp.co.thcomp.util.LocationUpdater;
import jp.co.thcomp.util.ToastUtil;
import jp.eq_inc.aranduh.fragment.AROnCameraFragment;

public class AROnMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final float DEFAULT_LATLNG_AREA_SIZE = 0.001f;

    private GoogleMap mGoogleMap;
    private LocationUpdater mLocationUpdater;
    private Location mCurrentLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_on_map);

        FragmentManager manager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) manager.findFragmentByTag(MapFragment.class.getName());
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            manager.beginTransaction().add(R.id.container, mapFragment, MapFragment.class.getName()).commitAllowingStateLoss();
        }
        mapFragment.getMapAsync(this);

        AROnCameraFragment arOnCameraFragment = (AROnCameraFragment) manager.findFragmentByTag(AROnCameraFragment.class.getName());
        if (arOnCameraFragment == null) {
            arOnCameraFragment = AROnCameraFragment.newInstance(View.GONE);
            manager.beginTransaction().add(R.id.container, arOnCameraFragment, AROnCameraFragment.class.getName()).commitAllowingStateLoss();
        }

        mLocationUpdater = new LocationUpdater(this);
        mLocationUpdater.setLocationUpdateAccuracy(Criteria.ACCURACY_FINE);
        mLocationUpdater.setMinLocationDistance(1f);
        mLocationUpdater.setMinLocationUpdateIntervalMs(5000);
        mLocationUpdater.setLocationListener(this);
        mLocationUpdater.startPollingLocation(new LocationUpdater.OnPollingStatusListener() {
            @Override
            public void onChangePollingStatus(LocationUpdater.LocationUpdateStatus status) {
                switch (status) {
                    case Init:
                        break;
                    case NotAllowedPermission:
                        break;
                    case RequestingPermission:
                        break;
                    case UpdatingLocation:
                        ToastUtil.showToast(AROnMapActivity.this, "Location Updating", Toast.LENGTH_SHORT);
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationUpdater.stopPollingLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Google Mapの初期設定
        initializeGoogleMap();

        // 位置情報を更新
        updateMapLocation(mCurrentLocation);
    }

    @Override
    public void onLocationChanged(Location location) {
        // 位置情報を更新
        mCurrentLocation = location;
        updateMapLocation(location);
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
}
