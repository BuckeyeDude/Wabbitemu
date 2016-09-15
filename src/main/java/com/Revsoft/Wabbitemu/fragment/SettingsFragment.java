package com.Revsoft.Wabbitemu.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.Revsoft.Wabbitemu.R;

public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}