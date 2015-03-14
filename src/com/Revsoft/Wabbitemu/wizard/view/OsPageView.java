package com.Revsoft.Wabbitemu.wizard.view;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.google.android.gms.ads.AdView;

public class OsPageView extends RelativeLayout {

	private final Button mNextButton;
	private final Button mBackButton;
	private final Spinner mSpinner;
	private final RadioGroup mRadioGroup;
	private final OsTypeButtonClickListener mRadioClickListener = new OsTypeButtonClickListener();

	public OsPageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		LayoutInflater.from(context).inflate(R.layout.os_page, this, true);

		final TextView osTerms = (TextView) findViewById(R.id.osTerms);
		osTerms.setMovementMethod(LinkMovementMethod.getInstance());

		mNextButton = (Button) findViewById(R.id.nextButton);
		mBackButton = (Button) findViewById(R.id.backButton);
		mSpinner = (Spinner) findViewById(R.id.osVersionSpinner);
		mRadioGroup = (RadioGroup) findViewById(R.id.setupOsAcquisistion);

		final RadioButton browseOsRadio = (RadioButton) findViewById(R.id.browseOsRadio);
		browseOsRadio.setOnClickListener(mRadioClickListener);
		final RadioButton downloadOsRadio = (RadioButton) findViewById(R.id.downloadOsRadio);
		downloadOsRadio.setOnClickListener(mRadioClickListener);

		final AdView adView = (AdView) findViewById(R.id.adView);
		AdUtils.loadAd(getResources(), adView);
	}

	public Button getNextButton() {
		return mNextButton;
	}

	public Button getBackButton() {
		return mBackButton;
	}

	public Spinner getSpinner() {
		return mSpinner;
	}

	public int getSelectedRadioId() {
		return mRadioGroup.getCheckedRadioButtonId();
	}

	private class OsTypeButtonClickListener implements OnClickListener {
		@Override
		public void onClick(final View v) {
			final RadioButton button = (RadioButton) v;
			if (!button.isChecked()) {
				return;
			}

			switch (button.getId()) {
			case R.id.browseOsRadio:
				mNextButton.setText(R.string.next);
				break;
			case R.id.downloadOsRadio:
				mNextButton.setText(R.string.finish);
				break;
			}
		}
	}
}
