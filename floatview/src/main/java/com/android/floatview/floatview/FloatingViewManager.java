package com.android.floatview.floatview;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.floatview.R;

import java.lang.ref.WeakReference;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.core.view.ViewCompat;

public class FloatingViewManager implements IFloatingView {

    private BaseFloatingView mFloatingView;//悬浮view
    private static volatile FloatingViewManager mInstance;
    private WeakReference<FrameLayout> mContainer;//rootview
    @LayoutRes
    private int mLayoutId = R.layout.float_layout;//悬浮view的布局
    @DrawableRes
    private int mIconRes = R.drawable.ic_launcher;//悬浮icon
    private ViewGroup.LayoutParams mLayoutParams = getParams();
    private Handler handler=new Handler(Looper.getMainLooper());

    private FloatingViewManager() {
    }

    public static FloatingViewManager get() {
        if (mInstance == null) {
            synchronized (FloatingViewManager.class) {
                if (mInstance == null) {
                    mInstance = new FloatingViewManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 移除悬浮view
     * @return
     */
    @Override
    public FloatingViewManager remove() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mFloatingView == null) {
                    return;
                }
                if (ViewCompat.isAttachedToWindow(mFloatingView) && getContainer() != null) {
                    getContainer().removeView(mFloatingView);
                }
                mFloatingView = null;
            }
        });
        return this;
    }

    /**
     * 确保悬浮view
     */
    private void ensureFloatingView() {
        synchronized (this) {
            if (mFloatingView != null) {
                return;
            }
            PFloatingView pFloatingView = new PFloatingView(getContainer().getContext());
            mFloatingView = pFloatingView;
            pFloatingView.setLayoutParams(mLayoutParams);
            pFloatingView.setIconImage(mIconRes);
            addViewToWindow(pFloatingView);
        }
    }

    @Override
    public FloatingViewManager add() {
        ensureFloatingView();
        return this;
    }

    /**
     * 显示悬浮view
     * @param activity
     * @return
     */
    @Override
    public FloatingViewManager attach(Activity activity) {
        attach(getRootView(activity));
        ensureFloatingView();
        return this;
    }

    @Override
    public FloatingViewManager attach(FrameLayout container) {
        if (container == null || mFloatingView == null) {
            mContainer = new WeakReference<>(container);
            return this;
        }
        if (mFloatingView.getParent() == container) {
            return this;
        }
        if (getContainer() != null && mFloatingView.getParent() == getContainer()) {
            getContainer().removeView(mFloatingView);
        }
        mContainer = new WeakReference<>(container);
        container.addView(mFloatingView);
        return this;
    }

    @Override
    public FloatingViewManager detach(Activity activity) {
        detach(getRootView(activity));
        return this;
    }

    /**
     * 移除view
     * @param container
     * @return
     */
    @Override
    public FloatingViewManager detach(FrameLayout container) {
        if (mFloatingView != null && container != null && ViewCompat.isAttachedToWindow(mFloatingView)) {
            container.removeView(mFloatingView);
        }
        if (getContainer() == container) {
            mContainer = null;
        }
        return this;
    }

    @Override
    public BaseFloatingView getView() {
        return mFloatingView;
    }

    @Override
    public FloatingViewManager icon(@DrawableRes int resId) {
        mIconRes = resId;
        return this;
    }

    @Override
    public FloatingViewManager customView(BaseFloatingView viewGroup) {
        mFloatingView = viewGroup;
        return this;
    }

    @Override
    public FloatingViewManager customView(@LayoutRes int resource) {
        mLayoutId = resource;
        return this;
    }

    @Override
    public FloatingViewManager layoutParams(ViewGroup.LayoutParams params) {
        mLayoutParams = params;
        if (mFloatingView != null) {
            mFloatingView.setLayoutParams(params);
        }
        return this;
    }

    /**
     * 将view添加到rootview
     * @param view
     */
    private void addViewToWindow(final View view) {
        if (getContainer() == null) {
            return;
        }
        getContainer().addView(view);
    }

    private FrameLayout getContainer() {
        if (mContainer == null) {
            return null;
        }
        return mContainer.get();
    }

    private FrameLayout.LayoutParams getParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.START;
        params.setMargins(13, params.topMargin, params.rightMargin, 500);
        return params;
    }

    /**
     *  获取activity的rootview
     * @param activity
     * @return
     */
    private FrameLayout getRootView(Activity activity) {
        if (activity == null) {
            return null;
        }
        try {
            return (FrameLayout) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}