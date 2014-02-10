package com.Revsoft.Wabbitemu.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class ProgressTask extends AsyncTask<Void, Void, Boolean> {

	private ProgressDialog mProgress;
	private final String mDescriptionString;
	private final Context mContext;
	private final boolean mIsCancelable;

	public ProgressTask(final Context context, final String descriptionString,
			final boolean isCancelable) {
		mContext = context;
		mDescriptionString = descriptionString;
		mIsCancelable = isCancelable;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		mProgress = new ProgressDialog(mContext);
		mProgress.setTitle("Loading");
		mProgress.setMessage(mDescriptionString);
		mProgress.setCancelable(mIsCancelable);
		mProgress.show();
	}

	@Override
	protected void onPostExecute(final Boolean arg) {
		super.onPostExecute(arg);

		if (mProgress != null) {
			mProgress.dismiss();
		}
	}
}
