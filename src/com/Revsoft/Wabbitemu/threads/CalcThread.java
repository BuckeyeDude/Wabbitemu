package com.Revsoft.Wabbitemu.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.SystemClock;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.calc.CalcScreenUpdateCallback;

public class CalcThread extends Thread {

	private static final long FPS = 50L;
	private static final long TPS = TimeUnit.SECONDS.toMillis(1) / FPS;
	private static final int MAX_FRAME_SKIP = (int) (FPS / 2);

	private final AtomicBoolean mIsPaused = new AtomicBoolean(false);
	private final AtomicBoolean mReset = new AtomicBoolean(false);
	private final List<String> mPauseList;

	private CalcScreenUpdateCallback mScreenUpdateCallback;

	public CalcThread() {
		mPauseList = new ArrayList<String>();
	}

	@Override
	public void run() {
		long startTime;
		int framesSkipped = 0;

		while (true) {
			if (isInterrupted()) {
				break;
			}

			if (mIsPaused.get()) {
				try {
					sleep(100);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
				continue;
			}

			if (mReset.getAndSet(false)) {
				CalcInterface.ResetCalc();
			}

			startTime = SystemClock.elapsedRealtime();
			framesSkipped = 0;

			CalcInterface.RunCalcs();
			if (mScreenUpdateCallback != null) {
				mScreenUpdateCallback.onUpdateScreen();
			}

			long sleepTime = TPS - SystemClock.elapsedRealtime() + startTime;

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

			// if (framesSkipped == MAX_FRAME_SKIP) {
			// Log.d("", "Frame skip: " + framesSkipped);
			// }
		}
	}

	public void setPaused(final String key, final boolean paused) {
		if (paused) {
			if (!mPauseList.contains(key)) {
				mPauseList.add(key);
			}

			mIsPaused.set(true);
		} else {
			mPauseList.remove(key);
			if (mPauseList.size() == 0) {
				mIsPaused.set(false);
			}
		}
	}

	public void resetCalc() {
		mReset.set(true);
	}

	public void setScreenUpdateCallback(CalcScreenUpdateCallback callback) {
		mScreenUpdateCallback = callback;
	}
}
