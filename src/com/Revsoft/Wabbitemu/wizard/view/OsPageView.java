package com.Revsoft.Wabbitemu.wizard.view;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.Revsoft.Wabbitemu.utils.ViewUtils;
import com.Revsoft.Wabbitemu.wizard.WizardNavigationController;
import com.google.android.gms.ads.AdView;

public class OsPageView extends RelativeLayout {

	private final Spinner mSpinner;
	private final RadioGroup mRadioGroup;

	private WizardNavigationController mNavController;

	public OsPageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		LayoutInflater.from(context).inflate(R.layout.os_page, this, true);

		final TextView osTerms = ViewUtils.findViewById(this, R.id.osTerms, TextView.class);
		osTerms.setMovementMethod(LinkMovementMethod.getInstance());

		mSpinner = ViewUtils.findViewById(this, R.id.osVersionSpinner, Spinner.class);
		mRadioGroup = ViewUtils.findViewById(this, R.id.setupOsAcquisistion, RadioGroup.class);

		final AdView adView = ViewUtils.findViewById(this, R.id.adView, AdView.class);
		AdUtils.loadAd(getResources(), adView);
	}

	public Spinner getSpinner() {
		return mSpinner;
	}

	public int getSelectedRadioId() {
		return mRadioGroup.getCheckedRadioButtonId();
	}

	public void configureButtons(final WizardNavigationController navController) {
		mNavController = navController;
		updateCheckId(mRadioGroup.getCheckedRadioButtonId());
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				updateCheckId(checkedId);
			}
		});
	}

	private void updateCheckId(int checkedId) {
		switch (checkedId) {
		case R.id.browseOsRadio:
			mNavController.setNextButton();
			break;
		case R.id.downloadOsRadio:
			mNavController.setFinishButton();
			break;
		}
	}
}
