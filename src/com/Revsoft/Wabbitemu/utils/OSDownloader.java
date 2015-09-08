package com.Revsoft.Wabbitemu.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.R;

public class OSDownloader extends AsyncTask<String, Integer, Boolean> {

	private final UserActivityTracker mUserActivityTracker = UserActivityTracker.getInstance();
	private final ProgressDialog mProgressDialog;
	private final String mOsFilePath;
	private final WebView mWebView;
	private final String mUserAgent;
	private final int mCalcType;
	private final int mOsVersion;

	public OSDownloader(Context context, String osFilePath, WebView webView, int calcType, int osVersion) {
		mOsFilePath = osFilePath;
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setTitle(R.string.downloadingTitle);
		mProgressDialog.setMessage(context.getResources().getString(R.string.downloadingOsDescription));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mWebView = webView;
		mCalcType = calcType;
		mOsVersion = osVersion;

		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(final DialogInterface dialog) {
				OSDownloader.this.cancel(true);
			}
		});
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings()
				.setUserAgentString(
						"User-Agent: Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				view.loadUrl("javascript:$(document).ajaxComplete(function(e, xhr, settings) { Android.onFoundCode(xhr.responseText); });"+
"var getDownloadAuth; "+
"if (window.oldDeferred == undefined) {"+
"	window.oldDeferred = $.Deferred;"+
"}"+
"$.Deferred = function() {"+ 
"	var internalDefer = window.oldDeferred();"+
"	return { "+
"		always: function() { return internalDefer.always(); },"+
"		done: function() { return internalDefer.done(); }, "+
"		fail: function() { return internalDefer.fail(); },"+
"		reject: function() { return internalDefer.reject(); },"+
"		rejectWith: function(a, c) { return internalDefer.rejectWith(a, c); },"+
"		resolve: function() { return internalDefer.resolve(); },"+
"		resolveWith: function(a, c) { return internalDefer.resolveWith(a, c); },"+
"		notify: function() { return internalDefer.notify(arguments); },"+
"		notifyWith: function(a, c) { return internalDefer.reject(a, c); },"+
"		promise: function(arg) { return internalDefer.promise(arg); },"+
"		progress: function() { return internalDefer.progress(); },"+
"		state: function() { return internalDefer.state(); },"+
"		then: function(arg) {"+
"			getDownloadAuth = arg; return internalDefer.then(arg);"+ 
"		},"+
"	}"+
"};"+
"try {"+
"$.grep($._data($( '.sublayout-etdownloadbundledetails' )[0], \"events\").click, function(item) {"+
"	return item != undefined && item.selector === '.protected-download [data-fileid]'"+
"})[0].handler({"+
"    currentTarget: $('a.download-file').first()[0],"+
"	preventDefault: function() { },"+
"	stopImmediatePropagation: function() {}"+
"}); "+
"} catch (e) {"+
"}"+
"getDownloadAuth();");
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if (getStatus() != Status.PENDING) {
					return;
				}

				execute((String) null);
			}
		});
		mWebView.addJavascriptInterface(new JavaScriptInterface(), "Android");

		mWebView.clearCache(true);
		mWebView.clearView();
		mWebView.reload();
		switch (mCalcType) {
		case CalcInterface.TI_73:
			mWebView.loadUrl("https://education.ti.com/en/us/software/details/en/956CE30854A74767893104FCDF195B76/73ti73exploreroperatingsystem");
			break;
		case CalcInterface.TI_83P:
		case CalcInterface.TI_83PSE:
			mWebView.loadUrl("https://education.ti.com/en/us/software/details/en/C95956E744FB4C0A899F5A63EBEA60DD/83ti83plusoperatingsystemsoftware");
			break;
		case CalcInterface.TI_84P:
		case CalcInterface.TI_84PSE:
			mWebView.loadUrl("https://education.ti.com/en/us/software/details/en/400C88E8E75B4123BB7E90B6A676368D/ti84plusoperatingsystem");
			break;
		case CalcInterface.TI_84PCSE:
			mWebView.loadUrl("https://education.ti.com/en/us/software/details/en/5F0CBAC101194542B16B80BCE6CB3602/ti-84-plus-c-silver-edition-operating-system");
			break;
		default:
			throw new IllegalStateException("Invalid calculator type");
		}

		mUserAgent = mWebView.getSettings().getUserAgentString();

		mProgressDialog.show();
		mUserActivityTracker.reportBreadCrumb("Showing OS Download dialog");
	}

	class JavaScriptInterface {
		@JavascriptInterface
		public void onFoundCode(final String message) {
			final Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {

				@Override
				public void run() {
					if (getStatus() != Status.PENDING) {
						return;
					}

					execute(message.replace("\"", ""));
				}
			});
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(final String... args) {
		final String authCode = args[0];
		if (authCode == null) {
			return false;
		}

		final String urlString;
		switch (mCalcType) {
		case CalcInterface.TI_73:
			urlString = "https://education.ti.com/~/media/32E99F6FAEB2424D8313B0DEE7B70791";
			break;
		case CalcInterface.TI_83P:
		case CalcInterface.TI_83PSE:
			urlString = "https://education.ti.com/~/media/91432485E6EE473BA3145C368007CCA6";
			break;
		case CalcInterface.TI_84P:
		case CalcInterface.TI_84PSE:
			urlString = "https://education.ti.com/~/media/2C5C976CA12C4AC996F83A0B645FE037";
			break;
		case CalcInterface.TI_84PCSE:
			urlString = "https://education.ti.com/~/media/0BB0CC9043204D52BF22BC717A917A9A";
			break;
		default:
			throw new IllegalStateException("Invalid calculator type");
		}

		OutputStream outputStream = null;
		try {
			mUserActivityTracker.reportBreadCrumb("Downloading OS: %s from: %s", mCalcType, urlString);
			final HttpClient connection = new DefaultHttpClient();
			final HttpGet httpGet = new HttpGet(urlString);
			httpGet.setHeader("Cookie", "DownloadAuthorizationToken=" + authCode);
			httpGet.setHeader("User-Agent", mUserAgent);
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
