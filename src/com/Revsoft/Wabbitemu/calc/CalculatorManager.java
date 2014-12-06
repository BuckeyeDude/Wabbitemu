package com.Revsoft.Wabbitemu.calc;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.CalcSkin;
import com.Revsoft.Wabbitemu.SkinBitmapLoader;
import com.Revsoft.Wabbitemu.threads.CalcThread;
import com.Revsoft.Wabbitemu.utils.PreferenceConstants;

public class CalculatorManager {
	private static final String PAUSE_KEY = "pauseKey";

	private static class SingletonHolder {
		private static final CalculatorManager SINGLETON = new CalculatorManager();
	}

	public static CalculatorManager getInstance() {
		return SingletonHolder.SINGLETON;
	}

	private final SkinBitmapLoader mSkinLoader = SkinBitmapLoader.getInstance();
	private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
	private final Map<FileLoadedCallback, String> mRomCallbacks = new ConcurrentHashMap<FileLoadedCallback, String>();
	private final AtomicBoolean mHasLoadedRom = new AtomicBoolean();
	private final CalcThread mCalcThread = new CalcThread();

	private Context mContext;
	private SharedPreferences mSharedPrefs;
	private String mCurrentRomFile;
	private CalcScreenUpdateCallback mScreenCallback;
	private CalcSkin mCalcSkin;

	private CalculatorManager() {
		// disallow instantiation
		pauseCalc(PAUSE_KEY);
		mCalcThread.start();
	}

	public void initialize(Context context) {
		mContext = context;
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public synchronized void loadRomFile(final File file) {
		if (mHasLoadedRom.get() && mCurrentRomFile.equals(file.getPath())) {
			mExecutorService.execute(new Runnable() {

				@Override
				public void run() {
					handleRomLoaded();
				}
			});
			return;
		}

		mHasLoadedRom.set(false);
		mCurrentRomFile = file.getPath();
		mExecutorService.execute(new LoadRomRunnable());
	}

	public void loadFile(final File file, final FileLoadedCallback callback) {
		mExecutorService.execute(new LoadFileRunnable(file, callback));
	}
	
	public void addRomLoadListener(FileLoadedCallback callback) {
		mRomCallbacks.put(callback, PAUSE_KEY);
	}

	public void removeRomLoadListener(FileLoadedCallback callback) {
		mRomCallbacks.remove(callback);
	}

	public void setScreenCallback(CalcScreenUpdateCallback callback) {
		if (mCalcThread != null) {
			mCalcThread.setScreenUpdateCallback(callback);
		}

		mScreenCallback = callback;
	}

	public void setCalcSkin(CalcSkin calcSkin) {
		mCalcSkin = calcSkin;
	}

	public void pauseCalc(String pauseKey) {
		if (mCalcThread != null) {
			mCalcThread.setPaused(pauseKey, true);
		}
	}
	
	public void unPauseCalc(String pauseKey) {
		if (mCalcThread != null) {
			mCalcThread.setPaused(pauseKey, false);
		}
	}

	public void resetCalc() {
		if (mCalcThread != null) {
			mCalcThread.resetCalc();
		}
	}

	public void saveCurrentRom() {
		if (mCurrentRomFile == null || mCurrentRomFile.equals("")) {
			return;
		}

		mExecutorService.execute(new SaveCurrentRomRunnable());
	}

	private void updateCurrentRomSetting() {
		final SharedPreferences.Editor editor = mSharedPrefs.edit();
		editor.putString(PreferenceConstants.ROM_PATH.toString(), mCurrentRomFile)
				.putInt(PreferenceConstants.ROM_MODEL.toString(), CalcInterface.GetModel())
				.commit();
	}

	private void handleRomLoaded() {
		mCalcThread.setScreenUpdateCallback(mScreenCallback);
		if (mCalcSkin != null) {
			mSkinLoader.loadSkinAndKeymap(CalcInterface.GetModel());
		}
		notifyRomCallbacks(true);
	}

	private void notifyRomCallbacks(boolean wasSuccessful) {
		mHasLoadedRom.set(true);
		for (FileLoadedCallback callback : mRomCallbacks.keySet()) {
			callback.onFileLoaded(wasSuccessful);
		}

		mRomCallbacks.clear();
	}

	private class LoadFileRunnable implements Runnable {
		private final File file;
		private final FileLoadedCallback callback;

		private LoadFileRunnable(File file, FileLoadedCallback callback) {
			this.file = file;
			this.callback = callback;
		}

		@Override
		public void run() {
			final int linkResult = CalcInterface.LoadFile(file.getPath());
			callback.onFileLoaded(linkResult == 0);
		}
	}

	private class SaveCurrentRomRunnable implements Runnable {
		@Override
		public void run() {
			final File tempDir = mContext.getFilesDir();
			mCurrentRomFile = tempDir.getAbsoluteFile() + "/Wabbitemu.sav";
			final boolean wasSuccessful = CalcInterface.SaveCalcState(mCurrentRomFile);
			if (wasSuccessful) {
				updateCurrentRomSetting();
			}
		}
	}

	private class LoadRomRunnable implements Runnable {
		@Override
		public void run() {
			final Boolean success;
			pauseCalc(PAUSE_KEY);

			CalcInterface.SetAutoTurnOn(mSharedPrefs.getBoolean(PreferenceConstants.AUTO_TURN_ON.toString(), true));
			success = CalcInterface.CreateCalc(mCurrentRomFile);
			if (!success) {
				notifyRomCallbacks(false);
				return;
			}

			unPauseCalc(PAUSE_KEY);
			handleRomLoaded();
		}
	}
}
