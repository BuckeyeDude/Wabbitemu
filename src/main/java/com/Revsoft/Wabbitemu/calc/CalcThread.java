package com.Revsoft.Wabbitemu.calc;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

public class CalcThread extends Thread {

    private static final long FPS = 50L;
    private static final long TPS = TimeUnit.SECONDS.toMillis(1) / FPS;
    private static final int MAX_FRAME_SKIP = (int) (FPS / 2);

    private final AtomicBoolean mIsPaused = new AtomicBoolean(false);
    private final AtomicBoolean mReset = new AtomicBoolean(false);
    private final List<String> mPauseList;
    private final List<Runnable> mRunnables = Collections.synchronizedList(new ArrayList<Runnable>());

    private CalcScreenUpdateCallback mScreenUpdateCallback;

    public CalcThread() {
        mPauseList = new ArrayList<>();
    }

    @Override
    public void run() {
        long startTime;
        int framesSkipped;

        while (true) {
            if (isInterrupted()) {
                break;
            }

            for (Runnable runnable : mRunnables) {
                runnable.run();
            }
            mRunnables.clear();

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

            startTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            framesSkipped = 0;

            runCalcs();
            if (mScreenUpdateCallback != null) {
                mScreenUpdateCallback.onUpdateScreen();
            }

            long sleepTime = TPS - TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) + startTime;

            if (sleepTime > 0) {
                try {
                    sleep(sleepTime);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIP) {
                runCalcs();
                sleepTime += TPS;
                framesSkipped++;
                Log.d("Wabbitemu", "Frame skip");
            }

            // if (framesSkipped == MAX_FRAME_SKIP) {
            // Log.d("", "Frame skip: " + framesSkipped);
            // }
        }
    }

    private void runCalcs() {
        CalcInterface.RunCalcs();
    }

    public void setPaused(@Nonnull String key, boolean shouldBePaused) {
        if (shouldBePaused) {
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

    public void queueRunnable(Runnable runnable) {
        mRunnables.add(runnable);
    }
}