package com.Revsoft.Wabbitemu;

import com.Revsoft.Wabbitemu.threads.MainThread;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WabbitLCD extends SurfaceView implements SurfaceHolder.Callback {

	public MainThread mainThread = null;
	private SurfaceHolder surfaceHolder;
	
	public WabbitLCD() {
		super(null, null);
	}

	public WabbitLCD(Context context, AttributeSet attrs) {
		super(context, attrs);

		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		mainThread = new MainThread(surfaceHolder, context);
		setFocusable(true);
	}

	private int oldWidth = 0, oldHeight = 0;
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (oldWidth != width || oldHeight != height) {
			mainThread.loadSkinAndKeymap(width, height);
			oldWidth = width;
			oldHeight = height;
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (mainThread == null) {
			mainThread.start();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		while (retry) {
			try {
				mainThread.join();
				retry = false;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mainThread.doTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		return mainThread.doKeyDown(keyCode, msg);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent msg) {
		return mainThread.doKeyUp(keyCode, msg);
	}
}
