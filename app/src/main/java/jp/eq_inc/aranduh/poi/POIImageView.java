package jp.eq_inc.aranduh.poi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dopanic.panicarkit.lib.PARPoi;

import jp.eq_inc.aranduh.R;

public class POIImageView extends PARPoi {
    private ImageView mContentImageView;
    private ImageView mUnderContentImageView;
    private ImageView mOverContentImageView;
    private View.OnClickListener mClickListener;
    private View.OnTouchListener mTouchListener;
    private Point mContentSize = null;
    private Integer mBgColor = null;
    private Integer mImageResourceId = null;
    private Integer mUnderContentImageResourceId = null;
    private Integer mOverContentImageResourceId = null;
    private Bitmap mImageBitmap = null;

    public POIImageView(Location atLocation) {
        super(atLocation);
        this.radarResourceId = R.drawable.radar_dot;
    }

    @Override
    public void createView() {
        super.createView();

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _labelView = (RelativeLayout) inflater.inflate(R.layout.poi_imageview, null, false);

        mContentImageView = (ImageView) _labelView.findViewById(R.id.ivPoiContent);
        mUnderContentImageView = (ImageView) _labelView.findViewById(R.id.ivUnderContent);
        mOverContentImageView = (ImageView) _labelView.findViewById(R.id.ivOverContent);
        if (mImageResourceId != null) {
            mContentImageView.setImageResource(mImageResourceId);
            mImageResourceId = null;
        }
        if (mImageBitmap != null) {
            mContentImageView.setImageBitmap(mImageBitmap);
            mImageBitmap = null;
        }
        if (mBgColor != null) {
            mContentImageView.setBackgroundColor(mBgColor);
        }
        setImageResourceForUnderContent(mUnderContentImageResourceId);
        setImageResourceForOverContent(mOverContentImageResourceId);

        if (mContentSize != null) {
            setSize(mContentSize.x, mContentSize.y);
        }

        setOnClickListener(mClickListener);
        setOnTouchListener(mTouchListener);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mClickListener = listener;
        if (mContentImageView != null) {
            mContentImageView.setOnClickListener(listener);
        }
        if (mOverContentImageView != null) {
            mOverContentImageView.setOnClickListener(listener);
        }
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        mTouchListener = listener;
        if (mContentImageView != null) {
            mContentImageView.setOnTouchListener(listener);
        }
        if (mOverContentImageView != null) {
            mOverContentImageView.setOnTouchListener(listener);
        }
    }

    public void setSize(int xSize, int ySize) {
        if (mContentSize == null) {
            mContentSize = new Point();
        }
        mContentSize.set(xSize, ySize);

        if ((mContentImageView != null) && (mContentSize != null)) {
            ViewGroup.LayoutParams params = mContentImageView.getLayoutParams();
            params.width = mContentSize.x;
            params.height = mContentSize.y;
            ((ViewGroup) (mContentImageView.getParent())).updateViewLayout(mContentImageView, params);
        }
    }

    public void setImageResource(int resId) {
        if (mContentImageView != null) {
            mContentImageView.setImageResource(resId);
        } else {
            mImageBitmap = null;
            mImageResourceId = resId;
        }
    }

    public void setImageBitmap(Bitmap bitmap) {
        if (mContentImageView != null) {
            mContentImageView.setImageBitmap(bitmap);
        } else {
            mImageBitmap = bitmap;
            mImageResourceId = null;
        }
    }

    public void setBackgroundColor(int color) {
        if (mContentImageView != null) {
            mContentImageView.setBackgroundColor(color);
        } else {
            mBgColor = color;
        }
    }

    public void setImageResourceForUnderContent(Integer resId) {
        if (mUnderContentImageView != null) {
            if (resId == null) {
                mUnderContentImageView.setVisibility(View.GONE);
            } else {
                mUnderContentImageView.setVisibility(View.VISIBLE);
                mUnderContentImageView.setImageResource(resId);
            }
        } else {
            mUnderContentImageResourceId = resId;
        }
    }

    public void setImageResourceForOverContent(Integer resId) {
        if (mOverContentImageView != null) {
            if (resId == null) {
                mOverContentImageView.setVisibility(View.GONE);
            } else {
                mOverContentImageView.setVisibility(View.VISIBLE);
                mOverContentImageView.setImageResource(resId);
            }
        } else {
            mOverContentImageResourceId = resId;
        }
    }
}
