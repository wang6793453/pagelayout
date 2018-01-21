package com.fanwe.lib.pagelayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.fanwe.lib.touchhelper.FScroller;
import com.fanwe.lib.touchhelper.FTouchHelper;

/**
 * Created by Administrator on 2018/1/16.
 */
public abstract class FGestureFrameLayout extends FrameLayout
{
    public FGestureFrameLayout(Context context)
    {
        super(context);
        init();
    }

    public FGestureFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public FGestureFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private FTouchHelper mTouchHelper = new FTouchHelper();
    private FScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private ViewConfiguration mViewConfiguration;

    private boolean mIntercept;

    private void init()
    {
    }

    protected final FTouchHelper getTouchHelper()
    {
        return mTouchHelper;
    }

    protected final FScroller getScroller()
    {
        if (mScroller == null)
        {
            mScroller = new FScroller(getContext());
        }
        return mScroller;
    }

    protected final VelocityTracker getVelocityTracker()
    {
        if (mVelocityTracker == null)
        {
            mVelocityTracker = VelocityTracker.obtain();
        }
        return mVelocityTracker;
    }

    protected final ViewConfiguration getViewConfiguration()
    {
        if (mViewConfiguration == null)
        {
            mViewConfiguration = ViewConfiguration.get(getContext());
        }
        return mViewConfiguration;
    }

    public void setIntercept(boolean intercept)
    {
        mIntercept = intercept;
    }

    @Override
    public void computeScroll()
    {
        super.computeScroll();
        if (getScroller().computeScrollOffset())
        {
            onComputeScroll(getScroller().getDeltaX(), getScroller().getDeltaY());
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if (!mIntercept)
        {
            return super.onInterceptTouchEvent(ev);
        }

        if (mTouchHelper.isNeedIntercept())
        {
            return true;
        }

        getVelocityTracker().addMovement(ev);
        mTouchHelper.processTouchEvent(ev);
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                releaseProcess();
                break;
            case MotionEvent.ACTION_MOVE:
                if (canPull(ev))
                {
                    mTouchHelper.setNeedIntercept(true);
                    FTouchHelper.requestDisallowInterceptTouchEvent(this, true);
                }
                break;
        }
        return mTouchHelper.isNeedIntercept();
    }

    private void releaseProcess()
    {
        mTouchHelper.setNeedCosume(false);
        mTouchHelper.setNeedIntercept(false);
        FTouchHelper.requestDisallowInterceptTouchEvent(this, false);

        if (mVelocityTracker != null)
        {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        getVelocityTracker().addMovement(event);
        mTouchHelper.processTouchEvent(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                if (mTouchHelper.isNeedCosume())
                {
                    if (processMoveEvent(event))
                    {
                    } else
                    {
                        releaseProcess();
                    }
                } else
                {
                    if (mTouchHelper.isNeedIntercept() || canPull(event))
                    {
                        mTouchHelper.setNeedCosume(true);
                        mTouchHelper.setNeedIntercept(true);
                        FTouchHelper.requestDisallowInterceptTouchEvent(this, true);
                    } else
                    {
                        releaseProcess();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getVelocityTracker().computeCurrentVelocity(1000);
                onActionUp(event, getVelocityTracker().getXVelocity(), getVelocityTracker().getYVelocity());

                releaseProcess();
                break;
            default:
                break;
        }

        return mTouchHelper.isNeedCosume() || event.getAction() == MotionEvent.ACTION_DOWN;
    }

    protected abstract boolean canPull(MotionEvent event);

    protected abstract boolean processMoveEvent(MotionEvent event);

    protected abstract void onActionUp(MotionEvent event, float xvel, float yvel);

    protected abstract void onComputeScroll(int dx, int dy);


    protected static void synchronizeMargin(View view, boolean update)
    {
        MarginLayoutParams params = getMarginLayoutParams(view);
        if (params == null)
        {
            return;
        }

        final int left = view.getLeft();
        final int top = view.getTop();

        boolean changed = false;
        if (params.leftMargin != left)
        {
            params.leftMargin = left;
            changed = true;
        }
        if (params.topMargin != top)
        {
            params.topMargin = top;
            changed = true;
        }

        if (changed)
        {
            if (update)
            {
                view.setLayoutParams(params);
            }
        }
    }

    /**
     * 获得view的MarginLayoutParams，返回值可能为null
     *
     * @param view
     * @return
     */
    private static MarginLayoutParams getMarginLayoutParams(View view)
    {
        if (view == null)
        {
            return null;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null && params instanceof MarginLayoutParams)
        {
            return (MarginLayoutParams) params;
        } else
        {
            return null;
        }
    }
}
