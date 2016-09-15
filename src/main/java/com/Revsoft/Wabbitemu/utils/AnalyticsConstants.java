package com.Revsoft.Wabbitemu.utils;

import android.app.Activity;

import com.Revsoft.Wabbitemu.activity.WabbitemuActivity;
import com.Revsoft.Wabbitemu.activity.WizardActivity;

public class AnalyticsConstants {
	public enum UserActionActivity {
		MAIN_ACTIVITY(WabbitemuActivity.class),
		WIZARD_ACTIVITY(WizardActivity.class);

		private final String mActivity;

		private UserActionActivity(Class<? extends Activity> activity) {
			mActivity = activity.getSimpleName();
		}

		@Override
		public String toString() {
			return mActivity;
		}
	}

	public enum UserActionEvent {
		OPEN_MENU("OpenMenu"),
		SEND_FILE("SendFile"),
		ERROR("ERROR"),
		HELP("Help"),
		SCREENSHOT("Screenshot"),
		SEND_FILE_ERROR("SendFileError"),
		HAVE_OWN_ROM("HaveOwnROM"),
		BOOTFREE_ROM("BootFreeROM"),
		MENU_ITEM_SELECTED("MenuItemSelected"),
		INVALID_KEYMAP_PIXEL("InvalidKeymapPixel");

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
