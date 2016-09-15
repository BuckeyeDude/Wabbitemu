package com.Revsoft.Wabbitemu.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.calc.CalcModel;
import com.Revsoft.Wabbitemu.wizard.controller.OsDownloadPageController;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OSDownloader extends AsyncTask<Integer, Integer, Boolean> {

    private final UserActivityTracker mUserActivityTracker = UserActivityTracker.getInstance();
    private final ProgressDialog mProgressDialog;
    private final String mOsDownloadUrl;
    private final String mOsFilePath;
    private final CalcModel mCalcType;
    private final String mAuthCode;

    protected OSDownloader(Context context,
            String osDownloadUrl,
            String osFilePath,
            CalcModel calcType,
            String authCode)
    {
        mOsDownloadUrl = osDownloadUrl;
        mOsFilePath = osFilePath;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(R.string.downloadingTitle);
        mProgressDialog.setMessage(context.getResources().getString(R.string.downloadingOsDescription));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mCalcType = calcType;
        mAuthCode = authCode;

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialog) {
                OSDownloader.this.cancel(true);
            }
        });


        mProgressDialog.show();
        mUserActivityTracker.reportBreadCrumb("Showing OS Download dialog");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(final Integer... args) {
        final URL url;
        try {
            url = new URL(mOsDownloadUrl);
        } catch (final MalformedURLException e) {
            UserActivityTracker.getInstance().reportBreadCrumb("OS download url was bad " + mOsDownloadUrl);
            return Boolean.FALSE;
        }

        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            mUserActivityTracker.reportBreadCrumb("Downloading OS: %s from: %s", mCalcType, url);
            final OkHttpClient connection = new OkHttpClient();
            final Request.Builder request = new Request.Builder()
                    .url(mOsDownloadUrl)
                    .addHeader("User-Agent", OsDownloadPageController.USER_AGENT);
            if (mAuthCode != null) {
                request.addHeader("Cookie", "DownloadAuthorizationToken=" + mAuthCode);
            }

            final Response response = connection.newCall(request.build()).execute();
            inputStream = response.body().byteStream();
            outputStream = new FileOutputStream(mOsFilePath);

            final int responseCode = response.code();
            mUserActivityTracker.reportBreadCrumb("OS Response code: %s", responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            final long fileLength = response.body().contentLength();
            final byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = inputStream.read(data)) != -1) {
                if (isCancelled()) {
                    mUserActivityTracker.reportBreadCrumb("OS download cancelled");
                    return null;
                }

                total += count;
                if (fileLength > 0) {
                    publishProgress((int) (total * 100 / fileLength));
                }
                outputStream.write(data, 0, count);
            }
        } catch (final IOException e) {
            mUserActivityTracker.reportBreadCrumb("OS download exception %s", e);
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                    inputStream.close();
                } catch (final IOException e) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(final Integer... progress) {
        super.onProgressUpdate(progress);

        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        mProgressDialog.dismiss();
        mUserActivityTracker.reportBreadCrumb("OS Download cancelled. Hiding dialog");
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        super.onPostExecute(result);

        mProgressDialog.dismiss();
        mUserActivityTracker.reportBreadCrumb("Hiding OS Download dialog. Result [%s]", result);
    }
}
