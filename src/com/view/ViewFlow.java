package com.view;

import java.util.ArrayList;
import java.util.LinkedList;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Scroller;
/**
 * 
 * @author miaowei
 *
 */
public class ViewFlow extends AdapterView<Adapter> {

	private static final int SNAP_VELOCITY = 1000;
	private static final int INVALID_SCREEN = -1;
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;

	private LinkedList<View> mLoadedViews;
	private int mCurrentBufferIndex;
	private int mCurrentAdapterIndex;
	private int mSideBuffer = 2;
	private Scroller mScroller;
	/**
	 * 主要用跟踪触摸屏事件（flinging事件和其他gestures手势事件）的速率。
	 * 用addMovement(MotionEvent)函数将Motion event加入到VelocityTracker类实例中.
	 * 你可以使用getXVelocity() 或getXVelocity()获得横向和竖向的速率到速率时，
	 * 但是使用它们之前请先调用computeCurrentVelocity(int)来初始化速率的单位 。
	 */
	private VelocityTracker mVelocityTracker;
	private int mTouchState = TOUCH_STATE_REST;
	private float mLastMotionX;
	private int mTouchSlop;
	private int mMaximumVelocity;
	private int mCurrentScreen;
	private int mNextScreen = INVALID_SCREEN;
	private boolean mFirstLayout = true;
	private ViewSwitchListener mViewSwitchListener;
	private Adapter mAdapter;
	private int mLastScrollDirection;
	private AdapterDataSetObserver mDataSetObserver;
	private FlowIndicator mIndicator;
	private int mLastOrientation = -1;

	Boolean next = false;// false : Left;true :right

	int endToNextActivity = 0;

	GuideToEnd gd;

	private OnGlobalLayoutListener orientationChangeListener = new OnGlobalLayoutListener() {

		@Override
		public void onGlobalLayout() {
			getViewTreeObserver().removeGlobalOnLayoutListener(
					orientationChangeListener);
			setSelection(0);
		}
	};

	/**
	 * Receives call backs when a new {@link View} has been scrolled to.
	 * 视图滚动接口
	 */
	public static interface ViewSwitchListener {

		/**
		 * This method is called when a new View has been scrolled to.
		 * 
		 * @param view
		 *            the {@link View} currently in focus.
		 * @param position
		 *            The position in the adapter of the {@link View} currently
		 *            in focus.
		 */
		void onSwitched(View view, int position);

	}

	public ViewFlow(Context context) {
		super(context);
		mSideBuffer = 3;
		init();
	}

	public ViewFlow(Context context, int sideBuffer) {
		super(context);
		mSideBuffer = sideBuffer;
		init();
	}

	public ViewFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		/*TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
				R.styleable.ViewFlow);
		mSideBuffer = styledAttrs.getInt(R.styleable.ViewFlow_sidebuffer, 3);*/
		init();
	}

	private void init() {
		mLoadedViews = new LinkedList<View>();
		mScroller = new Scroller(getContext());
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}

	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation != mLastOrientation) {
			mLastOrientation = newConfig.orientation;
			getViewTreeObserver().addOnGlobalLayoutListener(
					orientationChangeListener);
		}
	}

	public int getViewsCount() {
		return mAdapter.getCount();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// final int width = MeasureSpec.getSize(widthMeasureSpec);
		// final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		// if (widthMode != MeasureSpec.EXACTLY && !isInEditMode()) {
		// throw new IllegalStateException(
		// "ViewFlow can only be used in EXACTLY mode.");
		// }
		//
		// final int height = MeasureSpec.getSize(heightMeasureSpec);
		// final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		// if (heightMode != MeasureSpec.EXACTLY && !isInEditMode()) {
		// throw new IllegalStateException(
		// "ViewFlow can only be used in EXACTLY mode.");
		// }
		//
		// // The children are given the same width and height as the workspace
		// final int count = getChildCount();
		// for (int i = 0; i < count; i++) {
		// getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		// }
		//
		// if (mFirstLayout) {
		// mScroller.startScroll(0, 0, mCurrentScreen * width, 0, 0);
		// mFirstLayout = false;
		// }

		// http://lichunming-9-163-com.iteye.com/blog/1127766
		// 有三种可能的模式：
		// UNSPECIFIED：父布局没有给子布局任何限制，子布局可以任意大小。
		// EXACTLY：父布局决定子布局的确切大小。不论子布局多大，它都必须限制在这个界限里。
		// AT_MOST：子布局可以根据自己的大小选择任意大小。

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		// if (widthMode != MeasureSpec.EXACTLY && !isInEditMode()) {
		// throw new IllegalStateException(
		// "ViewFlow can only be used in EXACTLY mode.");
		// }

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		// if (heightMode != MeasureSpec.EXACTLY && !isInEditMode()) {
		// throw new IllegalStateException(
		// "ViewFlow can only be used in EXACTLY mode.");
		// }

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		if (mFirstLayout) {
			mScroller.startScroll(0, 0, mCurrentScreen * width, 0, 0);
			mFirstLayout = false;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth,
						child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (getChildCount() == 0)
			return false;

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.d("action", "ACTION_DOWN~~~~~~~~~~~~~");
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_MOVE:
			Log.d("action", "ACTION_MOVE~~~~~~~~~~~~~");
			final int xDiff = (int) Math.abs(x - mLastMotionX);

			boolean xMoved = xDiff > mTouchSlop;

			if (xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				Log.d("deltaX", "deltaX = " + deltaX);
				mLastMotionX = x;

				final int scrollX = getScrollX();
				if (deltaX < 0) {
					if (scrollX > 0) {
						next = false;
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					next = true;
					final int availableToScroll = getChildAt(
							getChildCount() - 1).getRight()
							- scrollX - getWidth();
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.d("action", "ACTION_UP~~~~~~~~~~~~~");
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurrentScreen < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
					Log.d("lmn", "snapToDestination--");
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}

			mTouchState = TOUCH_STATE_REST;

			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (getChildCount() == 0)
			return false;

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;

			break;
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);

			boolean xMoved = xDiff > mTouchSlop;

			if (xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;

				final int scrollX = getScrollX();
				if (deltaX < 0) {
					if (scrollX > 0) {
						next = false;
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					next = true;
					final int availableToScroll = getChildAt(
							getChildCount() - 1).getRight()
							- scrollX - getWidth();
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
				return true;
			}
			break;

		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurrentScreen < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}

			mTouchState = TOUCH_STATE_REST;

			break;
		case MotionEvent.ACTION_CANCEL:
			snapToDestination();
			mTouchState = TOUCH_STATE_REST;
		}
		return true;
	}

	@Override
	protected void onScrollChanged(int h, int v, int oldh, int oldv) {
		super.onScrollChanged(h, v, oldh, oldv);
		if (mIndicator != null) {
			/*
			 * The actual horizontal scroll origin does typically not match the
			 * perceived one. Therefore, we need to calculate the perceived
			 * horizontal scroll origin here, since we use a view buffer.
			 */
			int hPerceived = h + (mCurrentAdapterIndex - mCurrentBufferIndex)
					* getWidth();
			mIndicator.onScrolled(hPerceived, v, oldh, oldv);
		}
	}

	private void snapToDestination() {
		final int screenWidth = getWidth();
		final int whichScreen = (getScrollX() + (screenWidth / 2))
				/ screenWidth;
		snapToScreen(whichScreen);
	}

	private void snapToScreen(int whichScreen) {
		Log.d("lmn", "whichScreen = " + whichScreen);
		Log.d("lmn", "mCurrentScreen = " + mCurrentScreen);
		mLastScrollDirection = whichScreen - mCurrentScreen;
		if (!mScroller.isFinished()) {
			return;
		}

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));

		mNextScreen = whichScreen;

		final int newX = whichScreen * getWidth();
		final int delta = newX - getScrollX();
		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} else if (mNextScreen != INVALID_SCREEN) {
			mCurrentScreen = Math.max(0,
					Math.min(mNextScreen, getChildCount() - 1));
			mNextScreen = INVALID_SCREEN;
			postViewSwitched(mLastScrollDirection);
		}
	}

	/**
	 * Scroll to the {@link View} in the view buffer specified by the index.
	 * 
	 * @param indexInBuffer
	 *            Index of the view in the view buffer.
	 */
	private void setVisibleView(int indexInBuffer, boolean uiThread) {
		mCurrentScreen = Math.max(0,
				Math.min(indexInBuffer, getChildCount() - 1));
		int dx = (mCurrentScreen * getWidth()) - mScroller.getCurrX();
		mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), dx,
				0, 0);
		if (dx == 0)
			onScrollChanged(mScroller.getCurrX() + dx, mScroller.getCurrY(),
					mScroller.getCurrX() + dx, mScroller.getCurrY());
		if (uiThread)
			invalidate();
		else
			postInvalidate();
	}

	/**
	 * Set the listener that will receive notifications every time the {code
	 * ViewFlow} scrolls.
	 * 
	 * @param l
	 *            the scroll listener
	 */
	public void setOnViewSwitchListener(ViewSwitchListener l) {
		mViewSwitchListener = l;
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void setAdapter(Adapter adapter) {
		setAdapter(adapter, 0);
	}

	public void SetCallBack(GuideToEnd gd) {
		this.gd = gd;
	}

	public void setAdapter(Adapter adapter, int initialPosition) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		mAdapter = adapter;

		if (mAdapter != null) {
			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

		}
		if (mAdapter == null || mAdapter.getCount() == 0)
			return;

		setSelection(initialPosition);
	}

	@Override
	public View getSelectedView() {
		return (mCurrentBufferIndex < mLoadedViews.size() ? mLoadedViews
				.get(mCurrentBufferIndex) : null);
	}

	@Override
	public int getSelectedItemPosition() {
		return mCurrentAdapterIndex;
	}

	/**
	 * Set the FlowIndicator
	 * 
	 * @param flowIndicator
	 */
	public void setFlowIndicator(FlowIndicator flowIndicator) {
		mIndicator = flowIndicator;
		mIndicator.setViewFlow(this);
	}

	@Override
	public void setSelection(int position) {
		mNextScreen = INVALID_SCREEN;
		mScroller.forceFinished(true);
		if (mAdapter == null)
			return;

		position = Math.max(position, 0);
		position = Math.min(position, mAdapter.getCount() - 1);

		ArrayList<View> recycleViews = new ArrayList<View>();
		View recycleView;
		while (!mLoadedViews.isEmpty()) {
			recycleViews.add(recycleView = mLoadedViews.remove());
			detachViewFromParent(recycleView);
		}

		View currentView = makeAndAddView(position, true,
				(recycleViews.isEmpty() ? null : recycleViews.remove(0)));
		mLoadedViews.addLast(currentView);

		for (int offset = 1; mSideBuffer - offset >= 0; offset++) {
			int leftIndex = position - offset;
			int rightIndex = position + offset;
			if (leftIndex >= 0)
				mLoadedViews
						.addFirst(makeAndAddView(
								leftIndex,
								false,
								(recycleViews.isEmpty() ? null : recycleViews
										.remove(0))));
			if (rightIndex < mAdapter.getCount())
				mLoadedViews
						.addLast(makeAndAddView(rightIndex, true, (recycleViews
								.isEmpty() ? null : recycleViews.remove(0))));
		}

		mCurrentBufferIndex = mLoadedViews.indexOf(currentView);
		mCurrentAdapterIndex = position;

		for (View view : recycleViews) {
			removeDetachedView(view, false);
		}
		requestLayout();
		setVisibleView(mCurrentBufferIndex, false);
		if (mIndicator != null && mCurrentAdapterIndex < mLoadedViews.size()) {// �޸�������Խ���bug��yansw��130516
			mIndicator.onSwitched(mLoadedViews.get(mCurrentBufferIndex),
					mCurrentAdapterIndex);
		}
		if (mViewSwitchListener != null) {
			mViewSwitchListener
					.onSwitched(mLoadedViews.get(mCurrentBufferIndex),
							mCurrentAdapterIndex);
		}
	}

	private void resetFocus() {
		logBuffer();
		mLoadedViews.clear();
		removeAllViewsInLayout();

		for (int i = Math.max(0, mCurrentAdapterIndex - mSideBuffer); i < Math
				.min(mAdapter.getCount(), mCurrentAdapterIndex + mSideBuffer
						+ 1); i++) {
			mLoadedViews.addLast(makeAndAddView(i, true, null));
			if (i == mCurrentAdapterIndex)
				mCurrentBufferIndex = mLoadedViews.size() - 1;
		}
		logBuffer();
		requestLayout();
	}

	private void postViewSwitched(int direction) {
		Log.d("lmn", "direction = " + direction);
		Log.d("lmn", "mCurrentAdapterIndex = " + mCurrentAdapterIndex);
		Log.d("lmn", "mAdapter.getCount() = " + mAdapter.getCount());
		if (next) {
			Log.d("lmn", "next ===== ");
		}
		if (direction == 0 && next
				&& mCurrentAdapterIndex == mAdapter.getCount() - 1) {
			Log.d("lmn", "next~~~~~~~~~~~~~~~");
			if (gd != null)
				gd.GuideToEndPic();
		}

		if (direction == 0)
			return;

		if (direction > 0) { // to the right
			mCurrentAdapterIndex++;
			mCurrentBufferIndex++;

			View recycleView = null;

			// Remove view outside buffer range
			if (mCurrentAdapterIndex > mSideBuffer) {
				recycleView = mLoadedViews.removeFirst();
				detachViewFromParent(recycleView);
				// removeView(recycleView);
				mCurrentBufferIndex--;
			}

			// Add new view to buffer
			int newBufferIndex = mCurrentAdapterIndex + mSideBuffer;
			if (newBufferIndex < mAdapter.getCount())
				mLoadedViews.addLast(makeAndAddView(newBufferIndex, true,
						recycleView));

		} else { // to the left
			mCurrentAdapterIndex--;
			mCurrentBufferIndex--;
			View recycleView = null;

			// Remove view outside buffer range
			if (mAdapter.getCount() - 1 - mCurrentAdapterIndex > mSideBuffer) {
				recycleView = mLoadedViews.removeLast();
				detachViewFromParent(recycleView);
			}

			// Add new view to buffer
			int newBufferIndex = mCurrentAdapterIndex - mSideBuffer;
			if (newBufferIndex > -1) {
				mLoadedViews.addFirst(makeAndAddView(newBufferIndex, false,
						recycleView));
				mCurrentBufferIndex++;
			}

		}

		requestLayout();
		setVisibleView(mCurrentBufferIndex, true);
		if (mIndicator != null) {
			mIndicator.onSwitched(mLoadedViews.get(mCurrentBufferIndex),
					mCurrentAdapterIndex);
		}
		if (mViewSwitchListener != null) {
			mViewSwitchListener
					.onSwitched(mLoadedViews.get(mCurrentBufferIndex),
							mCurrentAdapterIndex);
		}
		logBuffer();
	}

	private View setupChild(View child, boolean addToEnd, boolean recycle) {
		ViewGroup.LayoutParams p = (ViewGroup.LayoutParams) child
				.getLayoutParams();
		if (p == null) {
			p = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 0);
		}
		if (recycle)
			attachViewToParent(child, (addToEnd ? -1 : 0), p);
		else
			addViewInLayout(child, (addToEnd ? -1 : 0), p, true);
		return child;
	}

	private View makeAndAddView(int position, boolean addToEnd, View convertView) {
		View view = mAdapter.getView(position, convertView, this);
		if (view == null)
			return null;
		return setupChild(view, addToEnd, convertView != null);
	}

	/**
	 * 内部类
	 * @author miaowei
	 *
	 */
	class AdapterDataSetObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			View v = getChildAt(mCurrentBufferIndex);
			if (v != null) {
				for (int index = 0; index < mAdapter.getCount(); index++) {
					if (v.equals(mAdapter.getItem(index))) {
						mCurrentAdapterIndex = index;
						break;
					}
				}
			}
			resetFocus();
		}

		@Override
		public void onInvalidated() {
			// Not yet implemented!
		}
	}

	private void logBuffer() {
		Log.d("viewflow", "Size of mLoadedViews: " + mLoadedViews.size()
				+ "X: " + mScroller.getCurrX() + ", Y: " + mScroller.getCurrY());
		Log.d("viewflow", "IndexInAdapter: " + mCurrentAdapterIndex
				+ ", IndexInBuffer: " + mCurrentBufferIndex);
	}
}
