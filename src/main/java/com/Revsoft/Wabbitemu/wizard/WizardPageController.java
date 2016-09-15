package com.Revsoft.Wabbitemu.wizard;

import android.support.annotation.NonNull;


public interface WizardPageController {

	void configureButtons(@NonNull WizardNavigationController navController);

	boolean hasPreviousPage();

	boolean hasNextPage();

	boolean isFinalPage();

	int getNextPage();

	int getPreviousPage();

	void onHiding();

	void onShowing(Object previousData);

	int getTitleId();

	Object getControllerData();
}
