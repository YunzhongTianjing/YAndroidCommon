package y.androidcommon;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ListView;

@SuppressLint("ClickableViewAccessibility")
public class PTRListView extends ListView {
	private static final int DURATION_FOLD_BASE_ANIMATION = 800;

	private int mInitHeaderMarginTop;
	private View mHeaderContentView;
	private ViewGroup mHeaderFrameView;

	private float mDownY;
	private float mPreMoveY;
	private float mScrollNotDragOffset;

	private ValueAnimator mCurAnimator;

	private boolean mDragging;

	public PTRListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public PTRListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PTRListView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setVerticalFadingEdgeEnabled(false);
		mHeaderFrameView = new FrameLayout(getContext());
		addHeaderView(mHeaderFrameView, null, false);
		// XXX
		setRefreshPromptView(R.layout.headview);

		getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						mInitHeaderMarginTop = -mHeaderContentView.getHeight();
						setHeadViewMarginTop(mInitHeaderMarginTop);
						// only record once
						getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
					}
				});
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownY = mPreMoveY = ev.getY();
			mScrollNotDragOffset = 0;
			foldHeaderView(mInitHeaderMarginTop);
			return super.onTouchEvent(ev);

		case MotionEvent.ACTION_MOVE: {
			if (null != mCurAnimator)
				return true;

			final float curMoveY = ev.getY();
			final float deltaMoveY = curMoveY - mPreMoveY;
			mPreMoveY = curMoveY;

			if (getHeadViewHeight() > 0)
				return drag(curMoveY);

			if (getHeadViewHeight() == 0) {
				if (deltaMoveY > 0) {

					if (0 == getFirstVisiblePosition())// init state
						return drag(curMoveY);

					mScrollNotDragOffset += deltaMoveY;
					mDragging = false;
					return super.onTouchEvent(ev);
				}

				mScrollNotDragOffset += deltaMoveY;
				mDragging = false;
				return super.onTouchEvent(ev);
			}
			// code below no execute chance
			throw new RuntimeException("strange");
		}

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mDragging = false;
			if (getHeadViewMarginTop() < 0)
				foldHeaderView(mInitHeaderMarginTop);// 完全收起
			else
				foldHeaderView(0);// 悬停刷新
			return super.onTouchEvent(ev);

		default:
			return super.onTouchEvent(ev);
		}
	}

	private boolean drag(final float curY) {
		mDragging = true;
		/*
		 * curY -
		 * downY包含两部分：1.滚动的(当down时列表第一个可见条目不是0，这是交给系统处理滚动listview，这部分的scrollY应该予以刨除
		 * )、2.拖动的
		 */
		final float deltaY = curY - mDownY - mScrollNotDragOffset;
		float marginTop = deltaY / 2 + mInitHeaderMarginTop;// 位移减半，阻尼效果
		marginTop = marginTop < mInitHeaderMarginTop ? mInitHeaderMarginTop
				: marginTop;
		setHeadViewMarginTop(marginTop);
		return true;
	}

	private void foldHeaderView(final int endMarginTop) {
		if (null != mCurAnimator)
			return;

		if (mDragging)
			return;

		final int startMargin = getHeadViewMarginTop();
		if (endMarginTop == startMargin)
			return;

		final float duration = DURATION_FOLD_BASE_ANIMATION
				* (startMargin - endMarginTop) / getHeight();
		mCurAnimator = ValueAnimator.ofInt(startMargin, endMarginTop)
				.setDuration((long) duration);
		mCurAnimator.setInterpolator(new AccelerateInterpolator());
		mCurAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int curMargin = (Integer) animation.getAnimatedValue();
				setHeadViewMarginTop(curMargin);
			}
		});
		mCurAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurAnimator = null;
			}
		});
		mCurAnimator.start();
	}

	private void setHeadViewMarginTop(float newMarginTop) {
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mHeaderContentView
				.getLayoutParams();
		params.topMargin = (int) newMarginTop;
		mHeaderContentView.setLayoutParams(params);
	}

	private int getHeadViewMarginTop() {
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mHeaderContentView
				.getLayoutParams();
		return params.topMargin;
	}

	private int getHeadViewHeight() {
		return getHeadViewMarginTop() - mInitHeaderMarginTop;
	}

	public void setRefreshPromptView(int layoutId) {
		mHeaderContentView = View.inflate(getContext(), layoutId, null);

		mHeaderFrameView.removeAllViews();
		mHeaderFrameView.addView(mHeaderContentView);
	}

	public void notifyCompleteRefresh() {
		foldHeaderView(mInitHeaderMarginTop);
	}

}
