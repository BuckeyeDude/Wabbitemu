package com.Revsoft.Wabbitemu.wizard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.Revsoft.Wabbitemu.utils.ViewUtils;
import com.google.android.gms.ads.AdView;

public class ModelPageView extends RelativeLayout {

	private final RadioGroup mRadioGroup;

	public ModelPageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		LayoutInflater.from(context).inflate(R.layout.model_page, this, true);
		mRadioGroup = ViewUtils.findViewById(this, R.id.setupModelRadioGroup, RadioGroup.class);

		final AdView adView = ViewUtils.findViewById(this, R.id.adView, AdView.class);
		AdUtils.loadAd(getResources(), adView);
	}

	public int getSelectedRadioId() {
		return mRadioGroup.getCheckedRadioButtonId();
	}
}
