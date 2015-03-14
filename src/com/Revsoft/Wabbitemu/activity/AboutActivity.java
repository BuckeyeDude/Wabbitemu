package com.Revsoft.Wabbitemu.activity;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.Revsoft.Wabbitemu.utils.ViewUtils;
import com.google.android.gms.ads.AdView;

public class AboutActivity extends Activity {

	@Override
	public void onCreate(final Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.about);
		setTitle(R.string.about);

		final TextView colorPickerLink = ViewUtils.findViewById(this, R.id.colorPickerLink, TextView.class);
		colorPickerLink.setMovementMethod(LinkMovementMethod.getInstance());

		final TextView bootFreeLink = ViewUtils.findViewById(this, R.id.bootFreeLink, TextView.class);
		bootFreeLink.setMovementMethod(LinkMovementMethod.getInstance());

		try {
			final TextView view = ViewUtils.findViewById(this, R.id.aboutVersion, TextView.class);
			final String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			view.setText(version);
		} catch (final NameNotFoundException e) {
			Log.e("About", "Version exception", e);
		}

		AdUtils.loadAd(getResources(), ViewUtils.findViewById(this, R.id.adView, AdView.class));
	}
}
