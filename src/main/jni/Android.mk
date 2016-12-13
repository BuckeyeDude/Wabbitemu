# Stores the project directory. Useful when you compile several libraries to restore
# the build location.
LOCAL_PATH := $(call my-dir)
NDK_PROJECT_PATH := $(LOCAL_PATH)/..

# Defines common options (android release, here the 5th which is bundled with
# Android Java API 5, 6 and 7). Also defines some preprocessor variables for
# Open GL ES (for extensions beware that it may not be compatible with all
# devices), STLport, ...
WABBIT_CFLAGS := -Ofast -funroll-loops -fsigned-char -ffast-math -fno-signed-zeros 
	-mfloat-abi=hard -mfpu=neon -mthumb -msoft-float 
#WABBIT_CFLAGS := -std=gnu99 -fsigned-char -ffast-math -fno-signed-zeros -mfloat-abi=softfp -mfpu=neon

# This is a C++ optimization, nothing specific to Android. It avoids (when applicable)
# copying objects initialized inside a function when they are returned. They get
# initialized directly at their final location.
WABBIT_CPPFLAGS := -felide-constructors

TARGET_GLOBAL_CFLAGS := -flto

# Clear options from previous compiled projects if applicable.
include $(CLEAR_VARS)

# Name of the project, used in the Application.mk project file.
LOCAL_MODULE   := Wabbitemu
LOCAL_ARM_MODE := arm
APP_ABI := armeabi armeabi-v7a x86
ARCH := $(APP_ABI)
# C Compiler options. "-I" adds C/C++ include files from the specified directory.
LOCAL_CFLAGS    := $(WABBIT_CFLAGS) -DHIGH_SHADE_GIF -DVERBOSE -DZLIB_WINAPI -D_ANDROID  -std=gnu99 -ggdb
LOCAL_C_INCLUDES := $(LOCAL_PATH)/core $(LOCAL_PATH)/utilities	$(LOCAL_PATH)/interface \
					$(LOCAL_PATH)/hardware
					
LOCAL_LDLIBS := -llog -lz
# C++ compiler options
LOCAL_CPPFLAGS  := $(WABBIT_CPPFLAGS) -DHIGH_SHADE_GIF -DVERBOSE -D_ANDROID -std=gnu++11
LOCAL_CPP_FEATURES := exceptions 
LOCAL_SRC_FILES := core/core.c interface/calc.c interface/state.c hardware/81hw.c hardware/83hw.c \
		hardware/83phw.c hardware/83psehw.c hardware/86hw.c hardware/lcd.c hardware/link.c \
		hardware/colorlcd.c hardware/keys.c core/indexcb.c core/alu.c core/device.c core/control.c utilities/var.c \
		utilities/savestate.cpp utilities/sendfile.c utilities/label.c utilities/gif.cpp utilities/linksendvar.c \
		utilities/screenshothandle.c utilities/exportvar.c utilities/sound.c utilities/breakpoint.c utilities/zpipe.c \
		javaInterface.c

include $(BUILD_SHARED_LIBRARY)