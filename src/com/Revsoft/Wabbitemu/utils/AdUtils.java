package com.Revsoft.Wabbitemu.utils;

import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AdUtils {

	public static void LoadAd(final AdView adView) {
		final AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("EB10E0BD305DAC3CDCBD1850A7C259A9")
				.build();
		try {
			adView.loadAd(adRequest);
		} catch (Exception e) {
			Log.d("AdUtils", "Ad threw exception, avoiding crash %s", e);
		}
	}
}
