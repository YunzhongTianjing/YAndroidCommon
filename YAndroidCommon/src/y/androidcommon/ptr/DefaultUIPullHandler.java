package y.androidcommon.ptr;

import y.androidcommon.R;
import y.androidcommon.ptr.PTRListView.IUIPullHandler;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

final class DefaultUIPullHandler extends FrameLayout implements IUIPullHandler {

	private View tvPrompt;
	private View pbPrompt;

	public DefaultUIPullHandler(Context context) {
		super(context);
		init();
	}

	private void init() {
		View.inflate(getContext(), R.layout.headerview, this);
		tvPrompt = findViewById(R.id.tv);
		pbPrompt = findViewById(R.id.pb);
	}

	@Override
	public void onUIStartRefresh() {
		tvPrompt.setVisibility(View.GONE);
		pbPrompt.setVisibility(View.VISIBLE);
	}

	@Override
	public void onUIPullViewHeightChange(boolean byUser, int oldHeaderHeight,
			int currentHeaderHeight, float oldPercent, float currentPercent) {
		if (0 == currentHeaderHeight) {
			tvPrompt.setVisibility(View.VISIBLE);
			pbPrompt.setVisibility(View.GONE);
		}
	}

}
