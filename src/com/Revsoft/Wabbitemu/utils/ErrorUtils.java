package com.Revsoft.Wabbitemu.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.Revsoft.Wabbitemu.R;

public class ErrorUtils {

	public static void showErrorDialog(Context context, int errorMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		AlertDialog dialog = builder.setTitle(R.string.errorTitle)
			.setMessage(errorMessage)
			.setPositiveButton(android.R.string.ok, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();									
				}
			})
			.create();
		dialog.show();
	}

}
