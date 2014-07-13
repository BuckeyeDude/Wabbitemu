package com.Revsoft.Wabbitemu;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.Revsoft.Wabbitemu.threads.MainThread;

public class WabbitLCD extends SurfaceView implements SurfaceHolder.Callback {

	public MainThread mMainThread = null;
	private SurfaceHolder surfaceHolder;

	public WabbitLCD() {
		super(null, null);
	}

	public WabbitLCD(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		mMainThread = new MainThread(surfaceHolder, context);
		setFocusable(true);
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
		if (mMainThread == null) {
			return;
		}

		mMainThread.loadSkinAndKeymap(width, height);
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		if (mMainThread == null) {
			mMainThread.start();
		}
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
	public boolean onTouchEvent(final MotionEvent event) {
		if (mMainThread == null) {
			return false;
		}

		return mMainThread.doTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent msg) {
		if (mMainThread == null) {
			return false;
		}

		return mMainThread.doKeyDown(keyCode, msg);
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent msg) {
		if (mMainThread == null) {
			return false;
		}

		return mMainThread.doKeyUp(keyCode, msg);
	}

	public void drawScreen() {
		if (mMainThread == null) {
			return;
		}

		mMainThread.run();
	}

	public void updateSkin() {
		if (mMainThread == null) {
			return;
		}

		mMainThread.loadSkinAndKeymap();
	}

	public Bitmap getScreen() {
		if (mMainThread == null) {
			return null;
		}

		return mMainThread.getScreen();
	}
}
