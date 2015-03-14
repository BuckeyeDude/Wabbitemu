package com.Revsoft.Wabbitemu.wizard.data;

import android.support.annotation.Nullable;

import com.Revsoft.Wabbitemu.CalcInterface;


public class FinishWizardData {

	private final int mCalcModel;
	private final String mFilePath;

	public FinishWizardData(int calcModel) {
		mCalcModel = calcModel;
		mFilePath = null;
	}

	public FinishWizardData(String filePath) {
		mCalcModel = CalcInterface.NO_CALC;
		mFilePath = filePath;
	}

	public FinishWizardData(int calcModel, String filePath) {
		mCalcModel = calcModel;
		mFilePath = filePath;
	}

	@Nullable
	public String getFilePath() {
		return mFilePath;
	}

	public int getCalcModel() {
		return mCalcModel;
	}
}
