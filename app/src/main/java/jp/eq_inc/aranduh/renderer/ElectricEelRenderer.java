package jp.eq_inc.aranduh.renderer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.SurfaceHolder;

import jp.eq_inc.aranduh.R;

public class ElectricEelRenderer implements SurfaceHolder.Callback {
    private static final int NORMAL_EEL = 0;
    private static final int DISCHARGING_EEL = 1;

    private Context mContext;
    private SurfaceHolder mHolder;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private HandlerThread mRenderThread;
    private Handler mRenderThreadHandler;
    private int mEelStatus = NORMAL_EEL;
    private Bitmap mNormalEelBitmap;
    private Bitmap mDischargingElectric;

    public ElectricEelRenderer(Context context) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }

        mContext = context;
        Resources res = context.getResources();
        mNormalEelBitmap = BitmapFactory.decodeResource(res, R.drawable.eel);
        mDischargingElectric = BitmapFactory.decodeResource(res, R.drawable.electric);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        mHolder.setFormat(PixelFormat.TRANSLUCENT);

        if (mRenderThread != null) {
            mRenderThread.quit();
        }
        mRenderThread = new HandlerThread(ElectricEelRenderer.class.getSimpleName(), Thread.NORM_PRIORITY);
        mRenderThread.start();
        mRenderThreadHandler = new Handler(mRenderThread.getLooper(), mRenderHandlerCallback);

        holder.setFixedSize(mNormalEelBitmap.getWidth(), mNormalEelBitmap.getHeight());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHolder = holder;
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        sendNextMessage();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        mSurfaceWidth = 0;
        mSurfaceHeight = 0;
        if (mRenderThread != null) {
            mRenderThread.quit();
        }
    }

    private void sendNextMessage() {
        int nextStatus = ((int) (Math.random() * 1000)) % 2;
        int delayMS = (((int) (Math.random() * 1000)) % 10) * 1000;
        mRenderThreadHandler.sendEmptyMessageDelayed(nextStatus, delayMS);
    }

    private Handler.Callback mRenderHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Canvas canvas = mHolder != null ? mHolder.lockCanvas() : null;

            if (canvas != null) {
                Rect imageRect = new Rect(0, 0, mNormalEelBitmap.getWidth(), mNormalEelBitmap.getHeight());
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                try {
                    mEelStatus = msg.what;
                    switch (mEelStatus) {
                        case NORMAL_EEL:
                            canvas.drawBitmap(mNormalEelBitmap, imageRect, imageRect, null);
                            break;
                        case DISCHARGING_EEL:
                            canvas.drawBitmap(mDischargingElectric, imageRect, imageRect, null);
                            break;
                    }
                    sendNextMessage();
                } finally {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }

            return true;
        }
    };
}
