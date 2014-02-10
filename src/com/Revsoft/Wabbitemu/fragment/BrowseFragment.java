package com.Revsoft.Wabbitemu.fragment;

import java.util.List;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.Revsoft.Wabbitemu.utils.BrowseCallback;
import com.Revsoft.Wabbitemu.utils.FileUtils;
import com.Revsoft.Wabbitemu.utils.IntentConstants;
import com.Revsoft.Wabbitemu.utils.ProgressTask;
import com.google.android.gms.ads.AdView;

public class BrowseFragment extends ListFragment {

	private String mDescriptionString;
	private String mExtensionsRegex;
	private int mReturnId;

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.browse, null);

		// HACK: can't seem to figure out how to hide the progress bar without this
		final ArrayAdapter<String> fakeAdapter = new ArrayAdapter<String>(inflater.getContext(),
				android.R.layout.simple_list_item_1);
		setListAdapter(fakeAdapter);

		if (getArguments() != null) {
			mExtensionsRegex = getArguments().getString(IntentConstants.EXTENSION_EXTRA_REGEX);
			mDescriptionString = getArguments().getString(IntentConstants.BROWSE_DESCRIPTION_EXTRA_STRING);
			mReturnId = getArguments().getInt(IntentConstants.RETURN_ID);

			setAdapter(inflater.getContext());
		}

		final AdView adView = (AdView) view.findViewById(R.id.adView4);
		AdUtils.LoadAd(adView);

		return view;
	}

	private void setAdapter(final Context context) {
		final ProgressTask dialog = new ProgressTask(context, mDescriptionString, true) {

			private ArrayAdapter<String> mAdapter;

			@Override
			protected Boolean doInBackground(final Void... params) {

				final List<String> files = FileUtils.getInstance().getValidFiles(mExtensionsRegex);
				mAdapter = new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, files);

				return true;
			}

			@Override
			protected void onPostExecute(final Boolean result) {
				super.onPostExecute(result);

				setListAdapter(mAdapter);
			}
		};

		dialog.execute();
	}

	@Override
	public void onListItemClick(final ListView listView, final View view, final int position, final long id) {
		final String filePath = (String) listView.getItemAtPosition(position);
		final BrowseCallback callback = (BrowseCallback) getActivity();
		callback.callback(mReturnId, filePath);
	}
}
