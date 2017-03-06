package jp.eq_inc.aranduh.poi;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.dopanic.panicarkit.lib.PARPoi;

import jp.eq_inc.aranduh.R;

public class POISurfaceView extends PARPoi {
    private SurfaceHolder.Callback mSurfaceHolderCallback;
    private SurfaceView mContentSurfaceView;
    private View.OnClickListener mClickListener;
    private View.OnTouchListener mTouchListener;
    private Point mContentSize = null;

    public POISurfaceView(Location atLocation, SurfaceHolder.Callback callback) {
        super(atLocation);

        if (callback == null) {
            throw new NullPointerException("callback == null");
        }
        mSurfaceHolderCallback = callback;
    }

    @Override
    public void createView() {
        super.createView();

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _labelView = (RelativeLayout) inflater.inflate(R.layout.poi_surfaceview, null, false);

        mContentSurfaceView = (SurfaceView) _labelView.findViewById(R.id.svPoiContent);

        if (mContentSize != null) {
            setSize(mContentSize.x, mContentSize.y);
        }

        mContentSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
        setOnClickListener(mClickListener);
        setOnTouchListener(mTouchListener);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mClickListener = listener;
        if (mContentSurfaceView != null) {
            mContentSurfaceView.setOnClickListener(listener);
        }
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        mTouchListener = listener;
        if (mContentSurfaceView != null) {
            mContentSurfaceView.setOnTouchListener(listener);
        }
    }

    public void setSize(int xSize, int ySize) {
        if (mContentSize == null) {
            mContentSize = new Point();
        }
        mContentSize.set(xSize, ySize);

        if ((mContentSurfaceView != null) && (mContentSize != null)) {
            ViewGroup.LayoutParams params = mContentSurfaceView.getLayoutParams();
            params.width = mContentSize.x;
            params.height = mContentSize.y;
            ((ViewGroup) (mContentSurfaceView.getParent())).updateViewLayout(mContentSurfaceView, params);
        }
    }
}
