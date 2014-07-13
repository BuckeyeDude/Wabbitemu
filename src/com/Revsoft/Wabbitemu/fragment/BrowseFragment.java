package com.Revsoft.Wabbitemu.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.Revsoft.Wabbitemu.utils.AdUtils;
import com.google.android.gms.ads.AdView;
import com.Revsoft.Wabbitemu.R;

public class BrowseFragment extends Fragment {

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState)
	{
		final View view  = inflater.inflate(R.layout.browse, container, false);

		if (getArguments() != null) {
			final BrowseInnerFragment fragment = new BrowseInnerFragment();
			fragment.setArguments(getArguments());
			getChildFragmentManager().beginTransaction().replace(R.id.browse_frame, fragment).commit();

			final AdView adView = (AdView) view.findViewById(R.id.adView4);
			AdUtils.LoadAd(adView);
		}

		return view;
	}
}