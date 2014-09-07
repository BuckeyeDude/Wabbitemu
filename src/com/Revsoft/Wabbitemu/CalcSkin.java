package com.Revsoft.Wabbitemu;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.Revsoft.Wabbitemu.utils.PreferenceConstants;

public class CalcSkin extends View {
	private static final int SKIN_WIDTH = 700;
	private static final Point[] RECT_POINTS = {
		new Point(150, 1350),
		new Point(190, 1366),
		new Point(524, 1364),
		new Point(558, 1350),
		new Point(632, 1070),
		new Point(632, 546),
		new Point(74, 546),
		new Point(74, 1136),
		new Point(SKIN_WIDTH - 74, 1136),
		new Point(SKIN_WIDTH - 74, 546),
		new Point(SKIN_WIDTH - 632, 546),
		new Point(SKIN_WIDTH - 632, 1070),
		new Point(SKIN_WIDTH - 558, 1350),
		new Point(SKIN_WIDTH - 524, 1364),
		new Point(SKIN_WIDTH - 190, 1366),
		new Point(SKIN_WIDTH - 150, 1350) };

	private final SharedPreferences mSharedPrefs;
	private final CalcKeyManager mCalcKeyManager;
	private final Vibrator mVibrator;
	private final Paint mPaint;
	private final Set<CalcSkinChangedListener> mSkinListeners = new HashSet<CalcSkinChangedListener>();

	private Bitmap mRenderedSkinImage;
	private double mRatio;
	private Rect mLcdRect;
	private Rect mScreenRect;
	private int mSkinX, mSkinY;
	private int mFaceplateColor;
	private Path mFaceplatePath;
	private int[] mKeymapPixels;
	private int mKeymapWidth;
	private int mKeymapHeight;
	private boolean mCorrectRatio;
	private boolean mLargeScreen;
	private boolean mHasVibrationEnabled;

	public CalcSkin(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		mCalcKeyManager = CalcKeyManager.getInstance();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		mHasVibrationEnabled = mSharedPrefs.getBoolean(PreferenceConstants.USE_VIBRATION, true);
		mCorrectRatio = mSharedPrefs.getBoolean(PreferenceConstants.CORRECT_SCREEN_RATIO, false);
		mPaint = new Paint();
		mPaint.setAntiAlias(false);
		mPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
	}

	private final OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
			if (key.equals(PreferenceConstants.FACEPLATE_COLOR)) {
				// the theory here is that its better memory wise
				// to throw away our scaled skin and to reload it
				// when the faceplate changes
				mFaceplateColor = sharedPreferences.getInt(PreferenceConstants.FACEPLATE_COLOR, Color.GRAY);
				loadSkinThread();
			} else if (key.equals(PreferenceConstants.LARGE_SCREEN)) {
				mLargeScreen = sharedPreferences.getBoolean(key, false);
				loadSkinThread();
			} else if (key.equals(PreferenceConstants.USE_VIBRATION)) {
				mHasVibrationEnabled = sharedPreferences.getBoolean(key, true);
			} else if (key.equals(PreferenceConstants.CORRECT_SCREEN_RATIO)) {
				mCorrectRatio = sharedPreferences.getBoolean(key, false);
				loadSkinThread();
			}
		}

		private void loadSkinThread() {
			final Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					loadSkinAndKeymap();
				}
			});
			thread.start();
		}
	};

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		boolean handled = false;
		for (int i = 0; i < event.getPointerCount(); i++) {
			handled |= handleTouchEvent(event, i);
		}

		return handled;
	}

	@Override
	public void onDraw(final Canvas canvas) {
		if (mRenderedSkinImage != null) {
			canvas.drawBitmap(mRenderedSkinImage, 0, 0, mPaint);
		}
	}

	public void destroySkin() {
		mRenderedSkinImage = null;
		mScreenRect = null;
		mLcdRect = null;
		mKeymapPixels = null;
	}

	public void registerSkinChangedListener(final CalcSkinChangedListener listener) {
		mSkinListeners.add(listener);
	}

	public void unregisterSkinChangedListener(final CalcSkinChangedListener listener) {
		mSkinListeners.remove(listener);
	}

	public Rect getLCDRect() {
		return mLcdRect;
	}

	public Rect getLCDSkinRect() {
		return mScreenRect;
	}

	public void loadSkinAndKeymap() {
		final int skinImageId;
		final int keymapImageId;
		if (mLargeScreen) {
			switch (CalcInterface.GetModel()) {
			case CalcInterface.TI_73:
				skinImageId = R.drawable.ti73;
				keymapImageId = R.drawable.ti83pkeymaplarge;
				break;
			case CalcInterface.TI_81:
				skinImageId = R.drawable.ti81;
				keymapImageId = R.drawable.ti81keymaplarge;
				break;
			case CalcInterface.TI_82:
				skinImageId = R.drawable.ti82;
				keymapImageId = R.drawable.ti82keymaplarge;
				break;
			case CalcInterface.TI_83:
				skinImageId = R.drawable.ti83;
				keymapImageId = R.drawable.ti83keymaplarge;
				break;
			case CalcInterface.TI_83P:
				skinImageId = R.drawable.ti83p;
				keymapImageId = R.drawable.ti83pkeymaplarge;
				break;
			case CalcInterface.TI_83PSE:
				skinImageId = R.drawable.ti83pse;
				keymapImageId = R.drawable.ti83pkeymaplarge;
				break;
			case CalcInterface.TI_84P:
				skinImageId = R.drawable.ti84p;
				keymapImageId = R.drawable.ti84psekeymaplarge;
				break;
			case CalcInterface.TI_84PSE:
				skinImageId = R.drawable.ti84pse;
				keymapImageId = R.drawable.ti84psekeymaplarge;
				break;
			case CalcInterface.TI_84PCSE:
				skinImageId = R.drawable.ti84pcse;
				keymapImageId = R.drawable.ti84pcsekeymaplarge;
				break;
			case CalcInterface.TI_85:
				skinImageId = R.drawable.ti85;
				keymapImageId = R.drawable.ti85keymaplarge;
				break;
			case CalcInterface.TI_86:
				skinImageId = R.drawable.ti86;
				keymapImageId = R.drawable.ti86keymaplarge;
				break;
			default:
				return;
			}
		} else {
			switch (CalcInterface.GetModel()) {
			case CalcInterface.TI_73:
				skinImageId = R.drawable.ti73;
				keymapImageId = R.drawable.ti83pkeymap;
				break;
			case CalcInterface.TI_81:
				skinImageId = R.drawable.ti81;
				keymapImageId = R.drawable.ti81keymap;
				break;
			case CalcInterface.TI_82:
				skinImageId = R.drawable.ti82;
				keymapImageId = R.drawable.ti82keymap;
				break;
			case CalcInterface.TI_83:
				skinImageId = R.drawable.ti83;
				keymapImageId = R.drawable.ti83keymap;
				break;
			case CalcInterface.TI_83P:
				skinImageId = R.drawable.ti83p;
				keymapImageId = R.drawable.ti83pkeymap;
				break;
			case CalcInterface.TI_83PSE:
				skinImageId = R.drawable.ti83pse;
				keymapImageId = R.drawable.ti83pkeymap;
				break;
			case CalcInterface.TI_84P:
				skinImageId = R.drawable.ti84p;
				keymapImageId = R.drawable.ti84psekeymap;
				break;
			case CalcInterface.TI_84PSE:
				skinImageId = R.drawable.ti84pse;
				keymapImageId = R.drawable.ti84psekeymap;
				break;
			case CalcInterface.TI_84PCSE:
				skinImageId = R.drawable.ti84pcse;
				keymapImageId = R.drawable.ti84pcsekeymap;
				break;
			case CalcInterface.TI_85:
				skinImageId = R.drawable.ti85;
				keymapImageId = R.drawable.ti85keymap;
				break;
			case CalcInterface.TI_86:
				skinImageId = R.drawable.ti86;
				keymapImageId = R.drawable.ti86keymap;
				break;
			default:
				return;
			}
		}

		setSurfaceSize(skinImageId, keymapImageId);
		for (final CalcSkinChangedListener listener : mSkinListeners) {
			listener.onCalcSkinChanged(mLcdRect, mScreenRect);
		}
	}

	private synchronized void setSurfaceSize(final int skinImageId, final int keymapImageId) {
		final WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		final Display display = wm.getDefaultDisplay();
		final Point displaySize = new Point();
		display.getSize(displaySize);
		// TODO: use getWidth() / getHeight()
		final int width = displaySize.x;
		final int height = displaySize.y;
		final Resources resources = getResources();
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeResource(resources, skinImageId, options);

		options.inJustDecodeBounds = false;

		mSkinX = 0;
		mSkinY = 0;
		final int skinResourceWidth = options.outWidth;
		final int skinResourceHeight = options.outHeight;
		int skinWidth = width;
		int skinHeight = height;
		mRatio = 1.0;

		if (resources.getConfiguration().smallestScreenWidthDp >= 600) {
			mRatio = Math.min((double) width / skinResourceWidth, (double) height / skinResourceHeight);
			skinWidth = (int) (skinResourceWidth * mRatio);
			skinHeight = (int) (skinResourceHeight * mRatio);
			mSkinX = (width - skinWidth) / 2;
			mSkinY = (height - skinHeight) / 2;
		}

		Bitmap keymapImage = BitmapFactory.decodeResource(resources, keymapImageId);
		keymapImage = Bitmap.createScaledBitmap(keymapImage, skinWidth, skinHeight, true);

		mKeymapWidth = keymapImage.getWidth();
		mKeymapHeight = keymapImage.getHeight();
		mKeymapPixels = new int[mKeymapWidth * mKeymapHeight];
		keymapImage.getPixels(mKeymapPixels, 0, mKeymapWidth, 0, 0, mKeymapWidth, mKeymapHeight);
		int foundX = -1;
		int foundY = -1;
		int foundWidth = 0;
		int foundHeight = 0;
		int pixel = 0;
		for (; pixel < mKeymapWidth * mKeymapHeight; pixel++) {
			if (mKeymapPixels[pixel] == 0xFFFF0000) {
				foundX = pixel % mKeymapWidth;
				foundY = pixel / mKeymapWidth;
				break;
			}
		}
		if ((foundX == -1) || (foundY == -1)) {
			android.util.Log.d("Keymap", "Keymap fail");
			mLcdRect = new Rect(0, 0, 1, 1);
			return;
		}

		do {
			foundWidth++;
			pixel++;
		} while (mKeymapPixels[pixel] == 0xFFFF0000 && foundWidth < mKeymapWidth);

		pixel--;
		do {
			foundHeight++;
			pixel += mKeymapWidth;
		} while (mKeymapPixels[pixel] == 0xFFFF0000);

		final int lcdWidth, lcdHeight;
		switch (CalcInterface.GetModel()) {
		case CalcInterface.TI_85:
		case CalcInterface.TI_86:
			lcdWidth = 128;
			lcdHeight = 64;
			break;
		case CalcInterface.TI_84PCSE:
			lcdWidth = 320;
			lcdHeight = 240;
			break;
		default:
			lcdWidth = 96;
			lcdHeight = 64;
			break;
		}
		mLcdRect = new Rect(0, 0, lcdWidth, lcdHeight);
		mScreenRect = new Rect(foundX + mSkinX, foundY + mSkinY, foundWidth + foundX + mSkinX, foundY + foundHeight
				+ mSkinY);
		if (mCorrectRatio) {
			final int screenWidth, screenHeight;
			final double screenRatio = (double) mLcdRect.width() / mLcdRect.height();
			final double realRatio = (double) foundWidth / foundHeight;
			if (realRatio > screenRatio) {
				// assuming all calc screens width > height
				screenHeight = mScreenRect.height();
				screenWidth = (int) (screenHeight * screenRatio);
				mScreenRect.right = mScreenRect.left + screenWidth;
				final int shift = (foundWidth - screenWidth) / 2;
				mScreenRect.left += shift;
				mScreenRect.right += shift;
			} else {
				screenWidth = mScreenRect.width();
				screenHeight = (int) (screenWidth * screenRatio);
				mScreenRect.bottom = mScreenRect.top + screenHeight;
				final int shift = (foundHeight - screenHeight) / 2;
				mScreenRect.top += shift;
				mScreenRect.bottom += shift;
			}
		}

		final Bitmap skinImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, skinImageId),
				skinWidth, skinHeight, true);

		mFaceplateColor = mSharedPrefs.getInt(PreferenceConstants.FACEPLATE_COLOR, Color.GRAY);
		mSharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);
		createRenderSkin(width, height, skinImage);
	}

	private void createRenderSkin(final int width, final int height, final Bitmap skinImage) {
		mRenderedSkinImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		final Canvas skinCanvas = new Canvas(mRenderedSkinImage);
		skinCanvas.setDensity(Bitmap.DENSITY_NONE);
		mFaceplatePath = getSkinPath();
		drawFaceplate(skinCanvas);
		skinCanvas.drawBitmap(skinImage, mSkinX, mSkinY, null);
	}

	private Path getSkinPath() {
		final Path path = new Path();
		path.moveTo(RECT_POINTS[0].x, RECT_POINTS[0].y);
		for (int i = 1; i < RECT_POINTS.length; i++) {
			path.lineTo(RECT_POINTS[i].x, RECT_POINTS[i].y);
		}

		final Matrix scaleMatrix = new Matrix();
		final RectF rectF = new RectF();
		path.computeBounds(rectF, true);
		scaleMatrix.setScale((float) mRatio, (float) mRatio, mSkinX, mSkinY);
		path.offset(mSkinX, mSkinY);
		path.transform(scaleMatrix);

		return path;
	}

	private void drawFaceplate(final Canvas canvas) {
		if (CalcInterface.GetModel() != CalcInterface.TI_84PSE && CalcInterface.GetModel() != CalcInterface.TI_84PCSE) {
			return;
		}

		mFaceplateColor = mSharedPrefs.getInt(PreferenceConstants.FACEPLATE_COLOR, Color.GRAY);

		if (mSkinX > 0) {
			final Paint faceplatePaint = new Paint();
			faceplatePaint.setStyle(Paint.Style.FILL);
			faceplatePaint.setColor(mFaceplateColor);
			canvas.drawPath(mFaceplatePath, faceplatePaint);
		} else {
			canvas.drawColor(mFaceplateColor);
		}
	}

	private boolean handleTouchEvent(final MotionEvent event, final int index) {
		final int x = (int) (event.getX(index) - mSkinX);
		final int y = (int) (event.getY(index) - mSkinY);
		final int id = event.getPointerId(index);

		if ((x >= mKeymapWidth) || (y >= mKeymapHeight) || (x < 0) || (y < 0) || mKeymapPixels == null) {
			return true;
		}

		final int actionMasked = event.getActionMasked();
		if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_POINTER_UP
				|| actionMasked == MotionEvent.ACTION_CANCEL)
		{
			mCalcKeyManager.doKeyUp(id);
			return true;
		}

		final int color = getKeymapPixel(x, y);
		if (Color.red(color) == 0xFF) {
			return true;
		}

		final int group = Color.green(color) >> 4;
		final int bit = Color.blue(color) >> 4;
		if ((group > 7) || (bit > 7)) {
			return true;
		}

		if (actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
			if (mHasVibrationEnabled) {
				mVibrator.vibrate(50);
			}

			mCalcKeyManager.doKeyDown(id, group, bit);
		}
		return true;
	}

	private int getKeymapPixel(final int x, final int y) {
		return mKeymapPixels[y * mKeymapWidth + x];
	}

	public interface CalcSkinChangedListener {
		public void onCalcSkinChanged(Rect lcdRect, Rect lcdSkinRect);
	}
}
