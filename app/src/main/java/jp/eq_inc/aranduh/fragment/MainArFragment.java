package jp.eq_inc.aranduh.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dopanic.panicarkit.lib.PARController;
import com.dopanic.panicarkit.lib.PARFragment;
import com.dopanic.panicarkit.lib.PARPoi;

import jp.co.thcomp.unlimitedhand.UhAccessHelper;
import jp.co.thcomp.util.Constant;
import jp.co.thcomp.util.LocationUpdater;
import jp.co.thcomp.util.LogUtil;
import jp.co.thcomp.util.ToastUtil;
import jp.eq_inc.aranduh.R;
import jp.eq_inc.aranduh.poi.POIImageView;

public class MainArFragment extends PARFragment {
    private static final String TAG = MainArFragment.class.getSimpleName();
    private static final int NORMAL_EEL = 0;
    private static final int DISCHARGING_EEL = 1;
    private static final int[] DISCHARGING_EEL_CHANNELS = {/*0, 1, */2, 3, 4, 5, /*6, 7*/};

    private LocationUpdater mLocationUpdater;
    private Location mFirstLocation;
    private PARPoi mPOI;
    private Handler mMainLooperHandler;
    private UhAccessHelper mUHAccessHelper;
    private int mEelstatus = NORMAL_EEL;
    private boolean mDischarging = false;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        LogUtil.logoutput(Constant.LOG_SWITCH.LOG_SWITCH_ERROR | Constant.LOG_SWITCH.LOG_SWITCH_WARNING | Constant.LOG_SWITCH.LOG_SWITCH_INFO | Constant.LOG_SWITCH.LOG_SWITCH_DEBUG);
        mLocationUpdater = new LocationUpdater(getActivity());
        mLocationUpdater.setLocationListener(mLocationListener);
        mMainLooperHandler = new Handler(Looper.getMainLooper(), mRenderHandlerCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.viewLayoutId = R.layout.fragment_main;
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getRadarView().setRadarRange(500);

        UHConnectTask task = new UHConnectTask();
        task.execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocationUpdater.startPollingLocation(mPollingStatusListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationUpdater.stopPollingLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mUHAccessHelper != null) {
            mUHAccessHelper.disconnect();
            mUHAccessHelper = null;
        }
        PARController.getInstance().removeObject(mPOI);
        mPOI = null;
    }

    private void sendNextMessage() {
        int nextStatus = ((int) (Math.random() * 1000)) % 2;
        int delayMS = (((int) (Math.random() * 1000)) % 10) * 1000;
        mMainLooperHandler.sendEmptyMessageDelayed(nextStatus, delayMS);
    }

    private LocationUpdater.OnPollingStatusListener mPollingStatusListener = new LocationUpdater.OnPollingStatusListener() {
        @Override
        public void onChangePollingStatus(LocationUpdater.LocationUpdateStatus status) {
            if (status == LocationUpdater.LocationUpdateStatus.NotAllowedPermission) {
                ToastUtil.showToast(getActivity(), "cannot get location by not allowed permissions", Toast.LENGTH_LONG);
            }
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mFirstLocation == null) {
                mFirstLocation = new Location(location);

                // show POI near first location
                //mPOI = new POISurfaceView(mFirstLocation, new ElectricEelRenderer(getActivity()));
                mFirstLocation.setLatitude(mFirstLocation.getLatitude() + 0.01);
                mFirstLocation.setLongitude(mFirstLocation.getLongitude() + 0.01);
                mPOI = new POIImageView(mFirstLocation);
                ((POIImageView) mPOI).setOnTouchListener(mPOITouchListener);
                ((POIImageView) mPOI).setImageResource(R.drawable.eel);
                PARController.getInstance().addPoi(mPOI);
                sendNextMessage();
            }
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
    };

    private View.OnTouchListener mPOITouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean ret = false;

            if (mEelstatus == DISCHARGING_EEL) {
                int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if (!mDischarging) {
                            mDischarging = true;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    UhAccessHelper uhAccessHelper = mUHAccessHelper;
                                    Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                    if (uhAccessHelper != null) {
                                        // ボリュームを触れるたびに上げていく
                                        uhAccessHelper.upVoltageLevel();
                                    }

                                    while (mDischarging && (mEelstatus == DISCHARGING_EEL)) {
                                        LogUtil.d(TAG, "electricMuscleStimulation start");

                                        for (int channel : DISCHARGING_EEL_CHANNELS) {
                                            if (mDischarging && (mEelstatus == DISCHARGING_EEL)) {
                                                if (uhAccessHelper != null) {
                                                    uhAccessHelper.electricMuscleStimulation(channel);
                                                } else {
                                                    vibrator.vibrate(100);
                                                }
                                                try {
                                                    Thread.sleep(100);
                                                } catch (InterruptedException e) {
                                                }
                                            } else {
                                                break;
                                            }
                                        }
                                        LogUtil.d(TAG, "electricMuscleStimulation end");
                                    }
                                    mDischarging = false;
                                }
                            }).start();
                            ret = true;
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        mDischarging = false;
                        LogUtil.d(TAG, "mDischarging = false");
                        ret = true;
                        break;
                }
            }

            return ret;
        }
    };

    private Handler.Callback mRenderHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mPOI != null) {
                POIImageView poiImageView = (POIImageView) mPOI;

                mEelstatus = msg.what;
                switch (mEelstatus) {
                    case NORMAL_EEL:
                        poiImageView.setImageResourceForOverContent(null);
                        break;
                    case DISCHARGING_EEL:
                        poiImageView.setImageResourceForOverContent(R.drawable.electric);
                        break;
                }
                sendNextMessage();
            }

            return true;
        }
    };

    private class UHConnectTask extends AsyncTask<Void, Void, UhAccessHelper.ConnectResult> {
        private ProgressDialog mConnectingDialog;
        private boolean mUseUH = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Activity activity = getActivity();
            mUHAccessHelper = new UhAccessHelper(activity);

            mConnectingDialog = new ProgressDialog(MainArFragment.this.getActivity());
            mConnectingDialog.setMessage("Search and connect to Unlimited Hand");
            mConnectingDialog.setCanceledOnTouchOutside(false);
            mConnectingDialog.setCancelable(true);
            mConnectingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    UHConnectTask.this.cancel(true);
                    mUseUH = false;
                    mUHAccessHelper = null;
                }
            });
            mConnectingDialog.show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mUseUH = false;
            mUHAccessHelper = null;
            if (mConnectingDialog != null) {
                mConnectingDialog.dismiss();
                mConnectingDialog = null;
            }
        }

        @Override
        protected void onCancelled(UhAccessHelper.ConnectResult connectResult) {
            super.onCancelled(connectResult);
            mUseUH = false;
            mUHAccessHelper = null;
            if (mConnectingDialog != null) {
                mConnectingDialog.dismiss();
                mConnectingDialog = null;
            }
        }

        @Override
        protected UhAccessHelper.ConnectResult doInBackground(Void... params) {
            return mUHAccessHelper.connect();
        }

        @Override
        protected void onPostExecute(UhAccessHelper.ConnectResult connectResult) {
            super.onPostExecute(connectResult);
            Activity activity = getActivity();

            if (mUseUH) {
                switch (connectResult) {
                    case ErrNoSupportBT:
                        ToastUtil.showToast(activity, "Not support Bluetooth", Toast.LENGTH_LONG);
                        mUHAccessHelper = null;
                        break;
                    case ErrNotPairedUnlimitedHand:
                        ToastUtil.showToast(activity, "Please pair to Unlimited Hand", Toast.LENGTH_LONG);
                        mUHAccessHelper = null;
                        break;
                    case PairedWithoutConnection:
                        ToastUtil.showToast(activity, "Success to search Unlimited Hand(not connected)", Toast.LENGTH_LONG);
                        break;
                    case Connected:
                        ToastUtil.showToast(activity, "Success to connect Unlimited Hand", Toast.LENGTH_LONG);
                        break;
                    case ErrUnknown:
                    default:
                        ToastUtil.showToast(activity, "Not connect to Unlimited Hand", Toast.LENGTH_LONG);
                        mUHAccessHelper = null;
                        break;
                }
            } else {
                mUHAccessHelper = null;
            }

            if (mConnectingDialog != null) {
                mConnectingDialog.dismiss();
                mConnectingDialog = null;
            }
        }
    }
}
