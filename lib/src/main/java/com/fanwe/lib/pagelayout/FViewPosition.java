package com.fanwe.lib.pagelayout;

import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public class FViewPosition
{
    private static final Map<View, FViewPosition> MAP_VIEW_POSITION = new WeakHashMap<>();

    private WeakReference<View> mView;
    private int mLeft;
    private int mTop;

    private FViewPosition(View view)
    {
        setView(view);
    }

    public static FViewPosition get(View view)
    {
        if (view == null)
        {
            return null;
        }

        FViewPosition position = MAP_VIEW_POSITION.get(view);
        if (position == null)
        {
            position = new FViewPosition(view);
            MAP_VIEW_POSITION.put(view, position);
        }
        return position;
    }

    public void setView(View view)
    {
        final View old = getView();
        if (old != view)
        {
            if (view != null)
            {
                mView = new WeakReference<>(view);
            } else
            {
                mView = null;
            }
            reset();
        }
    }

    private View getView()
    {
        return mView == null ? null : mView.get();
    }

    public boolean savePosition()
    {
        final View view = getView();
        if (view == null)
        {
            return false;
        }
        mLeft = view.getLeft();
        mTop = view.getTop();
        return true;
    }

    public int getLeft()
    {
        return mLeft;
    }

    public int getTop()
    {
        return mTop;
    }

    public boolean layout()
    {
        final View view = getView();
        if (view == null)
        {
            return false;
        }
        if (!hasPosition())
        {
            return false;
        }

        view.layout(mLeft, mTop, mLeft + view.getWidth(), mTop + view.getHeight());
        return true;
    }

    public boolean hasPosition()
    {
        return mLeft >= 0 && mTop >= 0;
    }

    public void reset()
    {
        mLeft = -1;
        mTop = -1;
    }
}