package com.Revsoft.Wabbitemu.fragment;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.Revsoft.Wabbitemu.utils.BrowseCallback;
import com.Revsoft.Wabbitemu.utils.FileUtils;
import com.Revsoft.Wabbitemu.utils.IntentConstants;

public class BrowseInnerFragment extends ListFragment {

	private String mExtensionsRegex;
	private int mReturnId;

	public BrowseInnerFragment() {
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState)
	{
		mExtensionsRegex = getArguments().getString(IntentConstants.EXTENSION_EXTRA_REGEX);
		mReturnId = getArguments().getInt(IntentConstants.RETURN_ID);

		final AsyncTask<Void, Void, ArrayAdapter<String>> task = new AsyncTask<Void, Void, ArrayAdapter<String>>() {

			@Override
			protected ArrayAdapter<String> doInBackground(final Void... params) {
				final List<String> files = FileUtils.getInstance().getValidFiles(mExtensionsRegex);
				return new ArrayAdapter<String>(inflater.getContext(),
						android.R.layout.simple_list_item_1, files);
			}

			@Override
			protected void onPostExecute(final ArrayAdapter<String> adapter) {
				setListAdapter(adapter);
			}
		};

		if (mExtensionsRegex != null) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onListItemClick(final ListView listView, final View view, final int position, final long id) {
		final String filePath = (String) listView.getItemAtPosition(position);
		final BrowseCallback callback = (BrowseCallback) getActivity();
		callback.callback(mReturnId, filePath);
	}
}