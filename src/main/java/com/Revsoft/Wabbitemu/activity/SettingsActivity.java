package com.Revsoft.Wabbitemu.activity;

import android.app.Activity;
import android.os.Bundle;

import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.AdUtils;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.settings);
        setContentView(R.layout.settings);
        AdUtils.loadAd(this.findViewById(R.id.adView));
    }
}