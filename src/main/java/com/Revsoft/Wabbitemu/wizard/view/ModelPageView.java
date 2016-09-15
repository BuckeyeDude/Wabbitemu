package com.Revsoft.Wabbitemu.wizard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.Revsoft.Wabbitemu.utils.ViewUtils;

public class ModelPageView extends RelativeLayout {

	private final RadioGroup mRadioGroup;

	public ModelPageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		LayoutInflater.from(context).inflate(R.layout.model_page, this, true);
		mRadioGroup = ViewUtils.findViewById(this, R.id.setupModelRadioGroup, RadioGroup.class);

		AdUtils.loadAd(getResources(), this.findViewById(R.id.adView));
	}

	public int getSelectedRadioId() {
		return mRadioGroup.getCheckedRadioButtonId();
	}
}
