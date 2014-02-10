#include "com_Revsoft_Wabbitemu_CalcInterface.h"
#include <jni.h>
#include <android/log.h>
#include "calc.h"
#include "keys.h"
#include "lcd.h"
#include "colorlcd.h"
#include "sendfile.h"
#include "exportvar.h"

static LPCALC lpCalc;
static int redPalette[256];
static int bluePalette[256];
static int greenPalette[256];
/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    CreateCalc
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_CreateCalc
		(JNIEnv *env, jclass classObj, jstring filePath) {
	const char *path = (*env)->GetStringUTFChars(env, filePath, JNI_FALSE);
	//Do not allow more than one calc currently
	if (lpCalc) {
		calc_slot_free(lpCalc);
	}

	lpCalc = calc_slot_new();
	lpCalc->model = -1;
	rom_load(lpCalc, path);
	lpCalc->running = TRUE;
	lpCalc->speed = 100;

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
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_SaveCalcState
		(JNIEnv *env, jclass classObj, jstring filePath) {
	const char *path = (*env)->GetStringUTFChars(env, filePath, JNI_FALSE);

	SAVESTATE_t* save = SaveSlot(lpCalc, "Wabbitemu", "Automatic save state");
	if (save != NULL) {
		WriteSave(path, save, 0);
		FreeSave(save);
	}
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    CreateRom
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT jint JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_CreateRom
	(JNIEnv *env, jclass classObj, jstring jOsPath, jstring jBootPath,
			jstring jRomPath, jint model) {
	const char *osPath = (*env)->GetStringUTFChars(env, jOsPath, JNI_FALSE);
	const char *bootPath = (*env)->GetStringUTFChars(env, jBootPath, JNI_FALSE);
	const char *romPath = (*env)->GetStringUTFChars(env, jRomPath, JNI_FALSE);

	lpCalc = calc_slot_new();
	calc_init_model(lpCalc, model, NULL);

	//slot stuff
	strcpy(lpCalc->rom_path, romPath);
	lpCalc->active = TRUE;
	lpCalc->model = model;
	lpCalc->cpu.pio.model = model;
	FILE *file = fopen(bootPath, "rb");
	if (file == NULL) {
		calc_slot_free(lpCalc);
		return -1;
	}
	writeboot(file, &lpCalc->mem_c, -1);
	fclose(file);
	remove(bootPath);
	//if you don't want to load an OS, fine...
	TIFILE_t *tifile = importvar(osPath, FALSE);
	if (tifile == NULL) {
		calc_slot_free(lpCalc);
		return -1;
	}
	forceload_os(&lpCalc->cpu, tifile);
	calc_erase_certificate(lpCalc->mem_c.flash,lpCalc->mem_c.flash_size);
	calc_reset(lpCalc);
	//write the output from file
	MFILE *romfile = ExportRom((char *) romPath, lpCalc);
	if (romfile != NULL) {
		mclose(romfile);
		calc_slot_free(lpCalc);
		return -1;
	}
	calc_slot_free(lpCalc);
	return 0;
}

JNIEXPORT jint JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_LoadFile
		(JNIEnv *env, jclass classObj, jstring filePath) {
	const char *path = (*env)->GetStringUTFChars(env, filePath, JNI_FALSE);
	TIFILE_t *tifile = importvar(path, TRUE);
	__android_log_print(ANDROID_LOG_INFO, "native", "%d %d %s", !tifile, !lpCalc, path);
	if (!tifile || !lpCalc) {
		return (jint) LERR_FILE;
	}
	return SendFile(lpCalc, path, SEND_CUR);
}

JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_ResetCalc
		(JNIEnv *env, jclass classObj) {
	if (lpCalc) {
		calc_reset(lpCalc);
	}
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    RunCalc
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_RunCalcs
  (JNIEnv *env, jclass classObj) {
	calc_run_all();
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    PauseCalc
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_PauseCalc
  (JNIEnv *env, jclass classObj) {
	lpCalc->running = FALSE;
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    UnpauseCalc
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_UnpauseCalc
  (JNIEnv *env, jclass classObj) {
	lpCalc->running = TRUE;
}

JNIEXPORT jint JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_GetModel
  (JNIEnv *env, jclass classObj) {
	if (!lpCalc) {
		return -1;
	}
	return lpCalc->model;
}

JNIEXPORT jint JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_Tstates
  (JNIEnv *env, jclass classObj) {
	return (int) lpCalc->timer_c.tstates;
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    SetSpeedCalc
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_SetSpeedCalc
  (JNIEnv *env, jclass classObj, jint speed) {
	lpCalc->speed = speed;
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    ClearKeys
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_ClearKeys
  (JNIEnv *env, jclass classObj) {
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
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_PressKey
  (JNIEnv *env, jclass classObj, jint group, jint bit) {
	keypad_press(&lpCalc->cpu, (int) group, (int) bit);
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    SetAutoTurnOn
 * Signature: (B)V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_SetAutoTurnOn
  (JNIEnv *env, jclass classObj, jboolean turnOn) {
	auto_turn_on = turnOn ? TRUE : FALSE;
}


/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    ReleaseKey
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_ReleaseKey
  (JNIEnv *env, jclass classObj, jint group, jint bit) {
	keypad_release(&lpCalc->cpu, (int) group, (int) bit);
}

void CopyWideGrayscale(JNIEnv *env, jintArray bytes, uint8_t *image) {
	int screen[LCD_HEIGHT * LCD_WIDTH];
	for (int i = 0, j = 0; i < LCD_HEIGHT * 128; i++) {
		uint8_t val = image[i];
		screen[j++] = bluePalette[val] + (greenPalette[val] << 8) +
				(redPalette[val] << 16) + 0xFF000000;
	}
	(*env)->SetIntArrayRegion(env, bytes, 0, 128*LCD_HEIGHT, (const jint *) screen);
}

void CopyGrayscale(JNIEnv *env, jintArray bytes, uint8_t *image) {
	int screen[LCD_HEIGHT * 96];
	for (int i = 0, j = 0; i < LCD_HEIGHT * 128;) {
		for (int k = 0; k < 96; i++, k++) {
			uint8_t val = image[i];
			screen[j++] = bluePalette[val] + (greenPalette[val] << 8) +
					(redPalette[val] << 16) + 0xFF000000;
		}
		i += 32;
	}
	(*env)->SetIntArrayRegion(env, bytes, 0, 96*LCD_HEIGHT, (const jint *) screen);
}

void CopyColor(JNIEnv *env, jintArray bytes, uint8_t *image) {
	int screen[COLOR_LCD_DISPLAY_SIZE];
	for (int i = 0, j = 0; i < COLOR_LCD_DISPLAY_SIZE; i+=3) {
		screen[j++] = image[i] + (image[i+1] << 8) + (image[i + 2] << 16) + 0xFF000000;
	}
	(*env)->SetIntArrayRegion(env, bytes, 0, COLOR_LCD_WIDTH * COLOR_LCD_HEIGHT, (const jint *) screen);
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    IsLCDActive
 * Signature: ()B
 */
JNIEXPORT jboolean JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_IsLCDActive
  (JNIEnv *env, jclass classObj) {
	if (lpCalc == NULL) {
		return JNI_FALSE;
	}

	LCDBase_t *lcd = lpCalc->cpu.pio.lcd;
	if (lcd == NULL) {
		return JNI_FALSE;
	}
	return lcd->active;
}

/*
 * Class:     com_Revsoft_Wabbitemu_CalcInterface
 * Method:    GetLCD
 * Signature: ()[B
 */
JNIEXPORT jint JNICALL Java_com_Revsoft_Wabbitemu_CalcInterface_GetLCD
  (JNIEnv *env, jclass classObj, jintArray bytes) {
	LCDBase_t *lcd = lpCalc->cpu.pio.lcd;
	if (bytes == NULL) {
		return lcd->active;
	}

	uint8_t *image;
	if (lcd->active) {
		image = lcd->image(lcd);
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
	} else {
		return FALSE;
	}
}
