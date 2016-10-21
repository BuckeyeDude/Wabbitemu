package com.Revsoft.Wabbitemu.calc;

import java.nio.IntBuffer;

public interface CalcScreenUpdateCallback {

	void onUpdateScreen();

	IntBuffer getScreenBuffer();
}
