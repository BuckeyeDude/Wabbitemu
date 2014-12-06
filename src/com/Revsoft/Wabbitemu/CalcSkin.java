package com.Revsoft.Wabbitemu;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.Revsoft.Wabbitemu.utils.PreferenceConstants;

public class CalcSkin extends View {

	private final SkinBitmapLoader mSkinLoader = SkinBitmapLoader.getInstance();
	private final SharedPreferences mSharedPrefs;
	private final CalcKeyManager mCalcKeyManager;
	private final Vibrator mVibrator;
	private final Paint mPaint;

	private boolean mHasVibrationEnabled;

	public CalcSkin(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		mCalcKeyManager = CalcKeyManager.getInstance();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		mSharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		mHasVibrationEnabled = mSharedPrefs.getBoolean(PreferenceConstants.USE_VIBRATION.toString(), true);
		mPaint = new Paint();
		mPaint.setAntiAlias(false);
		mPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
	}

	private final OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
			if (key.equals(PreferenceConstants.USE_VIBRATION)) {
				mHasVibrationEnabled = sharedPreferences.getBoolean(key, true);
			}
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
		final Bitmap renderedSkin = mSkinLoader.getRenderedSkin();
		if (renderedSkin != null) {
			canvas.drawBitmap(renderedSkin, 0, 0, mPaint);
			Log.i("Wabbitemu activity", "Wabbit finish");
		} else {
			canvas.drawColor(Color.DKGRAY);
		}
	}

	public void destroySkin() {
		mSkinLoader.destroySkin();
	}

	public Rect getLCDRect() {
		return mSkinLoader.getLcdRect();
	}

	public Rect getLCDSkinRect() {
		return mSkinLoader.getSkinRect();
	}

	private boolean handleTouchEvent(final MotionEvent event, final int index) {
		final int x = (int) (event.getX(index) - mSkinLoader.getSkinX());
		final int y = (int) (event.getY(index) - mSkinLoader.getSkinY());
		final int id = event.getPointerId(index);

		if (mSkinLoader.isOutsideKeymap(x, y)) {
			return true;
		}

		final int actionMasked = event.getActionMasked();
		if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_POINTER_UP
				|| actionMasked == MotionEvent.ACTION_CANCEL)
		{
			mCalcKeyManager.doKeyUp(id);
			return true;
		}

		final int color = mSkinLoader.getKeymapPixel(x, y);
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

	public interface CalcSkinChangedListener {
		public void onCalcSkinChanged(Rect lcdRect, Rect lcdSkinRect);
	}
}
