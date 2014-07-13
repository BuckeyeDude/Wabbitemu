package com.Revsoft.Wabbitemu.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.Revsoft.Wabbitemu.fragment.SettingsFragment;
import com.Revsoft.Wabbitemu.R;

public class SettingsActivity extends FragmentActivity {
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.settings);
		// Display the fragment as the main content.
		getSupportFragmentManager().beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();
	}
}