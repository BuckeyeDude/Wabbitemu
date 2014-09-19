package com.Revsoft.Wabbitemu.threads;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import com.Revsoft.Wabbitemu.CalcInterface;

public class MainThread extends Thread {

	private final Paint mPaint;
	private final SurfaceHolder mSurfaceHolder;
	private final Object mScreenLock = new Object();
	private IntBuffer mScreenBuffer;
	private Bitmap mScreenBitmap;
	private volatile boolean mHasCreatedLcd;

	private Rect mLcdRect;
	private Rect mScreenRect;

	public MainThread(final SurfaceHolder surfaceHolder) {
		super();
		mSurfaceHolder = surfaceHolder;

		mPaint = new Paint();
		mPaint.setAntiAlias(false);
		mPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
	}

	public void createScreen(final Rect lcdRect, final Rect screenRect) {
		synchronized (mScreenLock) {
			mLcdRect = lcdRect;
			mScreenRect = new Rect(screenRect);
			mScreenRect.offset(-mScreenRect.left, -mScreenRect.top);

			mScreenBitmap = Bitmap.createBitmap(mLcdRect.width(),
					mLcdRect.height(), Bitmap.Config.ARGB_8888);
			mScreenBuffer = ByteBuffer.allocateDirect(mLcdRect.width() * mLcdRect.height() * 4)
					.asIntBuffer();
			mHasCreatedLcd = true;
		}
	}

	public void destroyScreen() {
		synchronized (mScreenLock) {
			mHasCreatedLcd = false;
			mScreenBitmap = null;
			mScreenBuffer = null;
		}
	}

	public Bitmap getScreen() {
		synchronized (mScreenLock) {
			if (!CalcInterface.IsLCDActive()) {
				final int lcdColor = CalcInterface.GetModel() == CalcInterface.TI_84PCSE ?
						Color.BLACK : Color.argb(0xFF, 0x9E, 0xAB, 0x88);
				mScreenBitmap.eraseColor(lcdColor);
			} else {
				mScreenBuffer.rewind();
				CalcInterface.GetLCD(mScreenBuffer);
				mScreenBuffer.rewind();
				mScreenBitmap.copyPixelsFromBuffer(mScreenBuffer);
			}
		}

		return mScreenBitmap;
	}

	@Override
	public void run() {
		synchronized (mScreenLock) {
			if (!mHasCreatedLcd) {
				return;
			}

			Canvas canvas = null;
			try {
				canvas = mSurfaceHolder.lockCanvas();
				if (canvas == null) {
					return;
				}
				getScreen();
				canvas.drawBitmap(mScreenBitmap, mLcdRect, mScreenRect, mPaint);
			} finally {
				if (canvas != null) {
					mSurfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
}