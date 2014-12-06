package com.Revsoft.Wabbitemu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.Revsoft.Wabbitemu.calc.CalcScreenUpdateCallback;
import com.Revsoft.Wabbitemu.threads.MainThread;
import com.Revsoft.Wabbitemu.utils.KeyMapping;

public class WabbitLCD extends SurfaceView implements SurfaceHolder.Callback, CalcScreenUpdateCallback {

	private final CalcKeyManager mCalcKeyManager;
	private final MainThread mMainThread;
	private final SurfaceHolder mSurfaceHolder;

	public WabbitLCD(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		mCalcKeyManager = CalcKeyManager.getInstance();
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mMainThread = new MainThread(mSurfaceHolder);
		setFocusable(true);
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
		// no-op
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		// no-op
	}

	@Override
	public void surfaceDestroyed(final SurfaceHolder holder) {
		boolean retry = true;
		while (retry) {
			try {
				mMainThread.join();
				retry = false;
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent msg) {
		final KeyMapping mapping = CalcKeyManager.getKeyMapping(keyCode);
		if (mapping == null) {
			return false;
		}

		mCalcKeyManager.doKeyDown(keyCode, mapping.getGroup(), mapping.getBit());
		return true;
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent msg) {
		final KeyMapping mapping = CalcKeyManager.getKeyMapping(keyCode);
		if (mapping == null) {
			return false;
		}

		mCalcKeyManager.doKeyUp(keyCode);
		return true;
	}

	@Override
	public void onUpdateScreen() {
		mMainThread.run();
	}

	public void updateSkin(final Rect lcdRect, final Rect lcdSkinRect) {
		if (mMainThread == null || lcdRect == null || lcdSkinRect == null) {
			return;
		}

		mMainThread.destroyScreen();
		final FrameLayout.LayoutParams layoutParams = (LayoutParams) getLayoutParams();
		layoutParams.width = lcdSkinRect.width();
		layoutParams.height = lcdSkinRect.height();
		layoutParams.setMargins(lcdSkinRect.left, lcdSkinRect.top, 0, 0);
		setLayoutParams(layoutParams);
		getHolder().setFixedSize(lcdSkinRect.width(), lcdSkinRect.height());
		mMainThread.createScreen(lcdRect, lcdSkinRect);
	}

	public Bitmap getScreen() {
		if (mMainThread == null) {
			return null;
		}

		return mMainThread.getScreen();
	}
}
