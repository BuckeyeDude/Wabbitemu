package com.Revsoft.Wabbitemu.wizard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.google.android.gms.ads.AdView;

public class LandingPageView extends RelativeLayout {

	private final Button mNextButton;
	private final RadioGroup mRadioGroup;

	public LandingPageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		LayoutInflater.from(context).inflate(R.layout.landing_page, this, true);
		mNextButton = (Button) findViewById(R.id.nextButton);
		mRadioGroup = (RadioGroup) findViewById(R.id.setupOptionsRadioGroup);

		final AdView adView = (AdView) findViewById(R.id.adView);
		AdUtils.loadAd(getResources(), adView);
	}

	public Button getNextButton() {
		return mNextButton;
	}

	public int getSelectedRadioId() { 
		return mRadioGroup.getCheckedRadioButtonId();
	}
}
