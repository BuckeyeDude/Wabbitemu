package com.Revsoft.Wabbitemu.fragment;

import java.io.File;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.CalcSkin;
import com.Revsoft.Wabbitemu.CalcSkin.CalcSkinChangedListener;
import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.WabbitLCD;
import com.Revsoft.Wabbitemu.threads.CalcThread;
import com.Revsoft.Wabbitemu.utils.PreferenceConstants;
import com.Revsoft.Wabbitemu.utils.ProgressTask;

public class EmulatorFragment extends Fragment {
	private static final String SEND_FILE_PAUSE_KEY = "SendFile";
	private static final String ACTIVITY_PAUSE_KEY = "EmulatorFragment";

	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private Context mContext;
	private CalcThread mCalcThread;
	private CalcSkin mCalcSkin;
	private String mCurrentRomFile;
	private SharedPreferences mSharedPrefs;
	private ProgressTask mSendFileTask;

	private WabbitLCD mSurfaceView;
	private boolean mIsInitialized;
	private File mFileToHandle;
	private Runnable mRunnableToHandle;

	public void handleFile(final File f, final Runnable runnable) {
		if (mCalcThread != null) {
			mCalcThread.setPaused(SEND_FILE_PAUSE_KEY, true);
		}

		if (!mIsInitialized) {
			mFileToHandle = f;
			mRunnableToHandle = runnable;
			return;
		}

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
		mCalcSkin.registerSkinChangedListener(new CalcSkinChangedListener() {

			@Override
			public void onCalcSkinChanged(final Rect lcdRect, final Rect lcdSkinRect) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						mSurfaceView.updateSkin(lcdRect, lcdSkinRect);
						mCalcSkin.invalidate();
					}

				});
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	@Override
	public void onPause() {
		if (mCalcThread != null) {
			mCalcThread.setPaused(ACTIVITY_PAUSE_KEY, true);
		}

		super.onPause();

		if (createTempSave()) {
			saveCurrentRom();
		}

		mIsInitialized = false;
	}

	@Override
	public void onResume() {
		mIsInitialized = true;
		if (mFileToHandle != null) {
			handleFile(mFileToHandle, mRunnableToHandle);
			mFileToHandle = null;
			mRunnableToHandle = null;
		}

		if (mCalcThread != null) {
			mCalcThread.setPaused(ACTIVITY_PAUSE_KEY, false);
		}

		super.onResume();

		updateSettings();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mCalcThread != null) {
			mCalcThread.interrupt();
		}

		if (mSendFileTask != null) {
			mSendFileTask.cancel(false);
		}
	}

	@Nullable
	public Bitmap getScreenshot() {
		if (mCalcThread == null) {
			return null;
		}

		return mCalcThread.getScreenshot();
	}

	public void resetCalc() {
		if (mCalcThread != null) {
			mCalcThread.resetCalc();
		}
	}

	private boolean createTempSave() {
		if (mCurrentRomFile == null || mCurrentRomFile.equals("")) {
			return false;
		}

		final File tempDir = getActivity().getFilesDir();
		mCurrentRomFile = tempDir.getAbsoluteFile() + "/Wabbitemu.sav";
		return CalcInterface.SaveCalcState(mCurrentRomFile);
	}

	private void saveCurrentRom() {
		final SharedPreferences.Editor editor = mSharedPrefs.edit();
		editor.putString(PreferenceConstants.ROM_PATH, mCurrentRomFile);
		editor.commit();
	}

	private void updateSettings() {
		if (mSharedPrefs.getBoolean(PreferenceConstants.STAY_AWAKE, false)) {
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
			CalcInterface.SetAutoTurnOn(mSharedPrefs.getBoolean(PreferenceConstants.AUTO_TURN_ON, true));
		}

		@Override
		protected Boolean doInBackground(final Void... params) {
			final Boolean success;
			if (mIsRom) {
				success = loadRom();
			} else {
				success = loadFile();
			}

			return success;
		}

		private Boolean loadFile() {
			final int linkResult = CalcInterface.LoadFile(mFile.getPath());
			return linkResult == 0 ? Boolean.TRUE : Boolean.FALSE;
		}

		private Boolean loadRom() {
			final Boolean success;
			mCurrentRomFile = mFile.getPath();
			if (mCalcThread != null) {
				mCalcThread.interrupt();
				try {
					mCalcThread.join();
				} catch (final InterruptedException e) {
					return Boolean.FALSE;
				}
			}

			success = CalcInterface.CreateCalc(mCurrentRomFile);
			if (!success) {
				return Boolean.FALSE;
			}

			mCalcSkin.loadSkinAndKeymap();
			if (!isCancelled()) {
				mCalcThread = new CalcThread(mSurfaceView);
				mCalcThread.start();
			}

			return success;
		}

		@Override
		protected void onPostExecute(final Boolean wasSuccessful) {
			if (mCalcThread != null) {
				mCalcThread.setPaused(SEND_FILE_PAUSE_KEY, false);
			}

			if (wasSuccessful) {
				if (mIsRom) {
					saveCurrentRom();
				}
			} else if (mRunnable != null) {
				mRunnable.run();
			}

			super.onPostExecute(wasSuccessful);
		}
	}
}