package com.Revsoft.Wabbitemu.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.fragment.BrowseFragment;
import com.Revsoft.Wabbitemu.utils.IntentConstants;
import com.Revsoft.Wabbitemu.utils.OnBrowseItemSelected;

public class BrowseActivity extends Activity implements OnBrowseItemSelected {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		final String regex = intent.getStringExtra(IntentConstants.EXTENSION_EXTRA_REGEX);
		final String description = intent.getStringExtra(IntentConstants.BROWSE_DESCRIPTION_EXTRA_STRING);

		final Bundle bundle = new Bundle();
		bundle.putString(IntentConstants.EXTENSION_EXTRA_REGEX, regex);
		bundle.putString(IntentConstants.BROWSE_DESCRIPTION_EXTRA_STRING, description);

		final BrowseFragment fragment = new BrowseFragment();
		fragment.setCallback(this);
		fragment.setArguments(bundle);

		setTitle(R.string.selectFile);
		getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
	}

	@Override
	public void onBrowseItemSelected(String fileName) {
		final Intent returnIntent = new Intent();
		returnIntent.putExtra(IntentConstants.FILENAME_EXTRA_STRING, fileName);
		setResult(Activity.RESULT_OK, returnIntent);
		finish();
	}
}