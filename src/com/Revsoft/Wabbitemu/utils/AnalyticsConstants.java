package com.Revsoft.Wabbitemu.utils;

public class AnalyticsConstants {
	public enum UserActionActivity {
		MAIN_ACTIVITY("WabbitemuActivity"),
		WIZARD_ACTIVITY("WizardActivity");

		private final String mActivity;

		private UserActionActivity(String activity) {
			mActivity = activity;
		}

		@Override
		public String toString() {
			return mActivity;
		}
	}

	public enum UserActionEvent {
		OPEN_MENU("OpenMenu"),
		SEND_FILE("SendFile"),
		ERROR("Error"),
		HELP("Help"),
		SCREENSHOT("Screenshot"),
		SEND_FILE_ERROR("SendFileError"),
		HAVE_OWN_ROM("HaveOwnROM"),
		BOOTFREE_ROM("BootFreeROM"),
		MENU_ITEM_SELECTED("MenuItemSelected");

		private final String mAction;

		private UserActionEvent(String action) {
			mAction = action;
		}

		@Override
		public String toString() {
			return mAction;
		}
	}
}
