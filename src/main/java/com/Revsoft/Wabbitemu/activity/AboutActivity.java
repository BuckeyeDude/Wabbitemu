package com.Revsoft.Wabbitemu.activity;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.Revsoft.Wabbitemu.utils.ViewUtils;

public class AboutActivity extends Activity {

	@Override
	public void onCreate(final Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.about);
		setTitle(R.string.about);

		final ViewGroup textLinkContainer = ViewUtils.findViewById(this, R.id.openSourceLinks, ViewGroup.class);
		for (int i = 0; i < textLinkContainer.getChildCount(); i++) {
			final View view = textLinkContainer.getChildAt(i);
			if (!(view instanceof TextView)) {
				continue;
			}
			final TextView textView = (TextView) view;
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		}

		try {
			final TextView view = ViewUtils.findViewById(this, R.id.aboutVersion, TextView.class);
			final String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			view.setText(version);
		} catch (final NameNotFoundException e) {
			Log.e("About", "Version exception", e);
		}

		AdUtils.loadAd(this.findViewById(R.id.adView));
	}
}
