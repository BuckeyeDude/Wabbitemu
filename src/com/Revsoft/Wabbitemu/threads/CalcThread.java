package com.Revsoft.Wabbitemu.threads;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.SurfaceHolder;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.WabbitLCD;

public class CalcThread extends Thread {

	private static final int FPS = 50;
	private static final int TPS = 1000 / FPS;
	private static final int MAX_FRAME_SKIP = 5;

	private boolean mPaused;
	private final SurfaceHolder mSurfaceHolder;
	private final WabbitLCD mSurfaceView;
	private final List<String> mPauseList;

	public CalcThread(final SurfaceHolder surfaceHolder, final WabbitLCD surfaceView) {
		mSurfaceHolder = surfaceHolder;
		mSurfaceView = surfaceView;
		mPauseList = new ArrayList<String>();
	}

	@Override
	public void run() {
		long startTime;
		long timeDiff;
		int sleepTime = 0;
		int framesSkipped;

		while (true) {
			if (mPaused) {
				try {
					sleep(100);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
				continue;
			}

			synchronized (mSurfaceHolder) {
				startTime = SystemClock.elapsedRealtime();
				framesSkipped = 0;

				CalcInterface.RunCalcs();
				mSurfaceView.mainThread.run();

				timeDiff = SystemClock.elapsedRealtime() - startTime;
				sleepTime = (int) (TPS - timeDiff);

				if (sleepTime > 0) {
					try {
						sleep(sleepTime);
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}

				while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIP) {
					CalcInterface.RunCalcs();
					sleepTime += TPS;
					framesSkipped++;
				}

				//android.util.Log.d("", "Frame skip: " + framesSkipped);
			}
		}
	}

	public Bitmap getScreenshot() {
		return mSurfaceView.mainThread.getScreen();
	}

	public void setPaused(final String key, final boolean paused) {
		if (paused) {
			if (!mPauseList.contains(key)) {
				mPauseList.add(key);
			}

			mPaused = true;
		} else {
			mPauseList.remove(key);
			if (mPauseList.size() == 0) {
				mPaused = false;
			}
		}
	}
}
