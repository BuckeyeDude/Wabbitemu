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

public class ModelPageView extends RelativeLayout {

	private final Button mNextButton;
	private final Button mBackButton;
	private final RadioGroup mRadioGroup;

	public ModelPageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		LayoutInflater.from(context).inflate(R.layout.model_page, this, true);
		mNextButton = (Button) findViewById(R.id.nextButton);
		mBackButton = (Button) findViewById(R.id.backButton);
		mRadioGroup = (RadioGroup) findViewById(R.id.setupModelRadioGroup);

		final AdView adView = (AdView) findViewById(R.id.adView);
		AdUtils.loadAd(getResources(), adView);
	}

	public Button getNextButton() {
		return mNextButton;
	}

	public Button getBackButton() {
		return mBackButton;
	}

	public int getSelectedRadioId() {
		return mRadioGroup.getCheckedRadioButtonId();
	}
}
