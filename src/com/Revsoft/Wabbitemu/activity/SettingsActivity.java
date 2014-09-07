package com.Revsoft.Wabbitemu.activity;

import android.app.Activity;
import android.os.Bundle;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.fragment.SettingsFragment;

public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.settings);
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}
}