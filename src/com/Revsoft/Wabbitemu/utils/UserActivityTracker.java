package com.Revsoft.Wabbitemu.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AnalyticsConstants.UserActionActivity;
import com.Revsoft.Wabbitemu.utils.AnalyticsConstants.UserActionEvent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.splunk.mint.Mint;

public class UserActivityTracker {
	private static final int REPORT_DELAY = 2000;
	private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);

	private static class SingletonHolder {
		private static final UserActivityTracker SINGLETON = new UserActivityTracker();
	}

	public static UserActivityTracker getInstance() {
		return SingletonHolder.SINGLETON;
	}

	private Context mContext;

	private UserActivityTracker() {
		// Disallow instantiation
	}

	public void initialize(Context context) {
		mContext = context;
		Mint.initAndStartSession(mContext, mContext.getString(R.string.mintKey));
	}

	public void reportActivityStart(final Activity activity) {
		EXECUTOR.schedule(new Runnable() {

			@Override
			public void run() {
				Mint.leaveBreadcrumb("Start " + activity.getClass().getSimpleName());
				EasyTracker.getInstance(mContext).activityStart(activity);
			}
		}, REPORT_DELAY, TimeUnit.MILLISECONDS);
	}

	public void reportActivityStop(final Activity activity) {
		EXECUTOR.schedule(new Runnable() {

			@Override
			public void run() {
				Mint.leaveBreadcrumb("Stop " + activity.getClass().getSimpleName());
				EasyTracker.getInstance(mContext).activityStop(activity);
			}
		}, REPORT_DELAY, TimeUnit.MILLISECONDS);
	}

	public void reportBreadCrumb(final String breadcrumb) {
		EXECUTOR.schedule(new Runnable() {

			@Override
			public void run() {
				Mint.leaveBreadcrumb(breadcrumb);
			}
		}, REPORT_DELAY, TimeUnit.MILLISECONDS);
	}

	public void reportUserAction(final UserActionActivity activity, final UserActionEvent event) {
		reportUserAction(activity, event, null);
	}

	public void reportUserAction(final UserActionActivity activity, final UserActionEvent event, final String extra) {
		EXECUTOR.schedule(new Runnable() {

			@Override
			public void run() {
				Mint.logEvent(event.toString());
				final Tracker tracker = EasyTracker.getInstance(mContext);
				tracker.send(MapBuilder.createEvent(activity.toString(), event.toString(), extra, null).build());
			}
		}, REPORT_DELAY, TimeUnit.MILLISECONDS);
	}
}
