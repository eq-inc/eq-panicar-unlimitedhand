package jp.eq_inc.aranduh.poi;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.dopanic.panicarkit.lib.PARPoi;

import jp.co.thcomp.glsurfaceview.GLContext;
import jp.co.thcomp.glsurfaceview.GLDrawView;
import jp.co.thcomp.util.ThreadUtil;
import jp.eq_inc.aranduh.R;

public class POIGLDrawView extends PARPoi {
    private GLDrawView mContentDrawView;
    private View.OnClickListener mClickListener;
    private View.OnTouchListener mTouchListener;
    private OnPrepareDrawListener mPrepareDrawListener;
    private Point mContentSize = null;

    public interface OnPrepareDrawListener {
        void onPrepareDrawView(GLDrawView drawView);
    }

    public POIGLDrawView(Location atLocation) {
        super(atLocation);
        this.radarResourceId = R.drawable.radar_dot;
    }

    @Override
    public void createView() {
        super.createView();

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _labelView = (RelativeLayout) inflater.inflate(R.layout.poi_gldrawview, null, false);

        mContentDrawView = (GLDrawView) _labelView.findViewById(R.id.dvPoiContent);
        mContentDrawView.setZOrderOnTop(true);
        mContentDrawView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mContentDrawView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        GLContext glContext = new GLContext(mContentDrawView, mContentDrawView.getContext());
        glContext.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mContentDrawView.startRenderer(glContext, mContentDrawView.getContext().getApplicationContext());

        if (mPrepareDrawListener != null) {
            mPrepareDrawListener.onPrepareDrawView(mContentDrawView);
        }

        if (mContentSize != null) {
            setSize(mContentSize.x, mContentSize.y);
        }

        setOnClickListener(mClickListener);
        setOnTouchListener(mTouchListener);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mClickListener = listener;
        if (mContentDrawView != null) {
            mContentDrawView.setOnClickListener(listener);
        }
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        mTouchListener = listener;
        if (mContentDrawView != null) {
            mContentDrawView.setOnTouchListener(listener);
        }
    }

    public void setOnPrepareDrawListener(OnPrepareDrawListener listener) {
        mPrepareDrawListener = listener;
        if (mContentDrawView != null) {
            ThreadUtil.runOnMainThread(mContentDrawView.getContext(), new Runnable() {
                @Override
                public void run() {
                    mPrepareDrawListener.onPrepareDrawView(mContentDrawView);
                }
            });
        }
    }

    public void setSize(int xSize, int ySize) {
        if (mContentSize == null) {
            mContentSize = new Point();
        }
        mContentSize.set(xSize, ySize);

        if ((mContentDrawView != null) && (mContentSize != null)) {
            ViewGroup.LayoutParams params = mContentDrawView.getLayoutParams();
            params.width = mContentSize.x;
            params.height = mContentSize.y;
            ((ViewGroup) (mContentDrawView.getParent())).updateViewLayout(mContentDrawView, params);
        }
    }

    public GLDrawView getContentDrawView() {
        return mContentDrawView;
    }
}
