package com.Revsoft.Wabbitemu.fragment;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.Revsoft.Wabbitemu.utils.FileUtils;
import com.Revsoft.Wabbitemu.utils.IntentConstants;
import com.Revsoft.Wabbitemu.utils.OnBrowseItemSelected;
import com.Revsoft.Wabbitemu.utils.ViewUtils;
import com.google.android.gms.ads.AdView;

public class BrowseFragment extends Fragment {

	private final FileUtils mFileUtils = FileUtils.getInstance();
	private final OnBrowseItemSelected mBrowseCallback;

	private AsyncTask<Void, Void, ArrayAdapter<String>> mSearchTask;
	private ListView mListView;
	private AdView mAdView;

	public BrowseFragment() {
		this(null);
	}

	public BrowseFragment(@Nullable OnBrowseItemSelected browseCallback) {
		mBrowseCallback = browseCallback;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.browse, container, false);

		if (getArguments() != null) {
			final Bundle arguments = getArguments();
			final String extensionsRegex = arguments.getString(IntentConstants.EXTENSION_EXTRA_REGEX);

			mListView = ViewUtils.findViewById(view, R.id.browseView, ListView.class);
			mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final String filePath = (String) mListView.getItemAtPosition(position);
					if (mBrowseCallback != null) {
						mBrowseCallback.onBrowseItemSelected(filePath);
					}
				}
			});

			startSearch(view, extensionsRegex);

			mAdView = ViewUtils.findViewById(view, R.id.adView, AdView.class);
			AdUtils.loadAd(getResources(), mAdView);
		}

		return view;
	}

	private void startSearch(final View view, final String extensionsRegex) {
		mSearchTask = new AsyncTask<Void, Void, ArrayAdapter<String>>() {
			private Context mContext;
			private View mLoadingSpinner;

			@Override
			protected void onPreExecute() {
				mContext = getActivity();
				mLoadingSpinner = ViewUtils.findViewById(view, R.id.browseLoadingSpinner, View.class);
				mLoadingSpinner.setVisibility(View.VISIBLE);
			}

			@Override
			protected ArrayAdapter<String> doInBackground(final Void... params) {
				final List<String> files = mFileUtils.getValidFiles(extensionsRegex);
				return new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, files);
			}

			@Override
			protected void onPostExecute(final ArrayAdapter<String> adapter) {
				mLoadingSpinner.setVisibility(View.GONE);
				mListView.setAdapter(adapter);
				mSearchTask = null;
			}
		};

		if (extensionsRegex != null) {
			mSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mSearchTask != null) {
			mSearchTask.cancel(true);
		}

		if (mAdView != null) {
			AdUtils.destroyView(mAdView);
		}
	}
}