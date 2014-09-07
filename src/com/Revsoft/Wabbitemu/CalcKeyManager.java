package com.Revsoft.Wabbitemu;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.view.KeyEvent;

import com.Revsoft.Wabbitemu.utils.KeyMapping;

public class CalcKeyManager {

	private static final KeyMapping[] KEY_MAPPINGS = {
		new KeyMapping(KeyEvent.KEYCODE_DPAD_DOWN, 0, 0),
		new KeyMapping(KeyEvent.KEYCODE_DPAD_LEFT, 0, 1),
		new KeyMapping(KeyEvent.KEYCODE_DPAD_RIGHT, 0, 2),
		new KeyMapping(KeyEvent.KEYCODE_DPAD_UP, 0, 3),
		new KeyMapping(KeyEvent.KEYCODE_ENTER, 1, 0),
		new KeyMapping(KeyEvent.KEYCODE_AT, 5, 0),
		new KeyMapping(KeyEvent.KEYCODE_A, 5, 6),
		new KeyMapping(KeyEvent.KEYCODE_B, 4, 6),
		new KeyMapping(KeyEvent.KEYCODE_C, 3, 6),
		new KeyMapping(KeyEvent.KEYCODE_D, 5, 5),
		new KeyMapping(KeyEvent.KEYCODE_E, 4, 5),
		new KeyMapping(KeyEvent.KEYCODE_F, 3, 5),
		new KeyMapping(KeyEvent.KEYCODE_G, 2, 5),
		new KeyMapping(KeyEvent.KEYCODE_H, 1, 5),
		new KeyMapping(KeyEvent.KEYCODE_I, 5, 4),
		new KeyMapping(KeyEvent.KEYCODE_J, 4, 4),
		new KeyMapping(KeyEvent.KEYCODE_K, 3, 4),
		new KeyMapping(KeyEvent.KEYCODE_L, 2, 4),
		new KeyMapping(KeyEvent.KEYCODE_M, 1, 4),
		new KeyMapping(KeyEvent.KEYCODE_N, 5, 3),
		new KeyMapping(KeyEvent.KEYCODE_O, 4, 3),
		new KeyMapping(KeyEvent.KEYCODE_P, 3, 3),
		new KeyMapping(KeyEvent.KEYCODE_Q, 2, 3),
		new KeyMapping(KeyEvent.KEYCODE_R, 1, 3),
		new KeyMapping(KeyEvent.KEYCODE_S, 5, 2),
		new KeyMapping(KeyEvent.KEYCODE_T, 4, 2),
		new KeyMapping(KeyEvent.KEYCODE_U, 3, 2),
		new KeyMapping(KeyEvent.KEYCODE_V, 2, 2),
		new KeyMapping(KeyEvent.KEYCODE_W, 1, 2),
		new KeyMapping(KeyEvent.KEYCODE_X, 5, 1),
		new KeyMapping(KeyEvent.KEYCODE_Y, 4, 1),
		new KeyMapping(KeyEvent.KEYCODE_Z, 3, 1),
		new KeyMapping(KeyEvent.KEYCODE_SPACE, 4, 0),
		new KeyMapping(KeyEvent.KEYCODE_0, 4, 0),
		new KeyMapping(KeyEvent.KEYCODE_1, 4, 1),
		new KeyMapping(KeyEvent.KEYCODE_2, 3, 1),
		new KeyMapping(KeyEvent.KEYCODE_3, 2, 1),
		new KeyMapping(KeyEvent.KEYCODE_4, 4, 2),
		new KeyMapping(KeyEvent.KEYCODE_5, 3, 2),
		new KeyMapping(KeyEvent.KEYCODE_6, 2, 2),
		new KeyMapping(KeyEvent.KEYCODE_7, 4, 3),
		new KeyMapping(KeyEvent.KEYCODE_8, 3, 3),
		new KeyMapping(KeyEvent.KEYCODE_9, 2, 3),
		new KeyMapping(KeyEvent.KEYCODE_PERIOD, 3, 0),
		new KeyMapping(KeyEvent.KEYCODE_COMMA, 4, 4),
		new KeyMapping(KeyEvent.KEYCODE_PLUS, 1, 1),
		new KeyMapping(KeyEvent.KEYCODE_MINUS, 1, 2),
		new KeyMapping(KeyEvent.KEYCODE_STAR, 2, 3),
		new KeyMapping(KeyEvent.KEYCODE_SLASH, 1, 4),
		new KeyMapping(KeyEvent.KEYCODE_LEFT_BRACKET, 3, 4),
		new KeyMapping(KeyEvent.KEYCODE_RIGHT_BRACKET, 2, 4),
		new KeyMapping(KeyEvent.KEYCODE_SHIFT_LEFT, 6, 5),
		new KeyMapping(KeyEvent.KEYCODE_SHIFT_RIGHT, 1, 6),
		new KeyMapping(KeyEvent.KEYCODE_ALT_LEFT, 5, 7),
		new KeyMapping(KeyEvent.KEYCODE_EQUALS, 4, 7),
		/*
		 * { VK_F1 , 6 , 4 }, { VK_F2 , 6 , 3 }, { VK_F3 , 6 , 2 }, { VK_F4
		 * , 6 , 1 }, { VK_F5 , 6 , 0 },
		 */
	};

	private static final int MIN_TSTATE_KEY = 600;
	private static final int MIN_TSTATE_ON_KEY = 2500;

	private static final int MAX_TSTATE_KEY = MIN_TSTATE_KEY * 1000000;
	private static final int MAX_TSTATE_ON_KEY = MAX_TSTATE_KEY * 1000000;
	private static final CalcKeyManager INSTANCE = new CalcKeyManager();

	public static CalcKeyManager getInstance() {
		return INSTANCE;
	}

	private final ArrayList<KeyMapping> mKeysDown = new ArrayList<KeyMapping>();
	private final long[][] mKeyTimePressed = new long[8][8];;

	public void doKeyDown(final int id, final int group, final int bit) {
		CalcInterface.PressKey(group, bit);
		mKeyTimePressed[group][bit] = CalcInterface.Tstates();
		mKeysDown.add(new KeyMapping(id, group, bit));
	}

	public void doKeyUp(final int id) {
		KeyMapping mapping = null;
		for (int i = 0; i < mKeysDown.size(); i++) {
			final KeyMapping possibleMapping = mKeysDown.get(i);
			if (possibleMapping != null && possibleMapping.getKey() == id) {
				mapping = mKeysDown.get(i);
			}
		}

		if (mapping == null) {
			return;
		}

		final int group = mapping.getGroup();
		final int bit = mapping.getBit();
		if (hasCalcProcessedKey(group, bit)) {
			final Timer repressTimer = new Timer();
			final TimerTask task = new TimerTask() {
				@Override
				public void run() {
					doKeyUp(id);
				}
			};
			repressTimer.schedule(task, 40);
		} else {
			CalcInterface.ReleaseKey(group, bit);
			mKeysDown.remove(mapping);
		}
	}

	private boolean hasCalcProcessedKey(final int group, final int bit) {
		if (group == CalcInterface.ON_KEY_GROUP && bit == CalcInterface.ON_KEY_BIT) {
			return ((mKeyTimePressed[group][bit] + MIN_TSTATE_ON_KEY) <= CalcInterface.Tstates())
					&& ((mKeyTimePressed[group][bit] + MAX_TSTATE_ON_KEY) <= CalcInterface.Tstates());
		} else {
			return ((mKeyTimePressed[group][bit] + MIN_TSTATE_KEY) <= CalcInterface.Tstates())
					&& ((mKeyTimePressed[group][bit] + MAX_TSTATE_KEY) <= CalcInterface.Tstates());
		}
	}

	public static KeyMapping getKeyMapping(final int keyCode) {
		KeyMapping foundMapping = null;
		for (final KeyMapping mapping : KEY_MAPPINGS) {
			if (mapping.getKey() == keyCode) {
				foundMapping = mapping;
				break;
			}
		}

		return foundMapping;
	}
}
