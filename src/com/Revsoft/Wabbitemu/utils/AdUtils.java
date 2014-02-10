package com.Revsoft.Wabbitemu.utils;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AdUtils {
	
	public static void LoadAd(AdView adView) {
		final AdRequest adRequest = new AdRequest.Builder()
		    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		    .addTestDevice("EB10E0BD305DAC3CDCBD1850A7C259A9")
	    	.build();
		adView.loadAd(adRequest);
	}
}
