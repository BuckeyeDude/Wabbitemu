package com.Revsoft.Wabbitemu.wizard.view;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.Revsoft.Wabbitemu.utils.ViewUtils;

public class OsPageView extends RelativeLayout {

	private final Spinner mSpinner;
	private final RadioGroup mRadioGroup;

	public OsPageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		LayoutInflater.from(context).inflate(R.layout.os_page, this, true);

		final TextView osTerms = ViewUtils.findViewById(this, R.id.osTerms, TextView.class);
		osTerms.setMovementMethod(LinkMovementMethod.getInstance());

		mSpinner = ViewUtils.findViewById(this, R.id.osVersionSpinner, Spinner.class);
		mRadioGroup = ViewUtils.findViewById(this, R.id.setupOsAcquisistion, RadioGroup.class);

		AdUtils.loadAd(getResources(), this.findViewById(R.id.adView));
	}

	public Spinner getSpinner() {
		return mSpinner;
	}

	public int getSelectedRadioId() {
		return mRadioGroup.getCheckedRadioButtonId();
	}
}
