# prebuilt library
# save the path to current directory
LOCAL_PATH := $(call my-dir)
# clear most of the LOCAL_ variables (except LOCAL_PATH)
include $(CLEAR_VARS)
LOCAL_MODULE := prebuiltlib
LOCAL_SRC_FILES := ../jniLibs/libtango_client_api.so
#LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

# set the module name
LOCAL_MODULE := framebuffer

# declare source files
LOCAL_SRC_FILES := FrameBuffer.cpp

# add logging library
LOCAL_LDLIBS := -llog

# declare local shared libraries
LOCAL_SHARED_LIBRARIES := prebuiltlib

# tie everything together
include $(BUILD_SHARED_LIBRARY)
