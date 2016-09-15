package com.Revsoft.Wabbitemu.utils;

import io.fabric.sdk.android.Fabric;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

import com.Revsoft.Wabbitemu.BuildConfig;
import com.Revsoft.Wabbitemu.utils.AnalyticsConstants.UserActionActivity;
import com.Revsoft.Wabbitemu.utils.AnalyticsConstants.UserActionEvent;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

public class UserActivityTracker {

    private static class SingletonHolder {
        private static final UserActivityTracker SINGLETON = new UserActivityTracker();
    }

    public static UserActivityTracker getInstance() {
        return SingletonHolder.SINGLETON;
    }

    private boolean mIsInitialized;

    private UserActivityTracker() {
        // Disallow instantiation
    }

    public void initializeIfNecessary(Context context) {
        if (mIsInitialized) {
            return;
        }

        mIsInitialized = true;
        Fabric.with(context, new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build());
        final String androidId = Secure.getString(context.getApplicationContext().getContentResolver(),
                Secure.ANDROID_ID);
        if (androidId == null) {
            Crashlytics.setUserIdentifier("emptyAndroidId");
        } else {
            Crashlytics.setUserIdentifier(androidId);
        }
    }

    public void reportActivityStart(final Activity activity) {
        Crashlytics.log("Starting " + activity.getClass().getSimpleName());
    }

    public void reportActivityStop(final Activity activity) {
        Crashlytics.log("Stop " + activity.getClass().getSimpleName());
    }

    public void reportBreadCrumb(String breadcrumb) {
        Crashlytics.log(Log.INFO, "UserActivityTracker", breadcrumb);
    }

    public void reportBreadCrumb(String format, Object... args) {
        reportBreadCrumb(String.format(format, args));
    }

    public void setKey(String key, int value) {
        Crashlytics.setInt(key, value);
    }

    public void reportUserAction(final UserActionActivity activity, final UserActionEvent event) {
        reportUserAction(activity, event, null);
    }

    public void reportUserAction(final UserActionActivity activity, final UserActionEvent event, final String extra) {
        final String log = String.format("Activity: [%s] Event: [%s] Extra: [%s]", activity, event, extra);
        Crashlytics.log(log);

        Log.i("Wabbitemu", log);
    }
}
