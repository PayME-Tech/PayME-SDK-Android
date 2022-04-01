package vn.payme.sdk.cardmodules;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import vn.payme.sdk.R;

/**
 * Any classes that subclass this must:
 * <p>
 * (1) set mIsPermissionCheckDone after the permission check is done, which should be sometime
 * before "onResume" is called
 * <p>
 * (2) Call setViewIds to set these resource IDs and initalize appropriate handlers
 */
abstract class ScanBaseActivity extends Activity implements Camera.PreviewCallback,
		View.OnClickListener, OnScanListener, OnObjectListener, OnCameraOpenListener {

	public static final String IS_OCR = "is_ocr";
	public static final String RESULT_FATAL_ERROR = "result_fatal_error";
	public static final String RESULT_CAMERA_OPEN_ERROR = "result_camera_open_error";

	private Camera mCamera = null;
	private OrientationEventListener mOrientationEventListener;
	private static MachineLearningThread machineLearningThread = null;
	private final Semaphore mMachineLearningSemaphore = new Semaphore(1);
	private int mRotation;
	private boolean mSentResponse = false;
	private boolean mIsActivityActive = false;
	private HashMap<String, Integer> numberResults = new HashMap<>();
	private HashMap<Expiry, Integer> expiryResults = new HashMap<>();
	private long firstResultMs = 0;
	private int mFlashlightId;
	private int mCardNumberId;
	private int mExpiryId;
	private int mTextureId;
	private float mRoiCenterYRatio;
	private CameraThread mCameraThread = null;
	private boolean mIsOcr = true;

	public boolean wasPermissionDenied = false;
	public String denyPermissionTitle;
	public String denyPermissionMessage;
	public String denyPermissionButton;

	public long mPredictionStartMs = 0;
	public boolean mIsPermissionCheckDone = false;
	protected boolean mShowNumberAndExpiryAsScanning = true;

	protected File objectDetectFile;

public long errorCorrectionDurationMs = 2500;
	public int flashCorrectionDurationMs = 4000;

	private TextView guideText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		denyPermissionTitle = getString(R.string.number_card_error);
		denyPermissionMessage = "";
		denyPermissionButton = getString(R.string.understood);
		mIsOcr = getIntent().getBooleanExtra(IS_OCR, true);

		mOrientationEventListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int orientation) {
				orientationChanged(orientation);
			}
		};
	}

	class MyGlobalListenerClass implements ViewTreeObserver.OnGlobalLayoutListener {
		private final int cardRectangleId;
		private final int overlayId;

		MyGlobalListenerClass(int cardRectangleId, int overlayId) {
			this.cardRectangleId = cardRectangleId;
			this.overlayId = overlayId;
		}

		@Override
		public void onGlobalLayout() {
			int[] xy = new int[2];
			View view = findViewById(cardRectangleId);
			view.getLocationInWindow(xy);

			// convert from DP to pixels
			int radius = (int) (11 * Resources.getSystem().getDisplayMetrics().density);
			RectF rect = new RectF(xy[0], xy[1],
					xy[0] + view.getWidth(),
					xy[1] + view.getHeight());
			Overlay overlay = findViewById(overlayId);
			overlay.setCircle(rect, radius);
			overlay.startAnimation();

			ScanBaseActivity.this.mRoiCenterYRatio =
					(xy[1] + view.getHeight() * 0.5f) / overlay.getHeight();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			mIsPermissionCheckDone = true;
		} else {
			wasPermissionDenied = true;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(this.denyPermissionMessage)
					.setTitle(this.denyPermissionTitle);
			builder.setPositiveButton(this.denyPermissionButton, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// just let the user click on the back button manually
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	@Override
	public void onCameraOpen(@Nullable Camera camera) {
		if (camera == null) {
			Intent intent = new Intent();
			intent.putExtra(RESULT_CAMERA_OPEN_ERROR, true);
			setResult(RESULT_CANCELED, intent);
			finish();
		} else if (!mIsActivityActive) {
			camera.release();
		} else {
			mCamera = camera;
			setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK,
					mCamera);
			// Create our Preview view and set it as the content of our activity.
			CameraPreview cameraPreview = new CameraPreview(this, this);
			FrameLayout preview = findViewById(mTextureId);
      guideText = (TextView) findViewById(R.id.positionCard);
      guideText.setText(getString(R.string.card_scanner_hint));
			preview.addView(cameraPreview);
			mCamera.setPreviewCallback(this);
		}
	}


	protected void startCamera() {
		numberResults = new HashMap<>();
		expiryResults = new HashMap<>();
		firstResultMs = 0;
		if (mOrientationEventListener.canDetectOrientation()) {
			mOrientationEventListener.enable();
		}

		try {
			if (mIsPermissionCheckDone) {
				if (mCameraThread == null) {
					mCameraThread = new CameraThread();
					mCameraThread.start();
				}
				mCameraThread.startCamera(this);
			}
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("")
					.setTitle("");
			builder.setPositiveButton("", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					finish();
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}

		mOrientationEventListener.disable();
		mIsActivityActive = false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		mIsActivityActive = true;
		firstResultMs = 0;
		numberResults = new HashMap<>();
		expiryResults = new HashMap<>();
		mSentResponse = false;

		if (findViewById(mCardNumberId) != null) {
			findViewById(mCardNumberId).setVisibility(View.INVISIBLE);
		}
		if (findViewById(mExpiryId) != null) {
			findViewById(mExpiryId).setVisibility(View.INVISIBLE);
		}

		startCamera();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void setViewIds(int cardRectangleId, int overlayId, int textureId,
						   int cardNumberId, int expiryId) {
		mTextureId = textureId;
		mCardNumberId = cardNumberId;
		mExpiryId = expiryId;
		findViewById(cardRectangleId).getViewTreeObserver()
				.addOnGlobalLayoutListener(new MyGlobalListenerClass(cardRectangleId, overlayId));
	}

	public void orientationChanged(int orientation) {
		if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;
		Camera.CameraInfo info =
				new Camera.CameraInfo();
		Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
		orientation = (orientation + 45) / 90 * 90;
		int rotation;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			rotation = (info.orientation - orientation + 360) % 360;
		} else {  // back-facing camera
			rotation = (info.orientation + orientation) % 360;
		}

		if (mCamera != null) {
			try {
				Camera.Parameters params = mCamera.getParameters();
				params.setRotation(rotation);
				mCamera.setParameters(params);
			} catch (Exception | Error e) {
				// This gets called often so we can just swallow it and wait for the next one
				e.printStackTrace();
			}
		}
	}

	public void setCameraDisplayOrientation(Activity activity,
											int cameraId, Camera camera) {
		Camera.CameraInfo info =
				new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
		mRotation = result;
	}

	static public void warmUp(Context context) {
		getMachineLearningThread().warmUp(context);
	}

	static public MachineLearningThread getMachineLearningThread() {
		if (machineLearningThread == null) {
			machineLearningThread = new MachineLearningThread();
			new Thread(machineLearningThread).start();
		}

		return machineLearningThread;
	}

	@Override
	public void onPreviewFrame(byte[] bytes, Camera camera) {
		if (mMachineLearningSemaphore.tryAcquire()) {

			MachineLearningThread mlThread = getMachineLearningThread();

			Camera.Parameters parameters = camera.getParameters();
			int width = parameters.getPreviewSize().width;
			int height = parameters.getPreviewSize().height;
			int format = parameters.getPreviewFormat();

			mPredictionStartMs = SystemClock.uptimeMillis();

			// Use the application context here because the machine learning thread's lifecycle
			// is connected to the application and not this activity
			if (mIsOcr) {
				mlThread.post(bytes, width, height, format, mRotation, this,
						this.getApplicationContext(), mRoiCenterYRatio);
			} else {
				mlThread.post(bytes, width, height, format, mRotation, this,
						this.getApplicationContext(), mRoiCenterYRatio, objectDetectFile);
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (mCamera != null && mFlashlightId == view.getId()) {
			Camera.Parameters parameters = mCamera.getParameters();
			if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			} else {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			}
			mCamera.setParameters(parameters);
			mCamera.startPreview();
		}

	}

	@Override
	public void onBackPressed() {
		if (!mSentResponse && mIsActivityActive) {
			mSentResponse = true;
			Intent intent = new Intent();
			setResult(RESULT_CANCELED, intent);
			finish();
		}
	}

	private final List<String> cardPrefix = Arrays.asList(
  			"9704","4","51","52", "53", "54", "55", "2221", "2229", "223", "229", "23", "26", "270", "271", "2720", "2131", "1800", "35"
  	);
  	@VisibleForTesting()
  	public boolean incrementNumber(String number) {
  		for (String val : cardPrefix) {
  			String optimizedNum;
  			if (number.startsWith("9")) {
  				optimizedNum = "9704" + number.substring(4);
  			} else {
  				optimizedNum = number;
  			}
  			int numberLength = optimizedNum.length();
  			if (optimizedNum.startsWith(val)
  					&& ((val.equals("9704") && numberLength == 16)
  						|| (!val.equals("9704") && numberLength >= 13 && numberLength <= 16)
  			)) {
  				Integer currentValue = numberResults.get(optimizedNum);
  				if (currentValue == null) {
  					currentValue = 0;
  				}
  				numberResults.put(optimizedNum, currentValue + 1);
				return currentValue + 1 == 2;
			}
  		}
  		return false;
  	}


	@VisibleForTesting()
	public void incrementExpiry(Expiry expiry) {
		Integer currentValue = expiryResults.get(expiry);
		if (currentValue == null) {
			currentValue = 0;
		}

		expiryResults.put(expiry, currentValue + 1);
	}

	@VisibleForTesting()
	public String getNumberResult() {
		// Ugg there has to be a better way
		String result = null;
		int maxValue = 0;

		for (String number : numberResults.keySet()) {
			int value = 0;
			Integer count = numberResults.get(number);
			if (count != null) {
				value = count;
			}
			if (value > maxValue) {
				result = number;
				maxValue = value;
			}
		}

		return result;
	}

	@VisibleForTesting()
	public Expiry getExpiryResult() {
		Expiry result = null;
		int maxValue = 0;

		for (Expiry expiry : expiryResults.keySet()) {
			int value = 0;
			Integer count = expiryResults.get(expiry);
			if (count != null) {
				value = count;
			}
			if (value > maxValue) {
				result = expiry;
				maxValue = value;
			}
		}

		return result;
	}

	protected abstract void onCardScanned(String numberResult, String month, String year);

	protected void setNumberAndExpiryAnimated(long duration) {
		String numberResult = getNumberResult();
		Expiry expiryResult = getExpiryResult();
		TextView textView = findViewById(mCardNumberId);

		if (expiryResult != null && duration >= (errorCorrectionDurationMs / 2)) {
			textView = findViewById(mExpiryId);
		}
	}

	@Override
	public void onFatalError() {
		Intent intent = new Intent();
		intent.putExtra(RESULT_FATAL_ERROR, true);
		setResult(RESULT_CANCELED, intent);
		finish();
	}

  private boolean hadResult = false;
  private long flashCountMs = 0;
	@Override
	public void onPrediction(final CardScanned cardScanned) {
		if (!mSentResponse && mIsActivityActive) {
		  if (cardScanned.didStartScan && firstResultMs == 0) {
        firstResultMs = SystemClock.uptimeMillis();
      }

			if (cardScanned.number != null) {
        hadResult = incrementNumber(cardScanned.number);
      }

			if (hadResult) {
        String numberResult = getNumberResult();
        Expiry expiryResult = getExpiryResult();
        String month = null;
        String year = null;
        if (expiryResult != null) {
          month = Integer.toString(expiryResult.getMonth());
          year = Integer.toString(expiryResult.getYear());
        }

        if (numberResult != null) {
          mSentResponse = true;
          onCardScanned(numberResult, month, year);
        }
      } else {
        long duration = SystemClock.uptimeMillis() - firstResultMs;
        long flashDuration = SystemClock.uptimeMillis() - flashCountMs;
        if (firstResultMs != 0 && mShowNumberAndExpiryAsScanning) {
          setNumberAndExpiryAnimated(duration);
        }
        if (firstResultMs != 0 && duration >= 1000 && duration < errorCorrectionDurationMs) {
          guideText.setText(getString(R.string.card_scanning));
        }

        if (firstResultMs != 0 && duration >= errorCorrectionDurationMs) {
         if (flashCountMs == 0) {
            Camera.Parameters camParams = mCamera.getParameters();
            camParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(camParams);
            flashCountMs = SystemClock.uptimeMillis();
            guideText.setText(getString(R.string.card_scanner_hint2));
          } else {
            if (flashDuration >= flashCorrectionDurationMs) {
              mSentResponse = true;
              Log.d("DEBUG_", String.valueOf(numberResults));
              String numberResult = getNumberResult();
              Expiry expiryResult = getExpiryResult();
              String month = null;
              String year = null;
              if (expiryResult != null) {
                month = Integer.toString(expiryResult.getMonth());
                year = Integer.toString(expiryResult.getYear());
              }
              if (numberResult != null) {
                onCardScanned(numberResult, month, year);
              } else {
                onCardScanned(null, "", "");
              }
            }
          }
        }
      }
    }
		mMachineLearningSemaphore.release();
	}

	@Override
	public void onObjectFatalError() {
		Log.d("ScanBaseActivity", "onObjectFatalError for object detection");
	}

	@Override
	public void onPrediction(Bitmap bm, int imageWidth, int imageHeight) {
		if (!mSentResponse && mIsActivityActive) {
			// do something with the prediction
		}
		mMachineLearningSemaphore.release();
	}

	/**
	 * A basic Camera preview class
	 */
	public class CameraPreview extends SurfaceView implements Camera.AutoFocusCallback, SurfaceHolder.Callback {
		private final SurfaceHolder mHolder;
		private final Camera.PreviewCallback mPreviewCallback;

		public CameraPreview(Context context, Camera.PreviewCallback previewCallback) {
			super(context);

			mPreviewCallback = previewCallback;

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			Camera.Parameters params = mCamera.getParameters();
			List<String> focusModes = params.getSupportedFocusModes();
			if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			} else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			}
			params.setRecordingHint(true);
			mCamera.setParameters(params);
		}

		@Override
		public void onAutoFocus(boolean success, Camera camera) {

		}

		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, now tell the camera where to draw the preview.
			try {
				if (mCamera == null)
					return;
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (IOException e) {
				Log.d("CameraCaptureActivity", "Error setting camera preview: " + e.getMessage());
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// empty. Take care of releasing the Camera preview in your activity.
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			// If your preview can change or rotate, take care of those events here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				// ignore: tried to stop a non-existent preview
			}

			// set preview size and make any resize, rotate or
			// reformatting changes here

			// start preview with new settings
			try {
				mCamera.setPreviewDisplay(mHolder);
				mCamera.setPreviewCallback(mPreviewCallback);
				mCamera.startPreview();
			} catch (Exception e) {
				Log.d("CameraCaptureActivity", "Error starting camera preview: " + e.getMessage());
			}
		}
	}
}
