package jp.eq_inc.aranduh;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.maps.MapFragment;

import jp.eq_inc.aranduh.fragment.AROnCameraFragment;

public class AROnMapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager manager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) manager.findFragmentByTag(MapFragment.class.getName());
        if(mapFragment == null){
            mapFragment = MapFragment.newInstance();
            manager.beginTransaction().add(R.id.container, mapFragment, MapFragment.class.getName()).commitAllowingStateLoss();
        }

        AROnCameraFragment arOnCameraFragment = (AROnCameraFragment) manager.findFragmentByTag(AROnCameraFragment.class.getName());
        if(arOnCameraFragment == null){
            arOnCameraFragment = AROnCameraFragment.newInstance(View.GONE);
            manager.beginTransaction().add(R.id.container, arOnCameraFragment, AROnCameraFragment.class.getName()).commitAllowingStateLoss();
        }
    }
}
