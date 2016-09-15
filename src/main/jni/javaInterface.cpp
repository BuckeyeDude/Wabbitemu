#include "com_Revsoft_Wabbitemu_CalcInterface.h"
#include "calc.h"
#include "linksendvar.h"
#include "sendfile.h"
#include "exportvar.h"

void checkThread();

static LPCALC lpCalc;
static int redPalette[256];
static int bluePalette[256];
static int greenPalette[256];
char cache_dir[MAX_PATH];
static long staticThreadId = -1;

void load_settings(LPCALC lpCalc, LPVOID lParam) {
	lpCalc->running = TRUE;
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    SetCacheDir
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_Initialize
		(JNIEnv *env, jclass classObj, jstring filePath) {
	checkThread();
	const char *path = env->GetStringUTFChars(filePath, JNI_FALSE);
	strcpy(cache_dir, path);
	lpCalc = calc_slot_new();
	lpCalc->model = INVALID_MODEL;
	calc_register_event(lpCalc, ROM_LOAD_EVENT, &load_settings, NULL);

	for (int i = 0; i < 256; i++) {
		redPalette[i] = (0x9E * (256 - i)) / 255;
		bluePalette[i] = (0x88 * (256 - i)) / 255;
		greenPalette[i] = (0xAB * (256 - i)) / 255;
	}
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    SaveCalcState
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT jboolean JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_SaveCalcState
		(JNIEnv *env, jclass classObj, jstring filePath) {
	checkThread();
	const char *path = env->GetStringUTFChars(filePath, JNI_FALSE);

    SAVESTATE_t *save;
    try {
        save = SaveSlot(lpCalc, "Wabbitemu", "Automatic save state");
    } catch (std::exception &e) {
        _tprintf_s(_T("Exception loading save state: %s"), e.what());
        return JNI_FALSE;
    }
	BOOL wasSuccessful = FALSE;
	if (save != NULL) {
		wasSuccessful = WriteSave(path, save, ZLIB_CMP);
		FreeSave(save);
	}

	return (jboolean) (save != NULL && wasSuccessful? JNI_TRUE : JNI_FALSE);
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    CreateRom
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT jint JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_CreateRom
	(JNIEnv *env, jclass classObj, jstring jOsPath, jstring jBootPath,
			jstring jRomPath, jint model) {
	checkThread();
	const char *osPath = env->GetStringUTFChars(jOsPath, JNI_FALSE);
	const char *bootPath = env->GetStringUTFChars(jBootPath, JNI_FALSE);
	const char *romPath = env->GetStringUTFChars(jRomPath, JNI_FALSE);

	// Do not allow more than one calc currently
	if (lpCalc) {
		calc_slot_free(lpCalc);
	}

	lpCalc = calc_slot_new();
	calc_init_model(lpCalc, model, NULL);

	// slot stuff
	strcpy(lpCalc->rom_path, romPath);
	lpCalc->active = TRUE;
	lpCalc->model = (CalcModel) model;
	lpCalc->cpu.pio.model = model;
	FILE *file = fopen(bootPath, "rb");
	if (file == NULL) {
		return -1;
	}
	writeboot(file, &lpCalc->mem_c, -1);
	fclose(file);
	remove(bootPath);
	TIFILE_t *tifile = importvar(osPath, FALSE);
	if (tifile == NULL) {
		return -2;
	}
	int link_error = forceload_os(&lpCalc->cpu, tifile);
	if (link_error != LERR_SUCCESS) {
		return link_error;
	}

	calc_erase_certificate(lpCalc->mem_c.flash,lpCalc->mem_c.flash_size);
	calc_reset(lpCalc);
	//write the output from file
	MFILE *romfile = ExportRom((char *) romPath, lpCalc);
	if (romfile == NULL) {
		return -3;
	}

	calc_register_event(lpCalc, ROM_LOAD_EVENT, &load_settings, NULL);
	mclose(romfile);
	return 0;
}

JNIEXPORT jint JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_LoadFile
		(JNIEnv *env, jclass classObj, jstring filePath) {
	checkThread();
	const char *path = env->GetStringUTFChars(filePath, JNI_FALSE);
	TIFILE_t *tifile = importvar(path, TRUE);
	if (!tifile || !lpCalc) {
		return (jint) LERR_FILE;
	}

	int result = SendFile(lpCalc, path, SEND_CUR);
	return result;
}

JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_ResetCalc
		(JNIEnv *env, jclass classObj) {
	checkThread();
	if (!lpCalc) {
		return;
	}

	lpCalc->fake_running = TRUE;
	calc_reset(lpCalc);
	lpCalc->fake_running = FALSE;
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    RunCalc
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_RunCalcs
  (JNIEnv *env, jclass classObj) {
	checkThread();
	calc_run_all();
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    PauseCalc
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_PauseCalc
  (JNIEnv *env, jclass classObj) {
	checkThread();
	if (!lpCalc) {
		return;
	}

	lpCalc->running = FALSE;
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    UnpauseCalc
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_UnpauseCalc
  (JNIEnv *env, jclass classObj) {
	checkThread();
	if (!lpCalc) {
		return;
	}

	lpCalc->running = TRUE;
}

JNIEXPORT jint JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_GetModel
  (JNIEnv *env, jclass classObj) {
	if (!lpCalc) {
		return -1;
	}

	return lpCalc->model;
}

JNIEXPORT jlong JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_Tstates
  (JNIEnv *env, jclass classObj) {
	if (!lpCalc) {
		return -1;
	}
	return lpCalc->timer_c.tstates;
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    SetSpeedCalc
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_SetSpeedCalc
  (JNIEnv *env, jclass classObj, jint speed) {
	checkThread();
	lpCalc->speed = speed;
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    ClearKeys
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_ClearKeys
  (JNIEnv *env, jclass classObj) {
	checkThread();
	for (int i = 0; i < 7; i++) {
		for (int j = 0; j < 8; j++) {

		}
	}
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    PressKey
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_PressKey
  (JNIEnv *env, jclass classObj, jint group, jint bit) {
	checkThread();
	if (!lpCalc) {
		return;
	}

	keypad_press(&lpCalc->cpu, (int) group, (int) bit);
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    SetAutoTurnOn
 * Signature: (B)V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_SetAutoTurnOn
  (JNIEnv *env, jclass classObj, jboolean turnOn) {
	checkThread();
	auto_turn_on = turnOn ? TRUE : FALSE;
}


/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    ReleaseKey
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_ReleaseKey
  (JNIEnv *env, jclass classObj, jint group, jint bit) {
	checkThread();
	if (!lpCalc) {
		return;
	}

	keypad_release(&lpCalc->cpu, (int) group, (int) bit);
}

void CopyWideGrayscale(JNIEnv *env, int *screen, uint8_t *image) {
	for (int i = 0, j = 0; i < LCD_HEIGHT * 128; i++) {
		uint8_t val = image[i];
		screen[j++] = redPalette[val] + (greenPalette[val] << 8) +
				(bluePalette[val] << 16) + 0xFF000000;
	}
}

void CopyGrayscale(JNIEnv *env, int *screen, uint8_t *image) {
	for (int i = 0, j = 0; i < LCD_HEIGHT * 128;) {
		for (int k = 0; k < 96; i++, k++) {
			uint8_t val = image[i];
			screen[j++] = redPalette[val] + (greenPalette[val] << 8) +
					(bluePalette[val] << 16) + 0xFF000000;
		}
		i += 32;
	}
}

void CopyColor(JNIEnv *env, int *screen, uint8_t *image) {
	for (int i = 0, j = 0; i < COLOR_LCD_DISPLAY_SIZE; i+=3) {
		screen[j++] = image[i + 2] + (image[i+1] << 8) + (image[i] << 16) + 0xFF000000;
	}
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    GetLCD
 * Signature: ()[B
 */
JNIEXPORT jint JNICALL Java_com_Revsoft_Wabbitemu_calc_CalcInterface_GetLCD
  (JNIEnv *env, jclass classObj, jobject intBuffer) {
	if (!lpCalc) {
		return FALSE;
	}

	LCDBase_t *lcd = lpCalc->cpu.pio.lcd;
	assert(intBuffer != NULL);

	int *bytes = (int *) env->GetDirectBufferAddress(intBuffer);
	if (bytes == NULL) {
		return FALSE;
	}

	uint8_t *image;
	if (lcd != NULL && lcd->active) {
		image = lcd->image(lcd);
	} else {
		size_t size = (lpCalc->model == TI_84PCSE ? COLOR_LCD_DISPLAY_SIZE : GRAY_DISPLAY_SIZE);
		image = (uint8_t *) malloc(size);
		memset(image, 0, size);
	}

	switch (lpCalc->model) {
		case TI_85:
		case TI_86:
			CopyWideGrayscale(env, bytes, image);
			break;
		case TI_84PCSE:
			CopyColor(env, bytes, image);
			break;
		default:
			CopyGrayscale(env, bytes, image);
			break;
	}
	free(image);
	return TRUE;
}

void checkThread() {
	int threadId = pthread_self();
	if (staticThreadId == -1) {
		staticThreadId = threadId;
	} else {
		assert(threadId == staticThreadId);
	}
}