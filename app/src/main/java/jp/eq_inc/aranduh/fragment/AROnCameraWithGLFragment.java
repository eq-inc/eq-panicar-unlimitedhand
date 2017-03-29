package jp.eq_inc.aranduh.fragment;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dopanic.panicarkit.lib.PARController;

import jp.co.thcomp.droidsearch3d.Droid3DCG;
import jp.co.thcomp.glsurfaceview.GLDrawView;
import jp.eq_inc.aranduh.poi.POIGLDrawView;

public class AROnCameraWithGLFragment extends AROnCameraFragment {
    private static final String TAG = AROnCameraWithGLFragment.class.getSimpleName();

    private Droid3DCG mDroid;

    public static AROnCameraWithGLFragment newInstance(int cameraVisibility) {
        AROnCameraWithGLFragment ret = new AROnCameraWithGLFragment();
        Bundle argumentBundle = new Bundle();

        argumentBundle.putInt("cameraVisibility", cameraVisibility);
        ret.setArguments(argumentBundle);

        return ret;
    }

    @Override
    protected Handler.Callback getRenderHandlerCallback() {
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (mPOI != null) {
                    POIGLDrawView poiDrawView = (POIGLDrawView) mPOI;

                    EEL_STATUS eelStatus = EEL_STATUS.values()[msg.what];
                    switch (eelStatus) {
                        case NORMAL_EEL:
                            mEelstatus = eelStatus;
                            // TODO ノーマル状態への変更を設定
                            sendNextMessage();
                            break;
                        case DISCHARGING_EEL:
                            mEelstatus = eelStatus;
                            // TODO 放電状態への変更を設定
                            sendNextMessage();
                            break;
                        case MOVING_EEL:
                            if (mSwtMovingPOI.isChecked()) {
                                // MOVING_EELの場合はステータスは変更しないで元のままにする（変更したら放電状態が失われてしまうため）
                                updatePosition(mPOI);
                            } else {
                                // 動かす必要がないときは、再度メッセージを送信
                                sendNextMessage();
                            }
                            break;
                    }
                }

                return true;
            }
        };
    }

    @Override
    protected LocationListener getLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (mFirstLocation == null) {
                    mFirstLocation = new Location(location);

                    // show POI near first location
                    //mPOI = new POISurfaceView(mFirstLocation, new ElectricEelRenderer(getActivity()));
                    mFirstLocation.setLatitude(mFirstLocation.getLatitude() + 0.01);
                    mFirstLocation.setLongitude(mFirstLocation.getLongitude() + 0.01);

                    mPOI = new POIGLDrawView(mFirstLocation);
                    ((POIGLDrawView) mPOI).setOnPrepareDrawListener(new POIGLDrawView.OnPrepareDrawListener() {
                        @Override
                        public void onPrepareDrawView(GLDrawView drawView) {
                            mDroid = new Droid3DCG(drawView);
                            drawView.addDrawParts(mDroid);
                            ((POIGLDrawView) mPOI).setOnTouchListener(mPOITouchListener);
                            sendNextMessage();
                        }
                    });
                    PARController.getInstance().addPoi(mPOI);
                }
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
        };
    }
}
