package com.Revsoft.Wabbitemu.wizard.controller;

import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.utils.SpinnerDropDownAdapter;
import com.Revsoft.Wabbitemu.wizard.WizardNavigationController;
import com.Revsoft.Wabbitemu.wizard.WizardPageController;
import com.Revsoft.Wabbitemu.wizard.data.FinishWizardData;
import com.Revsoft.Wabbitemu.wizard.view.OsPageView;

public class OsPageController implements WizardPageController {

	private final OsPageView mView;

	private int mCalcModel;

	public OsPageController(@NonNull OsPageView osPageView) {
		mView = osPageView;
	}

	@Override
	public void configureButtons(@NonNull WizardNavigationController navController) {
		if (isFinalPage()) {
			navController.setFinishButton();
		} else {
			navController.setNextButton();
		}
	}

	@Override
	public boolean hasPreviousPage() {
		return true;
	}

	@Override
	public boolean hasNextPage() {
		return !isFinalPage();
	}

	@Override
	public boolean isFinalPage() {
		return mView.getSelectedRadioId() == R.id.downloadOsRadio && mCalcModel != CalcInterface.TI_84PCSE;
	}

	@Override
	public int getNextPage() {
		if (isFinalPage()) {
			throw new IllegalStateException("No next page");
		}
		return mCalcModel == CalcInterface.TI_84PCSE ? R.id.os_download_page : R.id.browse_os_page;
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
		final List<String> items = new ArrayList<>();

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
			//items.add("4.2");
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
		return isFinalPage() ? new FinishWizardData(mCalcModel, null) : mCalcModel;
	}
}
