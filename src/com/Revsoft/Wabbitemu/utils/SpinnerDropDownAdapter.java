package com.Revsoft.Wabbitemu.utils;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SpinnerDropDownAdapter extends BaseAdapter implements SpinnerAdapter {

	private List<String> mItems;
	private Context mContext;
	
	public SpinnerDropDownAdapter(Context context, List<String> items) {
		mContext = context;
		mItems = items;
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mItems.get(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final TextView text = new TextView(mContext);
        text.setTextColor(Color.WHITE);
        text.setText(mItems.get(position));
        text.setPadding(20, 20, 20, 20);
        return text;
	}

}
