package com.anvei.dab.mydome.view;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.ListPopupWindow.WRAP_CONTENT;

/**
 * Created by DAB on 2016/12/5 17:05.
 */

public class RefreshView extends ViewGroup {

    private static final String TAG = "RefreshView";
    //头部的高度
    protected float mHeadHeight, mHeadWidth;

    //底部高度
    private float mFootHeight, mFootWidth;

    private float mContentHeight, mContentWidth;

    private float mStartRefreshDistance;//需要下拉的距离才触发刷新

    private ViewDragHelper mViewDragHelper;

    private static final int PULL_DOWN_REFRESH = 1;//标志当前进入的刷新模式
    private static final int PULL_UP_LOAD = 2;
    private int state = PULL_DOWN_REFRESH;
    //触摸获得Y的位置
    private float mTouchY;
    //触摸获得X的位置(为防止滑动冲突而设置)
    private float mTouchX;

    private View mContentView, mHeadView, mFootView;

    public RefreshView(Context context) {
        this(context, null, 0);
    }

    public RefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mViewDragHelper = ViewDragHelper.create(this, mCallback);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        int childCount = getChildCount();
        Log.e(TAG, "init: " + childCount);
        if (childCount < 1 || childCount > 3) {
            throw new RuntimeException("里面只能含有一到三个子View!");
        }

//        int childCount = getChildCount();
        switch (childCount) {
            case 1:
                mContentView = getChildAt(0);
                break;
            case 2:
                mHeadView = getChildAt(0);
                mContentView = getChildAt(1);
                break;
            case 3:
                mHeadView = getChildAt(0);
                mContentView = getChildAt(1);
                mFootView = getChildAt(2);
                break;
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeigth = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(measureWidth, measureHeigth);

        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            Log.e(TAG, "measureWidth is " + v.getMeasuredWidth() + "measureHeight is " + v.getMeasuredHeight());
            int widthSpec = 0;
            int heightSpec = 0;
            LayoutParams params = v.getLayoutParams();
            if (params.width > 0) {
                widthSpec = MeasureSpec.makeMeasureSpec(params.width, MeasureSpec.EXACTLY);
            } else if (params.width == MATCH_PARENT) {
                widthSpec = MeasureSpec.makeMeasureSpec(measureWidth, MeasureSpec.EXACTLY);
            } else if (params.width == WRAP_CONTENT) {
                widthSpec = MeasureSpec.makeMeasureSpec(measureWidth, MeasureSpec.AT_MOST);
            }

            if (params.height > 0) {
                heightSpec = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY);
            } else if (params.height == MATCH_PARENT) {
                heightSpec = MeasureSpec.makeMeasureSpec(measureHeigth, MeasureSpec.EXACTLY);
            } else if (params.height == WRAP_CONTENT) {
                heightSpec = MeasureSpec.makeMeasureSpec(measureWidth, MeasureSpec.AT_MOST);
            }
            v.measure(widthSpec, heightSpec);

        }


    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mHeadView != null) {
            mHeadHeight = mHeadView.getMeasuredHeight();
            mHeadWidth = mHeadView.getMeasuredWidth();
        }
        if (mFootView != null) {
            mFootHeight = mFootView.getMeasuredHeight();
            mFootWidth = mFootView.getMeasuredWidth();
        }
        if (mContentView != null) {
            mContentHeight = mContentView.getMeasuredHeight();
            mContentWidth = mContentView.getMeasuredWidth();
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mHeadView.layout(0, (int) (-mHeadHeight), (int) mHeadWidth, 0);
        mContentView.layout(0, 0, (int) mContentWidth, (int) mContentHeight);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchY = event.getY();
                mTouchX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float y = event.getY() - mTouchY;
                if (y > 0 && canChildScrollUp(mContentView)) {
                    return false;
                }
                break;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return (child == mContentView);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (state != PULL_UP_LOAD) {
                //回调出去当前拉的距离
                if (mOnPullDownDistanceChange != null) {
                    mOnPullDownDistanceChange.onPullDownDistanceChange((int) mStartRefreshDistance, top);
                }
            }
            if (top < 0) top = 0;//禁止往上拉(只能往下拉)
//            if (top > mHeadHeight * 2) top = (int) (mHeadHeight * 2);
            return top;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mContentView == child ? child.getHeight() : 0;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            //修改头布局的位置
            mHeadView.layout(mHeadView.getLeft() + dx, mHeadView.getTop() + dy, mHeadView.getRight() + dx, mHeadView.getBottom() + dy);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (mStartRefreshDistance == 0) {
                mStartRefreshDistance = mHeadView.getMeasuredHeight();
            }
            //判断是否应该刷新
            if (mHeadView.getTop() > mStartRefreshDistance / 2) {
                state = PULL_UP_LOAD;
                mViewDragHelper.smoothSlideViewTo(mContentView, 0, (int) mHeadHeight);
                ViewCompat.postInvalidateOnAnimation(RefreshView.this);
            } else close();
        }
    };

    /**
     * 回到初始的位置
     */
    public void close() {
        state = PULL_DOWN_REFRESH;
        mViewDragHelper.smoothSlideViewTo(mContentView, 0, 0);
        ViewCompat.postInvalidateOnAnimation(RefreshView.this);
    }

    @Override
    public void computeScroll() {
        //判断是否滚动完成,没有则继续
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(RefreshView.this);
        } else {
            //滚动完成,来根据状态判断是否需要需要回调刷新
            if (state == PULL_UP_LOAD) {
                if (mOnRefreshing != null) {
                    mOnRefreshing.onRefreshing();
                }
            }
        }
    }


    /**
     * 用来判断是否可以下拉(解决点击冲突用)
     */
    public static boolean canChildScrollUp(View mChildView) {
        if (mChildView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mChildView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mChildView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mChildView, -1) || mChildView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mChildView, -1);
        }
    }

    private OnPullDownDistanceChange mOnPullDownDistanceChange;

    public void setOnPullDownDistanceChange(OnPullDownDistanceChange onPullDownDistanceChange) {
        mOnPullDownDistanceChange = onPullDownDistanceChange;
    }

    private OnRefreshing mOnRefreshing;

    public void setOnRefreshing(OnRefreshing onRefreshing) {
        mOnRefreshing = onRefreshing;
    }

    /**
     * 下拉距离回调
     */
    public interface OnPullDownDistanceChange {
        /**
         * @param startRefreshDistance 超过这个距离开始刷新
         * @param distance             当前的距离
         */
        void onPullDownDistanceChange(int startRefreshDistance, int distance);
    }

    /**
     * 刷新的回调
     */
    public interface OnRefreshing {
        void onRefreshing();
    }
}

