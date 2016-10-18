package com.Revsoft.Wabbitemu.utils;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashSet;
import java.util.Set;

public class AdUtils {

    private static Set<AdView> sLoadedAds = new HashSet<>();

    public static void initialize(Application application) {
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityStopped(Activity activity) {
                // no-op
            }

            @Override
            public void onActivityStarted(Activity activity) {
                // no-op
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                // no-op
            }

            @Override
            public void onActivityResumed(Activity activity) {
                for (AdView adView : sLoadedAds) {
                    if (activity.findViewById(adView.getId()) != null) {
                        adView.resume();
                    }
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                for (AdView adView : sLoadedAds) {
                    if (activity.findViewById(adView.getId()) != null) {
                        adView.pause();
                    }
                }
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                final AdView[] array = new AdView[sLoadedAds.size()];
                sLoadedAds.toArray(array);
                for (AdView adView : array) {
                    if (activity.findViewById(adView.getId()) != null) {
                        destroyView(adView);
                    }
                }

                Log.d("AdUtils", "Activity destroyed, Ads visible: " + sLoadedAds.size());
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // no-op
            }
        });
    }

    public static void loadAd(final View view) {
        final AdView adView = (AdView) view;

        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("EB10E0BD305DAC3CDCBD1850A7C259A9")
                .build();

        try {
            adView.loadAd(adRequest);
            sLoadedAds.add(adView);
        } catch (Exception e) {
            Log.d("AdUtils", "Ad threw exception, avoiding crash %s", e);
            Crashlytics.logException(e);
        }

        Log.d("AdUtils", "Showing ad, Ads visible: " + sLoadedAds.size());
    }

    public static void destroyView(View view) {
        final AdView adView = (AdView) view;
        adView.destroy();
        sLoadedAds.remove(adView);
    }
}
