package com.qzz.tagcloud.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.OverScroller;
import com.qzz.tagcloud.R;
import com.qzz.tagcloud.adapter.DataSetObserver;
import com.qzz.tagcloud.adapter.TagAdapter;

import java.util.ArrayList;
import java.util.List;

public class TagCloudView extends ViewGroup {

    // 默认配置常量
    private static final int DEFAULT_HORIZONTAL_MARGIN = 12; // dp
    private static final int DEFAULT_VERTICAL_MARGIN = 8;    // dp
    private static final int DEFAULT_LINE_SPACING = 8;       // dp
    private static final int DEFAULT_GRAVITY = 0;            // left
    private static final boolean DEFAULT_SCROLLABLE = false;
    private static final int DEFAULT_SCROLL_DIRECTION = 0;   // both
    private static final float DEFAULT_SCROLL_DAMPING = 0.9f;
    private static final float DEFAULT_OVERSCROLL_STRENGTH = 0.3f;
    private static final int DEFAULT_ANIMATION_DURATION = 300;

    // 布局参数
    private int mHorizontalMargin;
    private int mVerticalMargin;
    private int mLineSpacing;
    private int mMaxLines = -1; // -1表示不限制
    private int mGravity;

    // 滑动相关参数
    private boolean mScrollable;
    private int mScrollDirection;
    private float mScrollDamping;
    private float mOverScrollStrength;
    private int mAnimationDuration;
    private boolean mEnableRipple;

    // 滑动状态
    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    // 触摸相关
    private float mLastMotionX;
    private float mLastMotionY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = -1;

    // 滚动边界
    private int mScrollRangeX;
    private int mScrollRangeY;
    private int mOverScrollDistance;

    // 适配器和数据
    private TagAdapter mAdapter;
    private List<View> mChildViews;
    private DataSetObserver mDataSetObserver;

    // 布局信息
    private List<LineInfo> mLines;
    private int mContentWidth;
    private int mContentHeight;

    // 行信息类
    private static class LineInfo {
        int startIndex;
        int endIndex;
        int width;
        int height;
        int top;
        List<View> views;

        LineInfo() {
            views = new ArrayList<>();
        }
    }

    public TagCloudView(Context context) {
        this(context, null);
    }

    public TagCloudView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagCloudView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        // 初始化默认值
        float density = getResources().getDisplayMetrics().density;
        mHorizontalMargin = (int) (DEFAULT_HORIZONTAL_MARGIN * density);
        mVerticalMargin = (int) (DEFAULT_VERTICAL_MARGIN * density);
        mLineSpacing = (int) (DEFAULT_LINE_SPACING * density);
        mGravity = DEFAULT_GRAVITY;
        mScrollable = DEFAULT_SCROLLABLE;
        mScrollDirection = DEFAULT_SCROLL_DIRECTION;
        mScrollDamping = DEFAULT_SCROLL_DAMPING;
        mOverScrollStrength = DEFAULT_OVERSCROLL_STRENGTH;
        mAnimationDuration = DEFAULT_ANIMATION_DURATION;
        mEnableRipple = true;

        // 解析自定义属性
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TagCloudView, defStyleAttr, 0);

            mHorizontalMargin = a.getDimensionPixelSize(R.styleable.TagCloudView_tag_horizontal_margin, mHorizontalMargin);
            mVerticalMargin = a.getDimensionPixelSize(R.styleable.TagCloudView_tag_vertical_margin, mVerticalMargin);
            mLineSpacing = a.getDimensionPixelSize(R.styleable.TagCloudView_tag_line_spacing, mLineSpacing);
            mMaxLines = a.getInteger(R.styleable.TagCloudView_tag_max_lines, mMaxLines);
            mGravity = a.getInteger(R.styleable.TagCloudView_tag_gravity, mGravity);
            // 滑动相关属性
            mScrollable = a.getBoolean(R.styleable.TagCloudView_tag_scrollable, mScrollable);
            mScrollDirection = a.getInteger(R.styleable.TagCloudView_tag_scroll_direction, mScrollDirection);
            mScrollDamping = a.getFloat(R.styleable.TagCloudView_tag_scroll_damping, mScrollDamping);
            mOverScrollStrength = a.getFloat(R.styleable.TagCloudView_tag_overscroll_strength, mOverScrollStrength);
            mAnimationDuration = a.getInteger(R.styleable.TagCloudView_tag_animation_duration, mAnimationDuration);
            mEnableRipple = a.getBoolean(R.styleable.TagCloudView_tag_enable_ripple, mEnableRipple);

            a.recycle();
        }

        // 初始化滑动组件
        initScrolling(context);

        // 初始化数据结构
        mChildViews = new ArrayList<>();
        mLines = new ArrayList<>();
        mDataSetObserver = new DataSetObserver() { // 修改实现方式
            @Override
            public void onChanged() {
                Log.d("TagCloudView", "DataSet changed, refreshing views.");
                refreshViews();
            }
            @Override
            public void onInvalidated() {
                refreshViews();
            }
        };
    }

    private void initScrolling(Context context) {
        mScroller = new OverScroller(context);

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverScrollDistance = configuration.getScaledOverscrollDistance();

        setFocusable(true);
        setClickable(true);
    }

    // 适配器设置
    public void setAdapter(TagAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
        refreshViews();
    }

    public TagAdapter getAdapter() {
        return mAdapter;
    }

    // 刷新视图
    private void refreshViews() {
        removeAllViews();
        mChildViews.clear();

        if (mAdapter != null) {
            int count = mAdapter.getCount();
            for (int i = 0; i < count; i++) {
                View childView = mAdapter.getView(i, null, this);
                if (childView != null) {
                    addView(childView);
                    mChildViews.add(childView);

                    // 设置点击事件
                    final int position = i;
                    childView.setOnClickListener(v -> {
                        if (mOnTagClickListener != null) {
                            mOnTagClickListener.onTagClick(v, position);
                        }
                    });
                }
            }
        }

        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 计算可用宽度
        int availableWidth = widthSize - getPaddingLeft() - getPaddingRight();

        // 测量所有子视图
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        // 计算布局
        calculateLayout(availableWidth);

        // 确定最终尺寸
        int finalWidth = widthSize;
        int finalHeight;

        if (heightMode == MeasureSpec.EXACTLY) {
            finalHeight = heightSize;
        } else {
            finalHeight = mContentHeight + getPaddingTop() + getPaddingBottom();
            if (heightMode == MeasureSpec.AT_MOST) {
                finalHeight = Math.min(finalHeight, heightSize);
            }
        }

        setMeasuredDimension(finalWidth, finalHeight);

        // 计算滚动范围
        calculateScrollRange();
    }

    private void calculateLayout(int availableWidth) {
        mLines.clear();
        mContentWidth = 0;
        mContentHeight = 0;

        if (mChildViews.isEmpty()) {
            return;
        }

        LineInfo currentLine = new LineInfo();
        currentLine.top = getPaddingTop();
        int currentLineWidth = 0;
        int currentLineHeight = 0;

        for (int i = 0; i < mChildViews.size(); i++) {
            View child = mChildViews.get(i);
            if (child.getVisibility() == GONE) continue;

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 检查是否需要换行
            boolean needNewLine = false;
            if (currentLine.views.isEmpty()) {
                // 第一个元素，直接添加
                needNewLine = false;
            } else if (currentLineWidth + mHorizontalMargin + childWidth > availableWidth) {
                // 当前行放不下，需要换行
                needNewLine = true;
            } else if (mMaxLines > 0 && mLines.size() >= mMaxLines && !currentLine.views.isEmpty()) {
                // 达到最大行数限制
                break;
            }

            if (needNewLine) {
                // 完成当前行
                finishLine(currentLine, currentLineWidth, currentLineHeight);
                mLines.add(currentLine);

                // 开始新行
                currentLine = new LineInfo();
                currentLine.top = mContentHeight + mLineSpacing;
                currentLineWidth = 0;
                currentLineHeight = 0;
            }

            // 添加到当前行
            currentLine.views.add(child);
            currentLineWidth += (currentLine.views.size() > 1 ? mHorizontalMargin : 0) + childWidth;
            currentLineHeight = Math.max(currentLineHeight, childHeight);
        }

        // 完成最后一行
        if (!currentLine.views.isEmpty()) {
            finishLine(currentLine, currentLineWidth, currentLineHeight);
            mLines.add(currentLine);
        }
    }

    private void finishLine(LineInfo line, int lineWidth, int lineHeight) {
        line.width = lineWidth;
        line.height = lineHeight;
        mContentWidth = Math.max(mContentWidth, lineWidth);
        mContentHeight = line.top + lineHeight;
    }

    private void calculateScrollRange() {
        int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        mScrollRangeX = Math.max(0, mContentWidth - viewWidth);
        mScrollRangeY = Math.max(0, mContentHeight - viewHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        for (LineInfo line : mLines) {
            layoutLine(line, paddingLeft);
        }
    }

    private void layoutLine(LineInfo line, int paddingLeft) {
        int startX = paddingLeft;

        // 根据gravity调整起始位置
        if (mGravity == 1) { // center
            int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            startX += (availableWidth - line.width) / 2;
        } else if (mGravity == 2) { // right
            int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            startX += availableWidth - line.width;
        }

        int currentX = startX;
        for (View child : line.views) {
            if (child.getVisibility() == GONE) continue;

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 垂直居中对齐
            int childTop = line.top + (line.height - childHeight) / 2;

            child.layout(
                    currentX - getScrollX(),
                    childTop - getScrollY(),
                    currentX + childWidth - getScrollX(),
                    childTop + childHeight - getScrollY()
            );

            currentX += childWidth + mHorizontalMargin;
        }
    }

    // 触摸事件处理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mScrollable) {
            return super.onTouchEvent(event);
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                return onTouchDown(event);

            case MotionEvent.ACTION_MOVE:
                return onTouchMove(event);

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return onTouchUp(event);
        }

        return super.onTouchEvent(event);
    }

    private boolean onTouchDown(MotionEvent event) {
        mLastMotionX = event.getX();
        mLastMotionY = event.getY();
        mActivePointerId = event.getPointerId(0);

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        return true;
    }

    private boolean onTouchMove(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        if (pointerIndex == -1) return false;

        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);
        final float deltaX = mLastMotionX - x;
        final float deltaY = mLastMotionY - y;

        if (!mIsBeingDragged) {
            if (shouldStartDragging(deltaX, deltaY)) {
                mIsBeingDragged = true;
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }

        if (mIsBeingDragged) {
            mLastMotionX = x;
            mLastMotionY = y;

            int scrollX = getScrollX();
            int scrollY = getScrollY();

            if (canScrollHorizontally()) {
                scrollX += (int) deltaX;
                scrollX = Math.max(0, Math.min(scrollX, mScrollRangeX));
            }

            if (canScrollVertically()) {
                scrollY += (int) deltaY;
                scrollY = Math.max(0, Math.min(scrollY, mScrollRangeY));
            }

            scrollTo(scrollX, scrollY);
            return true;
        }

        return false;
    }

    private boolean onTouchUp(MotionEvent event) {
        if (mIsBeingDragged) {
            mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
            int velocityX = (int) mVelocityTracker.getXVelocity(mActivePointerId);
            int velocityY = (int) mVelocityTracker.getYVelocity(mActivePointerId);

            if (Math.abs(velocityX) > mMinimumVelocity || Math.abs(velocityY) > mMinimumVelocity) {
                fling(-velocityX, -velocityY);
            }

            mIsBeingDragged = false;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }

        return true;
    }

    private boolean shouldStartDragging(float deltaX, float deltaY) {
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        return distance > mTouchSlop;
    }

    private boolean canScrollHorizontally() {
        return mScrollDirection == 0 || mScrollDirection == 1; // both or horizontal
    }

    private boolean canScrollVertically() {
        return mScrollDirection == 0 || mScrollDirection == 2; // both or vertical
    }

    private void fling(int velocityX, int velocityY) {
        mScroller.fling(
                getScrollX(), getScrollY(),
                velocityX, velocityY,
                0, mScrollRangeX,
                0, mScrollRangeY,
                mOverScrollDistance, mOverScrollDistance
        );
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }

            if (!mScroller.isFinished()) {
                postInvalidate();
            }
        }
    }

    // 事件监听接口
    public interface OnTagClickListener {
        void onTagClick(View view, int position);
    }

    private OnTagClickListener mOnTagClickListener;

    public void setOnTagClickListener(OnTagClickListener listener) {
        mOnTagClickListener = listener;
    }


    // Setter方法
    public void setHorizontalMargin(int margin) {
        mHorizontalMargin = margin;
        requestLayout();
    }

    public void setVerticalMargin(int margin) {
        mVerticalMargin = margin;
        requestLayout();
    }

    public void setLineSpacing(int spacing) {
        mLineSpacing = spacing;
        requestLayout();
    }

    public void setMaxLines(int maxLines) {
        mMaxLines = maxLines;
        requestLayout();
    }

    public void setGravity(int gravity) {
        mGravity = gravity;
        requestLayout();
    }

    public void setScrollable(boolean scrollable) {
        mScrollable = scrollable;
    }

    public void notifyDataSetChanged() {
        if (mAdapter != null) {
            refreshViews();
        }
    }
}
