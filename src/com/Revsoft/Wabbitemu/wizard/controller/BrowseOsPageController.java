package com.Revsoft.Wabbitemu.wizard.controller;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.Revsoft.Wabbitemu.CalcInterface;
import com.Revsoft.Wabbitemu.R;
import com.Revsoft.Wabbitemu.fragment.BrowseFragment;
import com.Revsoft.Wabbitemu.utils.IntentConstants;
import com.Revsoft.Wabbitemu.utils.OnBrowseItemSelected;
import com.Revsoft.Wabbitemu.wizard.WizardNavigationController;
import com.Revsoft.Wabbitemu.wizard.WizardPageController;
import com.Revsoft.Wabbitemu.wizard.data.FinishWizardData;
import com.Revsoft.Wabbitemu.wizard.view.BrowseOsPageView;

public class BrowseOsPageController implements WizardPageController {

	private final Context mContext;
	private final FragmentManager mFragmentManager;
	private final OnBrowseItemSelected mBrowseCallback = new OnBrowseItemSelected() {

		@Override
		public void onBrowseItemSelected(String fileName) {
			if (mNavController == null) {
				return;
			}

			mOsPath = fileName;
			mNavController.finishWizard();
		}
	};

	private int mCalcModel;
	private String mOsPath;
	private WizardNavigationController mNavController;

	public BrowseOsPageController(@NonNull BrowseOsPageView view,
			@NonNull FragmentManager fragmentManager)
	{
		mContext = view.getContext();
		mFragmentManager = fragmentManager;
	}

	@Override
	public void configureButtons(@NonNull final WizardNavigationController navController) {
		mNavController = navController;
		navController.hideNextButton();
	}

	@Override
	public boolean hasPreviousPage() {
		return true;
	}

	@Override
	public boolean hasNextPage() {
		return false;
	}

	@Override
	public boolean isFinalPage() {
		return true;
	}

	@Override
	public int getNextPage() {
		throw new IllegalStateException("No next page");
	}

	@Override
	public int getPreviousPage() {
		return R.id.os_page;
	}

	@Override
	public void onHiding() {
		// no-op
	}

	@Override
	public void onShowing(Object previousData) {
		mCalcModel = (int) (Integer) previousData;
		launchBrowseOs();
	}

	@Override
	public int getTitleId() {
		return R.string.osSelectionTitle;
	}

	@Override
	public Object getControllerData() {
		return new FinishWizardData(mCalcModel, mOsPath);
	}

	private void launchBrowseOs() {
		final String extensions;
		switch (mCalcModel) {
		case CalcInterface.TI_73:
			extensions = "\\.73u";
			break;
		case CalcInterface.TI_84PCSE:
			extensions = "\\.8cu";
			break;
		case CalcInterface.TI_83P:
		case CalcInterface.TI_83PSE:
		case CalcInterface.TI_84P:
		case CalcInterface.TI_84PSE:
			extensions = "\\.8xu";
			break;
		default:
			throw new IllegalStateException("Invalid calc model");
		}

		final Bundle setupBundle = new Bundle();
		setupBundle.putString(IntentConstants.EXTENSION_EXTRA_REGEX, extensions);
		setupBundle.putString(IntentConstants.BROWSE_DESCRIPTION_EXTRA_STRING,
				mContext.getResources().getString(R.string.browseOSDescription));

		final BrowseFragment fragInfo = new BrowseFragment(mBrowseCallback);
		fragInfo.setArguments(setupBundle);

		final FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.replace(R.id.browse_page, fragInfo);
		transaction.commit();
	}
}
