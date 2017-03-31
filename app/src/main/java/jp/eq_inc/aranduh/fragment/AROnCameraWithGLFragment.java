package jp.eq_inc.aranduh.fragment;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import com.dopanic.panicarkit.lib.PARCameraView;
import com.dopanic.panicarkit.lib.PARController;

import jp.co.thcomp.glsurfaceview.GLCylinder;
import jp.co.thcomp.glsurfaceview.GLDrawView;
import jp.co.thcomp.glsurfaceview.RotateInfo;
import jp.co.thcomp.util.DeviceDirectionUpdater;
import jp.eq_inc.aranduh.R;
import jp.eq_inc.aranduh.poi.POIGLDrawView;

public class AROnCameraWithGLFragment extends AROnCameraFragment {
    protected static final int CylinderNormalColor = 0x0000FF00;
    protected static final int CylinderDischagingColor = 0x00FF0000;
    protected static final float CylinderNormalColorR[] = {((float) ((CylinderNormalColor & 0x00FF0000) >> 16)) / 0xFF};
    protected static final float CylinderNormalColorG[] = {((float) ((CylinderNormalColor & 0x0000FF00) >> 8)) / 0xFF};
    protected static final float CylinderNormalColorB[] = {((float) ((CylinderNormalColor & 0x000000FF) >> 0)) / 0xFF};
    protected static final float CylinderDischagingColorR[] = {((float) ((CylinderDischagingColor & 0x00FF0000) >> 16)) / 0xFF};
    protected static final float CylinderDischagingColorG[] = {((float) ((CylinderDischagingColor & 0x0000FF00) >> 8)) / 0xFF};
    protected static final float CylinderDischagingColorB[] = {((float) ((CylinderDischagingColor & 0x000000FF) >> 0)) / 0xFF};
    protected static final float CylinderColorA[] = {1.0f};

    private static final String TAG = AROnCameraWithGLFragment.class.getSimpleName();

    private GLDrawView mDrawView;
    private GLCylinder mCylinder;
    private RotateInfo mRotateInfo;
    private DeviceDirectionUpdater mDeviceDirectionUpdater;

    public static AROnCameraWithGLFragment newInstance(int cameraVisibility) {
        AROnCameraWithGLFragment ret = new AROnCameraWithGLFragment();
        Bundle argumentBundle = new Bundle();

        argumentBundle.putInt("cameraVisibility", cameraVisibility);
        ret.setArguments(argumentBundle);

        return ret;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = super.onCreateView(inflater, container, savedInstanceState);

        PARCameraView cameraView = (PARCameraView) ret.findViewWithTag("arCameraView");
        cameraView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cameraView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mDeviceDirectionUpdater = DeviceDirectionUpdater.getInstance(getActivity());

        return ret;
    }

    @Override
    public void onResume() {
        super.onResume();
        mDeviceDirectionUpdater.start(mDeviceDirectChangedListener, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mDeviceDirectionUpdater.stop(mDeviceDirectChangedListener);
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
                            if (mDrawView != null) {
                                mCylinder.setColors(CylinderNormalColorR, CylinderNormalColorG, CylinderNormalColorB, CylinderColorA);
                                mDrawView.requestRender();
                            }
                            sendNextMessage();
                            break;
                        case DISCHARGING_EEL:
                            mEelstatus = eelStatus;
                            // TODO 放電状態への変更を設定
                            if (mDrawView != null) {
                                mCylinder.setColors(CylinderDischagingColorR, CylinderDischagingColorG, CylinderDischagingColorB, CylinderColorA);
                                mDrawView.requestRender();
                            }
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
                    mFirstLocation.setLatitude(mFirstLocation.getLatitude() + 0.01);
                    mFirstLocation.setLongitude(mFirstLocation.getLongitude() + 0.01);

                    mPOI = new POIGLDrawView(mFirstLocation);
                    ((POIGLDrawView) mPOI).setOnPrepareDrawListener(new POIGLDrawView.OnPrepareDrawListener() {
                        @Override
                        public void onPrepareDrawView(GLDrawView drawView) {
                            mDrawView = drawView;
                            Resources resources = getResources();
                            int canvasWidth = resources.getDimensionPixelSize(R.dimen.droid_3dcg_width);
                            int canvasHeight = resources.getDimensionPixelSize(R.dimen.droid_3dcg_height);

                            mCylinder = new GLCylinder(drawView);
                            mCylinder.setCylinderInfo(
                                    canvasWidth / 2,
                                    canvasHeight / 2 - canvasHeight / 4,
                                    0,
                                    canvasHeight / 4,
                                    canvasHeight / 2);
                            mCylinder.setColor(Color.MAGENTA);
                            mRotateInfo = mCylinder.addRotation(0, canvasWidth / 2, canvasHeight / 2, 0);
                            drawView.addDrawParts(mCylinder);
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

    private DeviceDirectionUpdater.OnDeviceDirectionChangedListener mDeviceDirectChangedListener = new DeviceDirectionUpdater.OnDeviceDirectionChangedListener() {
        @Override
        public void OnDeviceDirectionChanged(float azimuthDegree, float pitchDegree, float rollDegree) {
            if (mCylinder != null) {
                mRotateInfo.rotateDegree = azimuthDegree % 30;
                mDrawView.requestRender();
            }
        }
    };
}
