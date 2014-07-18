package com.Revsoft.Wabbitemu.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.R;

public class OSDownloader extends AsyncTask<Integer, Integer, Boolean> {

	private final ProgressDialog mProgressDialog;
	private final String mOsFilePath;

	public OSDownloader(final Context context, final String osFilePath) {
		mOsFilePath = osFilePath;
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setTitle(R.string.downloadingTitle);
		mProgressDialog.setMessage(context.getResources().getString(R.string.downloadingOsDescription));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(final DialogInterface dialog) {
				OSDownloader.this.cancel(true);
			}
		});
	}

	@Override
	protected Boolean doInBackground(final Integer... args) {
		final int calcType = args[0];
		final int version = args[1];
		final String urlString;
		switch (calcType) {
		case CalcInterface.TI_73:
			urlString = "http://education.ti.com/en/asia/~/media/Files/Download%20Center/Software/73/TI73_OS.73u";
			break;
		case CalcInterface.TI_83P:
		case CalcInterface.TI_83PSE:
			urlString = "http://education.ti.com/en/asia/~/media/Files/Download%20Center/Software/83plus/TI83Plus_OS.8Xu";
			break;
		case CalcInterface.TI_84P:
		case CalcInterface.TI_84PSE:
			if (version == 0) {
				urlString = "http://education.ti.com/en/asia/~/media/Files/Download%20Center/Software/83plus/TI84Plus_OS.8Xu";
			} else {
				urlString = "http://education.ti.com/en/asia/~/media/Files/Download%20Center/Software/83plus/TI84Plus_OS243.8Xu";
			}
			break;
		case CalcInterface.TI_84PCSE:
			if (version == 0) {
				urlString = "http://education.ti.com/download/en/ASIA/5F0CBAC101194542B16B80BCE6CB3602/0BB0CC9043204D52BF22BC717A917A9A/TI84PlusC_OS-4.20.8Cu";
			} else {
				urlString = "http://education.ti.com/download/en/ASIA/5F0CBAC101194542B16B80BCE6CB3602/4D5547F48BBA4384BB85A645D7772A1A/TI84PlusC_OS.8Cu";
			}
			break;
		default:
			throw new IllegalStateException("Invalid calculator type");
		}

		final URL url;
		try {
			url = new URL(urlString);
		} catch (final MalformedURLException e) {
			throw new IllegalStateException("Url bad");
		}

		OutputStream outputStream = null;
		try {
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			final InputStream inputStream = connection.getInputStream();
			outputStream = new FileOutputStream(mOsFilePath);

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return false;
			}

			final int fileLength = connection.getContentLength();

			final byte data[] = new byte[4096];
			long total = 0;
			int count;
			while ((count = inputStream.read(data)) != -1) {
				if (isCancelled()) {
					return null;
				}

				total += count;
				if (fileLength > 0) {
					publishProgress((int) (total * 100 / fileLength));
				}
				outputStream.write(data, 0, count);
			}
		} catch (final IOException e) {
			return false;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (final IOException e) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog.show();
	}

	@Override
	protected void onProgressUpdate(final Integer... progress) {
		super.onProgressUpdate(progress);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(final Boolean result) {
		super.onPostExecute(result);
		mProgressDialog.dismiss();
	}
}
