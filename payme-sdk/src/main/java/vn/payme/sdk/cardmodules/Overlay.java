package vn.payme.sdk.cardmodules;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import vn.payme.sdk.R;


public class Overlay extends View {

	private RectF rect;
	private final RectF oval = new RectF();
	private int radius;
	private final Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

	int cornerDp = 3;

	boolean drawCorners = true;

  private final Handler handler;
  private final Runnable refreshRunnable;
  private final int DELAY = 0;

	public Overlay(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		handler = new Handler();
		refreshRunnable = new Runnable() {
      @Override
      public void run() {
        refreshView();
      }
    };
	}

	protected int getBackgroundColorId() {
		return R.color.camera_background;
	}

	protected int getCornerColorId() {
		return R.color.corner_color;
	}

	public void setCircle(RectF rect, int radius) {
		this.rect = rect;
		this.radius = radius;
		postInvalidate();
	}

	private int dpToPx(int dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}

  @Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (rect != null) {
			Paint paintAntiAlias = new Paint(Paint.ANTI_ALIAS_FLAG);
			paintAntiAlias.setColor(getResources().getColor(getBackgroundColorId()));
			paintAntiAlias.setStyle(Paint.Style.FILL);
			canvas.drawPaint(paintAntiAlias);

			paintAntiAlias.setXfermode(xfermode);
			canvas.drawRoundRect(rect, radius, radius, paintAntiAlias);

			if (!drawCorners) {
				return;
			}

			Paint paint = new Paint();
			paint.setColor(getResources().getColor(getCornerColorId()));
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(dpToPx(cornerDp));

			int lineLength = dpToPx(2);
			float rectHeight = rect.bottom - rect.top;
			mHeight = Math.round(rectHeight / 5);
			int rectVerticalPadding = Math.round(2 * rectHeight / 5);

      // top left
      float x = rect.left + dpToPx(20);
			float y = rect.top + rectVerticalPadding;
			oval.left = x;
			oval.top = y;
			oval.right = x + 2 * radius;
			oval.bottom = y + 2 * radius;
			canvas.drawArc(oval, 180, 90, false, paint);
			canvas.drawLine(oval.left, oval.bottom - radius, oval.left,
					oval.bottom - radius + lineLength, paint);
			canvas.drawLine(oval.right - radius, oval.top,
					oval.right - radius + lineLength, oval.top, paint);

			// top right
			x = rect.right - dpToPx(20) - 2 * radius;
			y = rect.top + rectVerticalPadding;
			oval.left = x;
			oval.top = y;
			oval.right = x + 2 * radius;
			oval.bottom = y + 2 * radius;
			canvas.drawArc(oval, 270, 90, false, paint);
			canvas.drawLine(oval.right, oval.bottom - radius, oval.right,
					oval.bottom - radius + lineLength, paint);
			canvas.drawLine(oval.right - radius, oval.top,
					oval.right - radius - lineLength, oval.top, paint);

			// bottom right
			x = rect.right - dpToPx(20) - 2 * radius;
			y = rect.bottom - rectVerticalPadding - 2 * radius;
			oval.left = x;
			oval.top = y;
			oval.right = x + 2 * radius;
			oval.bottom = y + 2 * radius;
			canvas.drawArc(oval, 0, 90, false, paint);
			canvas.drawLine(oval.right, oval.bottom - radius, oval.right,
					oval.bottom - radius - lineLength, paint);
			canvas.drawLine(oval.right - radius, oval.bottom,
					oval.right - radius - lineLength, oval.bottom, paint);

			// bottom left
			x = rect.left + dpToPx(20);
			y = rect.bottom - rectVerticalPadding - 2 * radius;
			oval.left = x;
			oval.top = y;
			oval.right = x + 2 * radius;
			oval.bottom = y + 2 * radius;
			canvas.drawArc(oval, 90, 90, false, paint);
			canvas.drawLine(oval.left, oval.bottom - radius, oval.left,
					oval.bottom - radius - lineLength, paint);
			canvas.drawLine(oval.right - radius, oval.bottom,
					oval.right - radius + lineLength, oval.bottom, paint);

			y = rect.top + rectVerticalPadding + mPosY;
			canvas.drawLine(rect.left + dpToPx(40), y, rect.right - dpToPx(40), y, paint);
      handler.postDelayed(refreshRunnable, DELAY);
		}
	}

  private int mPosY = 0;
  private boolean isGoingDown = true;
  private int mHeight;

  public void startAnimation() {
    this.invalidate();
  }

  public void stopAnimation() {
    reset();
    this.invalidate();
  }

  private void reset() {
    mPosY = 0;
    isGoingDown = true;
  }
  private void refreshView() {
    //Update new position of the line
    if (isGoingDown) {
      mPosY += 5;
      if (mPosY > mHeight) {
        //We invert the direction of the animation
        mPosY = mHeight;
        isGoingDown = false;
      }
    } else {
        mPosY -= 5;
        if (mPosY < 0) {
          //We invert the direction of the animation
          mPosY = 0;
          isGoingDown = true;
        }
    }
    this.invalidate();
  }
}
