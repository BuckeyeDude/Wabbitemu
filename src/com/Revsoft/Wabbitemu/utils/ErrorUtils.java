package com.Revsoft.Wabbitemu.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;

import com.Revsoft.Wabbitemu.R;
import com.crashlytics.android.Crashlytics;

public class ErrorUtils {

	public static void showErrorDialog(final Context context, final int errorMessage) {
		showErrorDialog(context, errorMessage, null);
	}

	public static void showErrorDialog(final Context context,
			final int errorMessage,
			final OnClickListener onClickListener)
	{
		final String error = context.getResources().getString(errorMessage);
		Crashlytics.log(Log.ERROR, context.getClass().getName(), error);
		Crashlytics.logException(new Exception());

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final AlertDialog dialog = builder.setTitle(R.string.errorTitle)
				.setMessage(error)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						dialog.dismiss();
						if (onClickListener != null) {
							onClickListener.onClick(dialog, which);
						}
					}
				})
				.create();
		dialog.show();
	}

}
