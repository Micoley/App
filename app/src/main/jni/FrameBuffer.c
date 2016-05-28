#include <android/log.h>
#include <jni.h>
#include "tango_client_api.h"

#define LOG_TAG "HFU_DEBUG"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_TangoActivity_printHello(JNIEnv *env, jobject jobj) {
    LOGD("Testing123");
}