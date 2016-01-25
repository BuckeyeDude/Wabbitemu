package com.Revsoft.Wabbitemu.wizard.controller;

import android.support.annotation.NonNull;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.SpinnerDropDownAdapter;
import com.Revsoft.Wabbitemu.wizard.WizardNavigationController;
import com.Revsoft.Wabbitemu.wizard.WizardPageController;
import com.Revsoft.Wabbitemu.wizard.view.OsPageView;

import java.util.ArrayList;
import java.util.List;

public class OsUseAutheticationPageController implements WizardPageController {

	private final OsPageView mView;

	private int mCalcModel;

	public OsUseAutheticationPageController(@NonNull OsPageView osPageView) {
		mView = osPageView;
	}

	@Override
	public void configureButtons(@NonNull WizardNavigationController navController) {
		navController.setNextButton();
	}

	@Override
	public boolean hasPreviousPage() {
		return true;
	}

	@Override
	public boolean hasNextPage() {
		return true;
	}

	@Override
	public boolean isFinalPage() {
		return false;
	}

	@Override
	public int getNextPage() {
		return mView.getSelectedRadioId() == R.id.downloadOsRadio ?
				R.id.os_download_page :
				R.id.browse_os_page;
	}

	@Override
	public int getPreviousPage() {
		return R.id.model_page;
	}

	@Override
	public void onHiding() {
		// no-op
	}

	@Override
	public void onShowing(Object previousData) {
		final List<String> items = new ArrayList<String>();

		mCalcModel = (int) (Integer) previousData;
		switch (mCalcModel) {
		case CalcInterface.TI_73:
			items.add("1.91");
			break;
		case CalcInterface.TI_83P:
		case CalcInterface.TI_83PSE:
			items.add("1.19");
			break;
		case CalcInterface.TI_84P:
		case CalcInterface.TI_84PSE:
			items.add("2.55 MP");
			items.add("2.43");
			break;
		case CalcInterface.TI_84PCSE:
			items.add("4.2");
			items.add("4.0");
			break;
		default:
			throw new IllegalStateException("Invalid calc model");
		}

		final Spinner spinner = mView.getSpinner();
		final SpinnerAdapter adapter = new SpinnerDropDownAdapter(spinner.getContext(), items);
		spinner.setAdapter(adapter);
		spinner.setSelection(0);
	}

	@Override
	public int getTitleId() {
		return R.string.osSelectionTitle;
	}

	@Override
	public Object getControllerData() {
		return mCalcModel;
	}
}
