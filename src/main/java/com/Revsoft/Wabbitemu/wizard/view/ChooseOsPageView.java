package com.Revsoft.Wabbitemu.wizard.view;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.Revsoft.Wabbitemu.utils.ViewUtils;

import javax.annotation.Nonnull;

public class ChooseOsPageView extends RelativeLayout {

	private final TextView mMessage;
	private final ProgressBar mLoadingSpinner;

	public ChooseOsPageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		LayoutInflater.from(context).inflate(R.layout.choose_os_page, this, true);
		AdUtils.loadAd(getResources(), this.findViewById(R.id.adView));
		mMessage = (TextView) findViewById(R.id.message);
		mLoadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);
	}

	@Nonnull
	public TextView getMessage() {
		return mMessage;
	}

	public ProgressBar getLoadingSpinner() {
		return mLoadingSpinner;
	}
}
