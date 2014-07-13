package com.Revsoft.Wabbitemu.fragment;

import java.io.File;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.WabbitLCD;
import com.Revsoft.Wabbitemu.threads.CalcThread;
import com.Revsoft.Wabbitemu.utils.PreferenceConstants;
import com.Revsoft.Wabbitemu.utils.ProgressTask;
import com.Revsoft.Wabbitemu.R;

public class EmulatorFragment extends Fragment {

	private static final int DELAY_MILLISECONDS = 500;
	private static final String SEND_FILE_PAUSE_KEY = "SendFile";
	private static final String ACTIVITY_PAUSE_KEY = "EmulatorFragment";

	private CalcThread mCalcThread;
	private String mCurrentRomFile;

	private WabbitLCD mSurfaceView;
	private boolean mIsInitialized;

	public void handleFile(final File f, final Runnable runnable) {
		if (mCalcThread != null) {
			mCalcThread.setPaused(SEND_FILE_PAUSE_KEY, true);
		}

		if (!mIsInitialized) {
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					handleFile(f, runnable);
				}
			}, DELAY_MILLISECONDS);
			return;
		}

		final boolean isRom = f.getName().endsWith(".rom")
				|| f.getName().endsWith(".sav");
		final int stringId = isRom ? R.string.sendingRom : R.string.sendingFile;
		final String description = getActivity().getResources().getString(stringId);
		final ProgressTask task = new ProgressTask(getActivity(), description, false) {

			@Override
			protected Boolean doInBackground(final Void... params) {
				final boolean success;
				final int linkResult;
				if (isRom) {
					final SharedPreferences sharedPrefs = PreferenceManager
							.getDefaultSharedPreferences(getActivity());
					CalcInterface.SetAutoTurnOn(sharedPrefs.getBoolean(PreferenceConstants.AUTO_TURN_ON, true));

					mCurrentRomFile = f.getPath();
					if (mCalcThread != null) {
						mCalcThread.interrupt();
					}
					mCalcThread = new CalcThread(mSurfaceView);
					CalcInterface.CreateCalc(mCurrentRomFile);
					mCalcThread.start();
					success = true;

					mSurfaceView.updateSkin();
					saveCurrentRom();
				} else {
					linkResult = CalcInterface.LoadFile(f.getPath());
					success = linkResult == 0;
				}

				return success;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(final Boolean result) {
				super.onPostExecute(result);

				if (mCalcThread != null) {
					mCalcThread.setPaused(SEND_FILE_PAUSE_KEY, false);
				}

				if (!result && runnable != null) {
					runnable.run();
				}
			}
		};
		task.execute();
	}

	public Bitmap getScreenshot() {
		return mCalcThread.getScreenshot();
	}

	public void resetCalc() {
		mCalcThread.resetCalc();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.emulator, null);
		mSurfaceView = (WabbitLCD) view.findViewById(R.id.textureView);
		return view;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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

		if (mCalcThread != null) {
			mCalcThread.setPaused(ACTIVITY_PAUSE_KEY, false);
		}

		super.onResume();

		updateSettings();
	}

	private boolean createTempSave() {
		if (mCurrentRomFile == null || "".equals(mCurrentRomFile)) {
			return false;
		}

		final File tempDir = getActivity().getFilesDir();
		mCurrentRomFile = tempDir.getAbsoluteFile() + "/Wabbitemu.sav";
		CalcInterface.SaveCalcState(mCurrentRomFile);
		return true;
	}

	private void saveCurrentRom() {
		final SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		if (sharedPrefs == null) {
			return;
		}

		final SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(PreferenceConstants.ROM_PATH, mCurrentRomFile);
		editor.commit();
	}

	private void updateSettings() {
		final SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		if (sharedPrefs == null) {
			return;
		}

		if (sharedPrefs.getBoolean(PreferenceConstants.STAY_AWAKE, false)) {
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
}
