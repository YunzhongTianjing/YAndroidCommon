package y.androidcommon;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.ListView;

@SuppressLint("ClickableViewAccessibility")
public class PTRListView extends ListView {

	private View mHeaderContentView;

	private int mInitHeaderMarginTop;

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
		final View headOutterView = View.inflate(getContext(),
				R.layout.headview, null);
		mHeaderContentView = headOutterView.findViewById(R.id.tv);
		addHeaderView(headOutterView, null, false);

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

	private float mDownY;
	private float mPreMoveY;

	private float mScrollNotDragOffset;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownY = mPreMoveY = ev.getY();
			mScrollNotDragOffset = 0;
			return super.onTouchEvent(ev);

		case MotionEvent.ACTION_MOVE: {
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
					return super.onTouchEvent(ev);
				}

				mScrollNotDragOffset += deltaMoveY;
				return super.onTouchEvent(ev);
			}
			return super.onTouchEvent(ev);
		}

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			foldHeaderView();
			return super.onTouchEvent(ev);

		default:
			return super.onTouchEvent(ev);
		}
	}

	private boolean drag(final float curY) {
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

	private void foldHeaderView() {
		final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mHeaderContentView
				.getLayoutParams();
		final int startMargin = params.topMargin;
		final ValueAnimator animator = ValueAnimator.ofInt(startMargin,
				mInitHeaderMarginTop).setDuration(120);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int curMargin = (Integer) animation.getAnimatedValue();
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mHeaderContentView
						.getLayoutParams();
				params.topMargin = curMargin;
				mHeaderContentView.setLayoutParams(params);
			}
		});
		animator.start();
	}

	private void setHeadViewMarginTop(float newMarginTop) {
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mHeaderContentView
				.getLayoutParams();
		params.topMargin = (int) newMarginTop;
		mHeaderContentView.setLayoutParams(params);
	}

	private int getHeadViewMarginTop() {
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mHeaderContentView
				.getLayoutParams();
		return params.topMargin;
	}

	private int getHeadViewHeight() {
		return getHeadViewMarginTop() - mInitHeaderMarginTop;
	}

}
