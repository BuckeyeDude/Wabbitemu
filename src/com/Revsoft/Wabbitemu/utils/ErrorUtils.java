package com.Revsoft.Wabbitemu.utils;

import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.Revsoft.Wabbitemu.R;

public class ErrorUtils {

	public static void showErrorDialog(final Context context, final int errorMessage) {
		final Tracker tracker = EasyTracker.getInstance(context);
		final Map<String, String> event = MapBuilder.createEvent(
				context.getClass().getName(),
				context.getResources().getString(errorMessage), null, null)
				.build();
		tracker.send(event);

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final AlertDialog dialog = builder.setTitle(R.string.errorTitle)
				.setMessage(errorMessage)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						dialog.dismiss();
					}
				})
				.create();
		dialog.show();
	}

}
