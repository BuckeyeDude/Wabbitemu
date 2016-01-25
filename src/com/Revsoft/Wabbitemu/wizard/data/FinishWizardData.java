package com.Revsoft.Wabbitemu.wizard.data;

import android.support.annotation.Nullable;

import com.Revsoft.Wabbitemu.CalcInterface;


public class FinishWizardData {

	private final int mCalcModel;
	private final String mStringValue;
	private final boolean mNeedsDownload;

	public FinishWizardData(int calcModel) {
		mCalcModel = calcModel;
		mStringValue = null;
		mNeedsDownload = false;
	}

	public FinishWizardData(String filePath) {
		mCalcModel = CalcInterface.NO_CALC;
		mStringValue = filePath;
		mNeedsDownload = false;
	}

	public FinishWizardData(int calcModel, String downloadCode) {
		mCalcModel = calcModel;
		mStringValue = downloadCode;
		mNeedsDownload = true;
	}

	public boolean shouldDownloadOs() {
		return mNeedsDownload;
	}

	@Nullable
	public String getDownloadCode() {
		if (!mNeedsDownload) {
			throw new IllegalArgumentException("Cannot get download code for non download");
		}

		return mStringValue;
	}


	@Nullable
	public String getFilePath() {
		return mStringValue;
	}

	public int getCalcModel() {
		return mCalcModel;
	}
}
