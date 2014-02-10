package com.Revsoft.Wabbitemu.activity;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.fragment.BrowseFragment;
import com.Revsoft.Wabbitemu.utils.BrowseCallback;
import com.Revsoft.Wabbitemu.utils.IntentConstants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class BrowseActivity extends Activity implements BrowseCallback {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		String regex = intent.getStringExtra(IntentConstants.EXTENSION_EXTRA_REGEX);
		String description = intent.getStringExtra(IntentConstants.BROWSE_DESCRIPTION_EXTRA_STRING);
		
		Bundle bundle = new Bundle();
		bundle.putString(IntentConstants.EXTENSION_EXTRA_REGEX, regex);
		bundle.putString(IntentConstants.BROWSE_DESCRIPTION_EXTRA_STRING, description);
		
		BrowseFragment fragment = new BrowseFragment();
		fragment.setArguments(bundle);
		
		setTitle(R.string.selectFile);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();
	}

	@Override
	public void callback(int currentId, String fileName) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra(IntentConstants.FILENAME_EXTRA_STRING, fileName);
		setResult(Activity.RESULT_OK, returnIntent);
		finish();
	}
}