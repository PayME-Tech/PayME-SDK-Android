package vn.payme.sdk.cardmodules;

import android.content.Context;
import android.util.AttributeSet;

import vn.payme.sdk.R;


public class OverlayWhite extends Overlay {

	int backgroundColorId = R.color.white_background;
	int cornerColorId = R.color.gray;

	public OverlayWhite(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		cornerDp = 3;
	}

	public void setColorIds(int backgroundColorId, int cornerColorId) {
		this.backgroundColorId = backgroundColorId;
		this.cornerColorId = cornerColorId;
		postInvalidate();
	}

	@Override
	protected int getBackgroundColorId() {
		return backgroundColorId;
	}

	@Override
	protected int getCornerColorId() {
		return cornerColorId;
	}
}
