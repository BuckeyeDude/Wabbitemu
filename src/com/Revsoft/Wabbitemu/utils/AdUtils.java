package com.Revsoft.Wabbitemu.utils;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;

import com.Revsoft.Wabbitemu.R;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AdUtils {

	public static void loadAd(Resources resources, AdView adView) {
		if (!resources.getBoolean(R.bool.shouldShowAds)) {
			adView.setVisibility(View.GONE);
			return;
		}

		final AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("EB10E0BD305DAC3CDCBD1850A7C259A9")
				.build();
		try {
			adView.loadAd(adRequest);
		} catch (Exception e) {
			Log.d("AdUtils", "Ad threw exception, avoiding crash %s", e);
			Crashlytics.logException(e);
		}
	}
}
