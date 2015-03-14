package com.Revsoft.Wabbitemu.wizard;


public interface WizardPageController {

	// TODO: kill in favor of a navigation controller
	void initialize(SetupWizardController wizardController);

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
