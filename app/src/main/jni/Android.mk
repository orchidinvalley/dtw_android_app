LOCAL_PATH := $(call  my-dir)
include $(CLEAR_VARS)
LOCAL_LDLIBS += -llog
LOCAL_MODULE := use_ndk_build
LOCAL_SRC_FILES := use_ndk_build.c mfcc.c fft.c
include $(BUILD_SHARED_LIBRARY)