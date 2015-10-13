package biz.mobidev.framework.view;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Scroller;
import biz.mobidev.framework.utils.Log;

@SuppressWarnings("deprecation")
public class ShoutGalleryView extends AdapterView<BaseAdapter> {

	public boolean mAlwaysOverrideTouch = true;

	private int mLeftViewIndex = -1;
	private int mRightViewIndex = 0;
	protected int mCurrentX;
	protected int mNextX;
	private int mMaxX = Integer.MAX_VALUE;
	private int mDisplayOffset = 0;
	protected Scroller mScroller;
	private GestureDetector mGesture;
	private Queue<View> mRemovedViewQueue = new LinkedList<View>();
	private OnItemSelectedListener mOnItemSelected;
	private OnItemClickListener mOnItemClicked;
	private OnItemLongClickListener mOnItemLongClicked;
	private boolean mDataChanged = false;
	private int mSelectionIndex = -1;
	private final int SPACE = 0;
	private int mNeedAnimate = -1;
	private int mSpaceDpi;
	private BaseAdapter mAdapter;
	private int mRemooveItemIndex = -1;

	// ****************************************************************
	ShoutGalleryView(Context context) {
		super(context);
		init();
	}

	public ShoutGalleryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ShoutGalleryView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private synchronized void init() {
		mSpaceDpi = (int) (getResources().getDisplayMetrics().density * SPACE);
		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mDisplayOffset = 0;
		mCurrentX = 0;
		mNextX = 0;
		mMaxX = Integer.MAX_VALUE;
		mScroller = new Scroller(getContext());
		mGesture = new GestureDetector(getContext(), mOnGesture);
	}

	@Override
	public BaseAdapter getAdapter() {

		return mAdapter;
	}

	@Override
	public void setAdapter(BaseAdapter adapter) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataObserver);
		}
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataObserver);
		reset();
	}

	@Override
	public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
		mOnItemSelected = listener;
	}

	@Override
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
		mOnItemClicked = listener;
	}

	@Override
	public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
		mOnItemLongClicked = listener;
	}

	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			synchronized (ShoutGalleryView.this) {
				mDataChanged = true;
			}
			invalidate();
			requestLayout();
		}

		@Override
		public void onInvalidated() {
			reset();
			invalidate();
			requestLayout();
		}

	};

	@Override
	public View getSelectedView() {
		return null;
	}

	private synchronized void reset() {
		init();
		removeAllViewsInLayout();
		requestLayout();
	}

	@Override
	public Object getSelectedItem() {
		Object result = null;
		if (mSelectionIndex != -1&& mAdapter.getCount()>mSelectionIndex) {
			result = mAdapter.getItem(mSelectionIndex);
		}
		return result;
	}

	@Override
	public void setSelection(int position) {

		mSelectionIndex = position;
	}

	private void addAndMeasureChild(final View child, int viewPos) {
		LayoutParams params = child.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}

		addViewInLayout(child, viewPos, params, true);
		child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}

	public synchronized void removeItem(final Object object, final View view) {
//		final int index = mAdapter.getSourceList().indexOf(object);
//		mRemooveItemIndex = index;
		// CountDownTimer countDownTimer = new CountDownTimer(3000, 100) {
		//
		// @Override
		// public void onTick(long millisUntilFinished) {
		// int width = view.getMeasuredWidth();
		// Log.d(width);
		// int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width-10, MeasureSpec.EXACTLY);
		// view.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
		// }
		//
		// @Override
		// public void onFinish() {
		//
		// }
		// };
		// countDownTimer.start();
		// mAdapter.notifyDataSetInvalidated();
//		final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.close_scale);
//		animation.setAnimationListener(new AnimationListener() {
//
//			@Override
//			public void onAnimationStart(Animation animation) {
//				// mSelectionIndex=-1;
//				view.setSelected(false);
//				mAdapter.getSourceList().remove(object);
//			}
//
//			@Override
//			public void onAnimationRepeat(Animation animation) {
//
//			}
//
//			@Override
//			public void onAnimationEnd(Animation animation) {
//				if (index >= mAdapter.getSourceList().size()) {
//					mSelectionIndex = mAdapter.getSourceList().size() - 1;
//				} else {
//					mSelectionIndex = index;
//				}
//				mRemooveItemIndex = -1;
//				mAdapter.notifyDataSetChanged();
//				if (mAdapter.getSourceList().size() - 1 > 0) {
//					// scrollToSelectedItem();
//					// scrollTo(mScroller.getCurrX()-152);
//					scrollTest();
//				}
//			}
//		});
		// view.setVisibility(INVISIBLE);

	//	view.startAnimation(animation);

		// if (mAdapter.getSourceList().size() == 0) {
		// mSelectionIndex = -1;
		// } else if (index != mSelectionIndex) {
		//
		// if (mSelectionIndex > 0) {
		// mSelectionIndex--;
		// } else if (mSelectionIndex < mAdapter.getSourceList().size() - 1) {
		// mSelectionIndex++;
		// }
		// } else if (mSelectionIndex > mAdapter.getSourceList().size() - 1) {
		// mSelectionIndex = mAdapter.getSourceList().size() - 1;
		// }
		// view.set

	}

	public void scrollTest() {
		post(new Runnable() {
			int count = 1;

			@Override
			public void run() {
				if (count == 0) {
					requestLayout();
					return;
				} else {
					count--;
					int width = getChildAt(0).getMeasuredWidth() + getChildAt(0).getPaddingRight();
					// int newPosition = (width * (position));
					// mScroller.startScroll(mScroller.getCurrX(), 0, -150, 0, 2000);
					mScroller.fling(mNextX, 0, -500, 0, width, (int) (mNextX + (width)), 0, 0);
					Log.e(mScroller.getCurrX());
				}
				post((Runnable) this);

			}
		});
	}

	public void addItem(Object object) {
//		mAdapter.getSourceList().add((IHolderSource) object);
//		setSelection(mAdapter.getCount() - 1);
//		mAdapter.notifyDataSetChanged();
//		// requestLayout();
//		scrollToPosition(mAdapter.getCount() - 1);
	}

	@Override
	protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (mAdapter == null) {
			return;
		}

		if (mDataChanged) {
			int oldCurrentX = mCurrentX;
			init();
			removeAllViewsInLayout();
			mNextX = oldCurrentX;
			mDataChanged = false;
		}

		if (mScroller.computeScrollOffset()) {
			int scrollx = mScroller.getCurrX();
			mNextX = scrollx;
		}

		if (mNextX <= 0) {
			mNextX = 0;
			mScroller.forceFinished(true);
		}
		if (mNextX >= mMaxX) {
			mNextX = mMaxX;
			mScroller.forceFinished(true);
		}

		int dx = mCurrentX - mNextX;

		removeNonVisibleItems(dx);
		fillList(dx);
		positionItems(dx);

		mCurrentX = mNextX;

		if (!mScroller.isFinished()) {
			post(new Runnable() {
				@Override
				public void run() {
					requestLayout();
				}
			});

		}
	}

	private void fillList(final int dx) {
		int edge = 0;
		View child = getChildAt(getChildCount() - 1);
		if (child != null) {
			edge = child.getRight();
		}
		fillListRight(edge, dx);

		edge = 0;
		child = getChildAt(0);
		if (child != null) {
			edge = child.getLeft();
		}
		fillListLeft(edge, dx);

	}

	private void fillListRight(int rightEdge, final int dx) {
		while (rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount()) {

			final View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, -1);
			final int index = mRightViewIndex;
			if (index == mSelectionIndex && index != mRemooveItemIndex) {
				child.post(new Runnable() {

					@Override
					public void run() {
						if (mOnItemSelected != null && !child.isSelected()) {
							mOnItemSelected.onItemSelected(ShoutGalleryView.this, child, mSelectionIndex,
									mAdapter.getItemId(mSelectionIndex));
						}
						child.setSelected(true);
					}
				});
			} else {
				child.setSelected(false);
			}
			if (index == mNeedAnimate) {

				mNeedAnimate = -1;
			}
			rightEdge += child.getMeasuredWidth();

			if (mRightViewIndex == mAdapter.getCount() - 1) {
				mMaxX = mCurrentX + rightEdge - getWidth();
			}

			if (mMaxX < 0) {
				mMaxX = 0;
			}
			mRightViewIndex++;
		}

	}

	private void fillListLeft(int leftEdge, final int dx) {
		while (leftEdge + dx > 0 && mLeftViewIndex >= 0) {
			final View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, 0);
			final int index = mLeftViewIndex;
			if (index == mSelectionIndex && index != mRemooveItemIndex) {
				child.post(new Runnable() {

					@Override
					public void run() {
						
						if (mOnItemSelected != null && !child.isSelected()) {
							mOnItemSelected.onItemSelected(ShoutGalleryView.this, child, mSelectionIndex,
									mAdapter.getItemId(mSelectionIndex));
						}
						child.setSelected(true);
					}
				});
			} else {
				child.setSelected(false);
			}
			if (index == mNeedAnimate) {
//				Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.wave_scale);
				// child.setAnimation(animation);
		//		child.startAnimation(animation);
				mNeedAnimate = -1;
			}
			leftEdge -= child.getMeasuredWidth();
			mLeftViewIndex--;
			mDisplayOffset -= child.getMeasuredWidth();
		}
	}

	private void removeNonVisibleItems(final int dx) {
		View child = getChildAt(0);
		while (child != null && child.getRight() + dx <= 0) {
			mDisplayOffset += child.getMeasuredWidth();
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
			mLeftViewIndex++;
			child = getChildAt(0);

		}

		child = getChildAt(getChildCount() - 1);
		while (child != null && child.getLeft() + dx >= getWidth()) {
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
			mRightViewIndex--;
			child = getChildAt(getChildCount() - 1);
		}
	}

	private void positionItems(final int dx) {
		if (getChildCount() > 0) {
			mDisplayOffset += dx;
			int left = mDisplayOffset + mSpaceDpi;
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				int childWidth = child.getMeasuredWidth();
				child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
				left += childWidth + child.getPaddingRight() + mSpaceDpi;
			}
		}
	}

	public synchronized void scrollToPosition(final int position) {

		mScroller.forceFinished(true);
		post(new Runnable() {
			int count = 1;

			@Override
			public void run() {
				if (count == 0) {
					requestLayout();
					return;
				} else {
					count--;
					int width = getChildAt(0).getMeasuredWidth() + getChildAt(0).getPaddingRight();
					int newPosition = (width * (position));
					mScroller.startScroll(mNextX, 0, newPosition - mNextX, 0, 2000);
					Log.i("mNextX", mNextX, "newPosition", newPosition, "newPosition - mNextX", (newPosition - mNextX),
							"\t");
				}
				post((Runnable) this);

			}
		});
	}

	public void scrollToSelectedItem() {
		scrollToPosition(mSelectionIndex);
	}

	public synchronized void scrollTo(int x) {
		mScroller.startScroll(mNextX, 0, x - mNextX, 2000);
		requestLayout();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean handled = super.dispatchTouchEvent(ev);
		handled |= mGesture.onTouchEvent(ev);
		return handled;
	}

	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		synchronized (ShoutGalleryView.this) {
			mScroller.fling(mNextX, 0, (int) -velocityX, 0, 0, mMaxX, 0, 0);
		}
		requestLayout();

		return true;
	}

	protected boolean onDown(MotionEvent e) {
		mScroller.forceFinished(true);
		return true;
	}

	public void setNeedAnimate() {
		mNeedAnimate = mSelectionIndex;
	}

	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDown(MotionEvent e) {
			return ShoutGalleryView.this.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return ShoutGalleryView.this.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

			synchronized (ShoutGalleryView.this) {
				mNextX += (int) distanceX;
			}
			requestLayout();

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if (isEventWithinView(e, child)) {
					if (mSelectionIndex != mLeftViewIndex + 1 + i) {
						if (mLeftViewIndex < mSelectionIndex + 1
								&& mSelectionIndex <= (mLeftViewIndex + 1) + (getChildCount() - 1)) {
							View oldSelectedView = getChildAt(mSelectionIndex - (mLeftViewIndex + 1));
							{
								if (oldSelectedView != null) {
									oldSelectedView.setSelected(false);
								}
							}
						}
						setSelection(mLeftViewIndex + 1 + i);
						child.setSelected(true);
						// mAdapter.notifyDataSetChanged();
					}
					if ((mOnItemClicked != null) && (mLeftViewIndex + 1 + i != mRemooveItemIndex)) {

						mOnItemClicked.onItemClick(ShoutGalleryView.this, child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					if ((mOnItemSelected != null)  && (mLeftViewIndex + 1 + i != mRemooveItemIndex)){
						mOnItemSelected.onItemSelected(ShoutGalleryView.this, child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					break;
				}

			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				if (isEventWithinView(e, child)) {
					if (mOnItemLongClicked != null) {
						mOnItemLongClicked.onItemLongClick(ShoutGalleryView.this, child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					View oldSelectedView = getChildAt(mSelectionIndex - (mLeftViewIndex + 1));
					if (oldSelectedView != null) {
						oldSelectedView.setSelected(false);
					}
					setSelection(mLeftViewIndex + 1 + i);
					child.setSelected(true);
					break;
				}

			}
		}

		private boolean isEventWithinView(MotionEvent e, View child) {
			Rect viewRect = new Rect();
			int[] childPosition = new int[2];
			child.getLocationOnScreen(childPosition);
			int left = childPosition[0];
			int right = left + child.getWidth();
			int top = childPosition[1];
			int bottom = top + child.getHeight();
			viewRect.set(left, top, right, bottom);
			return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
		}
	};

}
