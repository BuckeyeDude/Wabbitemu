package com.Revsoft.Wabbitemu.wizard.data;


import com.Revsoft.Wabbitemu.calc.CalcModel;

import javax.annotation.Nonnull;

public class OSDownloadData {
    public final CalcModel mCalcModel;
    public final String mOsUrl;

    public OSDownloadData(@Nonnull CalcModel calcModel, @Nonnull String osUrl) {
        mCalcModel = calcModel;
        mOsUrl = osUrl;
    }
}
