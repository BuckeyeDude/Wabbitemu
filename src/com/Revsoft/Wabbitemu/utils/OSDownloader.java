package com.Revsoft.Wabbitemu.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.wizard.controller.OsDownloadPageController;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class OSDownloader extends AsyncTask<Integer, Integer, Boolean> {

    private final UserActivityTracker mUserActivityTracker = UserActivityTracker.getInstance();
    private final ProgressDialog mProgressDialog;
    private final String mOsFilePath;
    private final int mCalcType;
    private final int mOsVersion;
    private final String mAuthCode;

    public OSDownloader(Context context, String osFilePath, int calcType, int osVersion, String authCode) {
        mOsFilePath = osFilePath;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(R.string.downloadingTitle);
        mProgressDialog.setMessage(context.getResources().getString(R.string.downloadingOsDescription));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mCalcType = calcType;
        mOsVersion = osVersion;
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
        final String urlString;
        switch (mCalcType) {
            case CalcInterface.TI_73:
                urlString = "https://education.ti.com/~/media/32E99F6FAEB2424D8313B0DEE7B70791";
                break;
            case CalcInterface.TI_83P:
            case CalcInterface.TI_83PSE:
                urlString = "https://education.ti.com/~/media/EEB252CDF6A748309894C1790408D0E7";
                break;
            case CalcInterface.TI_84P:
            case CalcInterface.TI_84PSE:
                if (mOsVersion == 0) {
                    urlString = "https://education.ti.com/~/media/A943680938CC460E8CB04554E99D665B";
                } else {
                    urlString = "https://education.ti.com/~/media/DA0795A5F0FA45D2A9AC03BB11B8245F";
                }
                break;
            case CalcInterface.TI_84PCSE:
                urlString = "https://education.ti.com/~/media/4D5547F48BBA4384BB85A645D7772A1A";
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
            mUserActivityTracker.reportBreadCrumb("Downloading OS: %s from: %s", mCalcType, url);
            final HttpClient connection = new DefaultHttpClient();
            final HttpGet httpGet = new HttpGet(urlString);
            httpGet.setHeader("User-Agent", OsDownloadPageController.USER_AGENT);
            if (mAuthCode != null) {
                httpGet.setHeader("Cookie", "DownloadAuthorizationToken=" + mAuthCode);
            }
            final HttpResponse response = connection.execute(httpGet);
            final InputStream inputStream = response.getEntity().getContent();
            outputStream = new FileOutputStream(mOsFilePath);

            final int responseCode = response.getStatusLine().getStatusCode();
            mUserActivityTracker.reportBreadCrumb("OS Response code: %s", responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            final long fileLength = response.getEntity().getContentLength();

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
