package com.Revsoft.Wabbitemu.wizard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.ViewUtils;

public class BrowseRomPageView extends RelativeLayout {

	private final Button mBackButton;

	public BrowseRomPageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		LayoutInflater.from(context).inflate(R.layout.browse_rom_page, this, true);

		mBackButton = ViewUtils.findViewById(this, R.id.backButton, Button.class);
	}

	public Button getBackButton() {
		return mBackButton;
	}
}
