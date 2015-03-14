package com.Revsoft.Wabbitemu.wizard.controller;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.wizard.SetupWizardController;
import com.Revsoft.Wabbitemu.wizard.WizardPageController;
import com.Revsoft.Wabbitemu.wizard.view.ModelPageView;

public class CalcModelPageController implements WizardPageController {

	private final ModelPageView mView;

	public CalcModelPageController(ModelPageView view) {
		mView = view;
	}

	@Override
	public void initialize(@NonNull final SetupWizardController wizardController) {
		mView.getNextButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				wizardController.moveNextPage();
			}
		});

		mView.getBackButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				wizardController.movePreviousPage();
			}
		});
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
		return R.id.os_page;
	}

	@Override
	public int getPreviousPage() {
		return R.id.landing_page;
	}

	@Override
	public void onHiding() {

	}

	@Override
	public void onShowing(Object previousData) {

	}

	@Override
	public int getTitleId() {
		return R.string.calculatorTypeTitle;
	}

	@Override
	public Object getControllerData() {
		switch (mView.getSelectedRadioId()) {
		case R.id.ti73Radio:
			return CalcInterface.TI_73;
		case R.id.ti83pRadio:
			return CalcInterface.TI_83P;
		case R.id.ti83pseRadio:
			return CalcInterface.TI_83PSE;
		case R.id.ti84pRadio:
			return CalcInterface.TI_84P;
		case R.id.ti84pseRadio:
			return CalcInterface.TI_84PSE;
		case R.id.ti84pcseRadio:
			return CalcInterface.TI_84PCSE;
		default:
			throw new IllegalStateException("Invalid radio id");
		}
	}
}
