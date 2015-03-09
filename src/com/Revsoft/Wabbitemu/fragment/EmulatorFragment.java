package com.Revsoft.Wabbitemu.fragment;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.Revsoft.Wabbitemu.CalcSkin;
import com.Revsoft.Wabbitemu.CalcSkin.CalcSkinChangedListener;
import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.SkinBitmapLoader;
import com.Revsoft.Wabbitemu.WabbitLCD;
import com.Revsoft.Wabbitemu.calc.CalculatorManager;
import com.Revsoft.Wabbitemu.calc.FileLoadedCallback;
import com.Revsoft.Wabbitemu.utils.PreferenceConstants;
import com.Revsoft.Wabbitemu.utils.ProgressTask;

public class EmulatorFragment extends Fragment {
	private static final String SEND_FILE_PAUSE_KEY = "SendFile";
	private static final String ACTIVITY_PAUSE_KEY = "EmulatorFragment";

	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private final CalculatorManager mCalculatorManager = CalculatorManager.getInstance();
	private final SkinBitmapLoader mSkinLoader = SkinBitmapLoader.getInstance();
	private final SkinUpdateListener mSkinUpdateListener = new SkinUpdateListener();

	private Context mContext;
	private SharedPreferences mSharedPrefs;
	private CalcSkin mCalcSkin;
	private ProgressTask mSendFileTask;

	private WabbitLCD mSurfaceView;
	private boolean mIsInitialized;
	private File mFileToHandle;
	private Runnable mRunnableToHandle;

	public void handleFile(final File f, final Runnable runnable) {
		if (!mIsInitialized) {
			mFileToHandle = f;
			mRunnableToHandle = runnable;
			return;
		}

		mCalculatorManager.pauseCalc(SEND_FILE_PAUSE_KEY);
		final String name = f.getName();
		final boolean isRom = name.endsWith(".rom") || name.endsWith(".sav");
		final int stringId = isRom ? R.string.sendingRom : R.string.sendingFile;
		final String description = mContext.getResources().getString(stringId);

		mSendFileTask = new LoadFileAsyncTask(mContext, description, false, runnable, f, isRom);
		mSendFileTask.execute();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.emulator, container);
		mSurfaceView = (WabbitLCD) view.findViewById(R.id.textureView);
		mCalcSkin = (CalcSkin) view.findViewById(R.id.skinView);
		mCalculatorManager.setScreenCallback(mSurfaceView);
		mCalculatorManager.setCalcSkin(mCalcSkin);

		mSkinLoader.registerSkinChangedListener(mSkinUpdateListener);

		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mSkinLoader.unregisterSkinChangedListener(mSkinUpdateListener);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	@Override
	public void onResume() {
		mCalculatorManager.setCalcSkin(mCalcSkin);
		mCalculatorManager.unPauseCalc(ACTIVITY_PAUSE_KEY);
		mSurfaceView.updateSkin(mSkinLoader.getLcdRect(), mSkinLoader.getSkinRect());
		mCalcSkin.invalidate();

		mIsInitialized = true;
		if (mFileToHandle != null) {
			handleFile(mFileToHandle, mRunnableToHandle);
			mFileToHandle = null;
			mRunnableToHandle = null;
		}
		super.onResume();

		updateSettings();
	}

	@Override
	public void onPause() {
		mCalculatorManager.pauseCalc(ACTIVITY_PAUSE_KEY);
		super.onPause();

		mCalculatorManager.saveCurrentRom();
		mIsInitialized = false;

		if (mSendFileTask != null) {
			mSendFileTask.cancel(false);
		}
	}

	@Nullable
	public Bitmap getScreenshot() {
		return mSurfaceView.getScreen();
	}

	public void resetCalc() {
		mCalculatorManager.resetCalc();
	}

	private void updateSettings() {
		if (mSharedPrefs.getBoolean(PreferenceConstants.STAY_AWAKE.toString(), false)) {
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	private class SkinUpdateListener implements CalcSkinChangedListener {
		@Override
		public void onCalcSkinChanged(final Rect lcdRect, final Rect lcdSkinRect) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mSurfaceView.updateSkin(lcdRect, lcdSkinRect);
					Log.d("View", "Request update");
					mCalcSkin.invalidate();
				}

			});
		}
	}

	private class LoadFileAsyncTask extends ProgressTask {
		private final Runnable mRunnable;
		private final File mFile;
		private final boolean mIsRom;

		private LoadFileAsyncTask(final Context context,
				final String descriptionString,
				final boolean isCancelable,
				final Runnable runnable,
				final File f,
				final boolean isRom)
		{
			super(context, descriptionString, isCancelable);
			mRunnable = runnable;
			mFile = f;
			mIsRom = isRom;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (mIsRom) {
				mCalcSkin.destroySkin();
				mCalcSkin.invalidate();
			}
		}

		private Boolean mSuccess;

		@Override
		protected Boolean doInBackground(final Void... params) {
			final CountDownLatch latch = new CountDownLatch(1);
			mSuccess = Boolean.FALSE;
			final FileLoadedCallback callback = new FileLoadedCallback() {

				@Override
				public void onFileLoaded(boolean wasSuccessful) {
					latch.countDown();
					mSuccess = wasSuccessful;
					mCalculatorManager.removeRomLoadListener(this);
				}

			};

			if (mIsRom) {
				mCalculatorManager.addRomLoadListener(callback);
				mCalculatorManager.loadRomFile(mFile);
			} else {
				mCalculatorManager.loadFile(mFile, callback);
			}

			try {
				latch.await();
			} catch (InterruptedException e) {
				return Boolean.FALSE;
			}

			return mSuccess;
		}

		@Override
		protected void onPostExecute(final Boolean wasSuccessful) {
			mCalculatorManager.unPauseCalc(SEND_FILE_PAUSE_KEY);

			if (!wasSuccessful && mRunnable != null) {
				mRunnable.run();
			}

			super.onPostExecute(wasSuccessful);
		}
	}
}