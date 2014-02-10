package com.Revsoft.Wabbitemu.threads;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.KeyMapping;
import com.Revsoft.Wabbitemu.utils.PreferenceConstants;

public class MainThread extends Thread {

	private static final int MIN_TSTATE_KEY = 600;
	private static final int MIN_TSTATE_ON_KEY = 2500;

	private static final int SKIN_WIDTH = 700;

	private double mRatio;
	private Rect mLcdRect;
	private Rect mScreenRect;
	private int mWidth, mHeight;
	private int mSkinX, mSkinY;

	private final Paint mPaint;
	private final SurfaceHolder mSurfaceHolder;
	private final Context mContext;
	private int mFaceplateColor;

	private int[] mKeymapPixels;
	private int mKeymapWidth;
	private int mKeymapHeight;

	private Bitmap mRenderedSkinImage;
	private Path mFaceplatePath;
	private OnSharedPreferenceChangeListener mPrefListener;

	private final ArrayList<KeyMapping> mKeysDown = new ArrayList<KeyMapping>();
	private final int[][] mKeyTimePressed;

	public final Point[] mRectPoints = { new Point(150, 1350),
			new Point(190, 1366), new Point(524, 1364), new Point(558, 1350),
			new Point(632, 1070), new Point(632, 546), new Point(74, 546),
			new Point(74, 1136), new Point(SKIN_WIDTH - 74, 1136),
			new Point(SKIN_WIDTH - 74, 546), new Point(SKIN_WIDTH - 632, 546),
			new Point(SKIN_WIDTH - 632, 1070),
			new Point(SKIN_WIDTH - 558, 1350),
			new Point(SKIN_WIDTH - 524, 1364),
			new Point(SKIN_WIDTH - 190, 1366),
			new Point(SKIN_WIDTH - 150, 1350) };

	private final Vibrator mVibrator;
	private final boolean mHasVibrationEnabled;

	public KeyMapping[] keyMappings = {
			new KeyMapping(KeyEvent.KEYCODE_DPAD_DOWN, 0, 0),
			new KeyMapping(KeyEvent.KEYCODE_DPAD_LEFT, 0, 1),
			new KeyMapping(KeyEvent.KEYCODE_DPAD_RIGHT, 0, 2),
			new KeyMapping(KeyEvent.KEYCODE_DPAD_UP, 0, 3),
			new KeyMapping(KeyEvent.KEYCODE_ENTER, 1, 0),
			new KeyMapping(KeyEvent.KEYCODE_AT, 5, 0),
			new KeyMapping(KeyEvent.KEYCODE_A, 5, 6),
			new KeyMapping(KeyEvent.KEYCODE_B, 4, 6),
			new KeyMapping(KeyEvent.KEYCODE_C, 3, 6),
			new KeyMapping(KeyEvent.KEYCODE_D, 5, 5),
			new KeyMapping(KeyEvent.KEYCODE_E, 4, 5),
			new KeyMapping(KeyEvent.KEYCODE_F, 3, 5),
			new KeyMapping(KeyEvent.KEYCODE_G, 2, 5),
			new KeyMapping(KeyEvent.KEYCODE_H, 1, 5),
			new KeyMapping(KeyEvent.KEYCODE_I, 5, 4),
			new KeyMapping(KeyEvent.KEYCODE_J, 4, 4),
			new KeyMapping(KeyEvent.KEYCODE_K, 3, 4),
			new KeyMapping(KeyEvent.KEYCODE_L, 2, 4),
			new KeyMapping(KeyEvent.KEYCODE_M, 1, 4),
			new KeyMapping(KeyEvent.KEYCODE_N, 5, 3),
			new KeyMapping(KeyEvent.KEYCODE_O, 4, 3),
			new KeyMapping(KeyEvent.KEYCODE_P, 3, 3),
			new KeyMapping(KeyEvent.KEYCODE_Q, 2, 3),
			new KeyMapping(KeyEvent.KEYCODE_R, 1, 3),
			new KeyMapping(KeyEvent.KEYCODE_S, 5, 2),
			new KeyMapping(KeyEvent.KEYCODE_T, 4, 2),
			new KeyMapping(KeyEvent.KEYCODE_U, 3, 2),
			new KeyMapping(KeyEvent.KEYCODE_V, 2, 2),
			new KeyMapping(KeyEvent.KEYCODE_W, 1, 2),
			new KeyMapping(KeyEvent.KEYCODE_X, 5, 1),
			new KeyMapping(KeyEvent.KEYCODE_Y, 4, 1),
			new KeyMapping(KeyEvent.KEYCODE_Z, 3, 1),
			new KeyMapping(KeyEvent.KEYCODE_SPACE, 4, 0),
			new KeyMapping(KeyEvent.KEYCODE_0, 4, 0),
			new KeyMapping(KeyEvent.KEYCODE_1, 4, 1),
			new KeyMapping(KeyEvent.KEYCODE_2, 3, 1),
			new KeyMapping(KeyEvent.KEYCODE_3, 2, 1),
			new KeyMapping(KeyEvent.KEYCODE_4, 4, 2),
			new KeyMapping(KeyEvent.KEYCODE_5, 3, 2),
			new KeyMapping(KeyEvent.KEYCODE_6, 2, 2),
			new KeyMapping(KeyEvent.KEYCODE_7, 4, 3),
			new KeyMapping(KeyEvent.KEYCODE_8, 3, 3),
			new KeyMapping(KeyEvent.KEYCODE_9, 2, 3),
			new KeyMapping(KeyEvent.KEYCODE_PERIOD, 3, 0),
			new KeyMapping(KeyEvent.KEYCODE_COMMA, 4, 4),
			new KeyMapping(KeyEvent.KEYCODE_PLUS, 1, 1),
			new KeyMapping(KeyEvent.KEYCODE_MINUS, 1, 2),
			new KeyMapping(KeyEvent.KEYCODE_STAR, 2, 3),
			new KeyMapping(KeyEvent.KEYCODE_SLASH, 1, 4),
			new KeyMapping(KeyEvent.KEYCODE_LEFT_BRACKET, 3, 4),
			new KeyMapping(KeyEvent.KEYCODE_RIGHT_BRACKET, 2, 4),
			new KeyMapping(KeyEvent.KEYCODE_SHIFT_LEFT, 6, 5),
			new KeyMapping(KeyEvent.KEYCODE_SHIFT_RIGHT, 1, 6),
			new KeyMapping(KeyEvent.KEYCODE_ALT_LEFT, 5, 7),
			new KeyMapping(KeyEvent.KEYCODE_EQUALS, 4, 7),
			/*
			 * { VK_F1 , 6 , 4 }, { VK_F2 , 6 , 3 }, { VK_F3 , 6 , 2 }, { VK_F4 , 6 , 1
			 * }, { VK_F5 , 6 , 0 },
			 */
	};

	public MainThread(final SurfaceHolder surfaceHolder, final Context context) {
		super();
		mSurfaceHolder = surfaceHolder;
		mContext = context;

		mPaint = new Paint();
		mPaint.setAntiAlias(false);
		mPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);

		mKeyTimePressed = new int[8][8];

		mVibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		final SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		mHasVibrationEnabled = sharedPrefs.getBoolean(PreferenceConstants.USE_VIBRATION, true);

		loadSkinAndKeymap();

		Canvas canvas = null;
		try {
			canvas = mSurfaceHolder.lockCanvas();
			if (canvas != null) {
				canvas.drawBitmap(mRenderedSkinImage, 0, 0, null);
			}
		} finally {
			if (canvas != null) {
				mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}

	public boolean doKeyDown(final int keyCode, final KeyEvent msg) {
		int group = -1, bit = 0;
		for (final KeyMapping mapping : keyMappings) {
			if (mapping.getKey() == keyCode) {
				group = mapping.getGroup();
				bit = mapping.getBit();
				break;
			}
		}

		if (group != -1) {
			mKeyTimePressed[group][bit] = CalcInterface.Tstates();
			CalcInterface.PressKey(group, bit);
		}
		return true;
	}

	public boolean doKeyUp(final int keyCode, final KeyEvent msg) {
		int group = -1, bit = 0;
		for (final KeyMapping mapping : keyMappings) {
			if (mapping.getKey() == keyCode) {
				group = mapping.getGroup();
				bit = mapping.getBit();
				break;
			}
		}

		if (group != -1) {
			if (hasCalcProcessedKey(group, bit)) {
				final int newKeyCode = keyCode;
				final KeyEvent newMsg = msg;
				final Timer repressTimer = new Timer();
				final TimerTask task = new TimerTask() {
					@Override
					public void run() {
						doKeyUp(newKeyCode, newMsg);
					}
				};
				repressTimer.schedule(task, 40);
			} else {
				CalcInterface.ReleaseKey(group, bit);
			}
		}
		return true;
	}

	private boolean hasCalcProcessedKey(final int group, final int bit) {
		if (group == CalcInterface.ON_KEY_GROUP && bit == CalcInterface.ON_KEY_BIT) {
			return (mKeyTimePressed[group][bit] + MIN_TSTATE_ON_KEY) > CalcInterface
					.Tstates();
		} else {
			return (mKeyTimePressed[group][bit] + MIN_TSTATE_KEY) > CalcInterface
					.Tstates();
		}
	}

	public boolean doTouchEvent(final MotionEvent event) {
		boolean handled = false;
		for (int i = 0; i < event.getPointerCount(); i++) {
			handled |= handleTouchEvent(event, i);
		}

		return handled;
	}

	private boolean handleTouchEvent(final MotionEvent event, final int index) {
		final int x = (int) (event.getX(index) - mSkinX);
		final int y = (int) (event.getY(index) - mSkinY);
		final int id = event.getPointerId(index);

		if ((x >= mKeymapWidth) || (y >= mKeymapHeight) || (x < 0) || (y < 0)) {
			return true;
		}

		if (event.getActionMasked() == MotionEvent.ACTION_UP
				|| event.getActionMasked() == MotionEvent.ACTION_POINTER_UP
				|| event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
			KeyMapping mapping = null;
			for (int i = 0; i < mKeysDown.size(); i++) {
				if (mKeysDown.get(i).getKey() == id) {
					mapping = mKeysDown.get(i);
				}
			}

			if (mapping == null) {
				return true;
			}

			final int group = mapping.getGroup();
			final int bit = mapping.getBit();
			if (hasCalcProcessedKey(group, bit)) {
				final MotionEvent newEvent = event;
				final Timer repressTimer = new Timer();
				final TimerTask task = new TimerTask() {
					@Override
					public void run() {
						doTouchEvent(newEvent);
					}
				};
				repressTimer.schedule(task, 40);
			} else {
				CalcInterface.ReleaseKey(group, bit);
				mKeysDown.remove(mapping);
			}

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

		if (event.getActionMasked() == MotionEvent.ACTION_DOWN
				|| event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
			if (mHasVibrationEnabled) {
				mVibrator.vibrate(50);
			}
			CalcInterface.PressKey(group, bit);
			mKeyTimePressed[group][bit] = CalcInterface.Tstates();
			mKeysDown.add(new KeyMapping(id, group, bit));
		}
		return true;
	}

	public Bitmap getScreen() {
		final int[] colors = new int[mLcdRect.width() * mLcdRect.height()];
		final Bitmap bitmap;
		if (!CalcInterface.IsLCDActive()) {
			final int lcdColor = CalcInterface.GetModel() == CalcInterface.TI_84PCSE ?
					Color.BLACK : Color.argb(0xFF, 0x9E, 0xAB, 0x88);
			bitmap = Bitmap.createBitmap(mLcdRect.width(), mLcdRect.height(), Bitmap.Config.ARGB_8888);
			bitmap.eraseColor(lcdColor);
		} else {
			CalcInterface.GetLCD(colors);
			bitmap = Bitmap.createBitmap(colors, mLcdRect.width(),
					mLcdRect.height(), Bitmap.Config.ARGB_8888);
		}

		return bitmap;
	}

	public void drawScreen(final Canvas canvas) {
		final Bitmap screenBitmap = getScreen();
		canvas.drawBitmap(screenBitmap, mLcdRect, mScreenRect, mPaint);
	}

	private Path getSkinPath() {
		final Path path = new Path();
		path.moveTo(mRectPoints[0].x, mRectPoints[0].y);
		for (int i = 1; i < mRectPoints.length; i++) {
			path.lineTo(mRectPoints[i].x, mRectPoints[i].y);
		}

		final Matrix scaleMatrix = new Matrix();
		final RectF rectF = new RectF();
		path.computeBounds(rectF, true);
		scaleMatrix.setScale((float) mRatio, (float) mRatio, mSkinX, mSkinY);
		path.offset(mSkinX, mSkinY);
		path.transform(scaleMatrix);

		return path;
	}

	public void loadSkinAndKeymap() {
		loadSkinAndKeymap(mWidth, mHeight);
	}

	public void loadSkinAndKeymap(final int width, final int height) {
		final int keymapImageId;
		final int skinImageId;

		mWidth = width;
		mHeight = height;

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

		if (mWidth > 0 && mHeight > 0) {
			setSurfaceSize(mWidth, mHeight, skinImageId, keymapImageId);
		}
	}

	@Override
	public void run() {
		Canvas canvas = null;
		try {
			canvas = mSurfaceHolder.lockCanvas(null);
			if (canvas != null) {
				if (mRenderedSkinImage != null) {
					canvas.drawColor(Color.DKGRAY);
					canvas.drawBitmap(mRenderedSkinImage, 0, mSkinY, null);
					drawScreen(canvas);
				}
			}
		} finally {
			if (canvas != null) {
				mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}

	private void setSurfaceSize(final int width, final int height,
			final int skinImageId, final int keymapImageId) {
		final Resources resources = mContext.getResources();
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeResource(resources, skinImageId, options);

		options.inJustDecodeBounds = false;

		mRenderedSkinImage = null;

		mSkinX = 0;
		mSkinY = 0;
		final int skinResourceWidth = options.outWidth;
		final int skinResourceHeight = options.outHeight;
		int skinWidth = width;
		int skinHeight = height;
		mRatio = 1.0;

		if ((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
			mRatio = Math.min((double) width / skinResourceWidth,
					(double) height / skinResourceHeight);
			skinWidth = (int) (skinResourceWidth * mRatio);
			skinHeight = (int) (skinResourceHeight * mRatio);
			mSkinX = (width - skinWidth) / 2;
			mSkinY = (height - skinHeight) / 2;
		}

		final Bitmap keymapImage = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(resources, keymapImageId),
				skinWidth, skinHeight, true);

		mKeymapWidth = keymapImage.getWidth();
		mKeymapHeight = keymapImage.getHeight();
		mKeymapPixels = new int[mKeymapWidth * mKeymapHeight];
		keymapImage.getPixels(mKeymapPixels, 0, mKeymapWidth, 0, 0,
				mKeymapWidth, mKeymapHeight);
		int foundX = -1, foundY = -1, foundWidth = 0, foundHeight = 0;
		outerloop: for (int y = 0; y < mKeymapHeight; y++) {
			for (int x = 0; x < mKeymapWidth; x++) {
				if (getKeymapPixel(x, y) == 0xFFFF0000) {
					foundX = x;
					foundY = y;
					break outerloop;
				}
			}
		}
		if ((foundX == -1) || (foundY == -1)) {
			mLcdRect = new Rect(0, 0, 1, 1);
			return;
		}

		do {
			foundWidth++;
		} while (getKeymapPixel(foundX + foundWidth, foundY) == 0xFFFF0000);

		do {
			foundHeight++;
		} while (getKeymapPixel(foundX, foundY + foundHeight) == 0xFFFF0000);

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
		mScreenRect = new Rect(foundX + mSkinX, foundY + mSkinY, foundWidth
				+ foundX + mSkinX, foundY + foundHeight + mSkinY);

		final Bitmap skinImage = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(resources, skinImageId),
				skinWidth, skinHeight, true);

		mPrefListener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					final SharedPreferences sharedPreferences, final String key) {
				if (key.equals(PreferenceConstants.FACEPLATE_COLOR)) {
					// the theory here is that its better memory wise
					// to throw away our scaled skin and to reload it
					// when the faceplate changes
					loadSkinAndKeymap();
				} else if (key.equals(PreferenceConstants.USE_VIBRATION)) {

				}
			}
		};
		final SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		mFaceplateColor = sharedPrefs.getInt(
				PreferenceConstants.FACEPLATE_COLOR, Color.GRAY);
		sharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);
		createRenderSkin(width, height, skinImage);
	}

	private int getKeymapPixel(final int x, final int y) {
		return mKeymapPixels[y * mKeymapWidth + x];
	}

	private void createRenderSkin(final int width, final int height,
			final Bitmap skinImage) {
		mRenderedSkinImage = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		final Canvas skinCanvas = new Canvas(mRenderedSkinImage);
		skinCanvas.setDensity(Bitmap.DENSITY_NONE);
		mFaceplatePath = getSkinPath();
		drawFaceplate(skinCanvas);
		skinCanvas.drawBitmap(skinImage, mSkinX, mSkinY, null);
	}

	private void drawFaceplate(final Canvas canvas) {
		if (CalcInterface.GetModel() == CalcInterface.TI_84PSE ||
				CalcInterface.GetModel() == CalcInterface.TI_84PCSE) {
			final SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			mFaceplateColor = sharedPrefs.getInt(
					PreferenceConstants.FACEPLATE_COLOR, Color.GRAY);

			if (mSkinX > 0) {
				final Paint faceplatePaint = new Paint();
				faceplatePaint.setStyle(Paint.Style.FILL);
				faceplatePaint.setColor(mFaceplateColor);
				canvas.drawPath(mFaceplatePath, faceplatePaint);
			} else {
				canvas.drawColor(mFaceplateColor);
			}
		}
	}
}