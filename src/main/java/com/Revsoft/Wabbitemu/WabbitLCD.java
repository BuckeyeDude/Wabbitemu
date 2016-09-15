package com.Revsoft.Wabbitemu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.Revsoft.Wabbitemu.calc.CalcScreenUpdateCallback;
import com.Revsoft.Wabbitemu.calc.MainThread;
import com.Revsoft.Wabbitemu.utils.KeyMapping;

import java.lang.reflect.Field;

public class WabbitLCD extends SurfaceView implements CalcScreenUpdateCallback {

	private final CalcKeyManager mCalcKeyManager;
	private final MainThread mMainThread;

	public WabbitLCD(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		mCalcKeyManager = CalcKeyManager.getInstance();
		final SurfaceHolder holder = getHolder();
		mMainThread = new MainThread();
		holder.addCallback(mMainThread);
		setFocusable(true);

		try {
			final Field field = SurfaceView.class.getDeclaredField("DEBUG");
			field.setAccessible(true);
			field.set(this, false);
		} catch (Exception e) {
			Log.e("Wabbitemu", e.toString());
		}
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent msg) {
		return mCalcKeyManager.doKeyDownKeyCode(keyCode);
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent msg) {
		return mCalcKeyManager.doKeyUpKeyCode(keyCode);
	}

	@Override
	public void onUpdateScreen() {
		mMainThread.run();
	}

	public void updateSkin(final Rect lcdRect, final Rect lcdSkinRect) {
		if (mMainThread == null || lcdRect == null || lcdSkinRect == null) {
			return;
		}

		final FrameLayout.LayoutParams layoutParams = (LayoutParams) getLayoutParams();
		layoutParams.width = lcdSkinRect.width();
		layoutParams.height = lcdSkinRect.height();
		layoutParams.setMargins(lcdSkinRect.left, lcdSkinRect.top, 0, 0);
		setLayoutParams(layoutParams);
		getHolder().setFixedSize(lcdSkinRect.width(), lcdSkinRect.height());
		mMainThread.recreateScreen(lcdRect, lcdSkinRect);
	}

	@Nullable
	public Bitmap getScreen() {
		if (mMainThread == null) {
			return null;
		}

		return mMainThread.getScreen();
	}
}
