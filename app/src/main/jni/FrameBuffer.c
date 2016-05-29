#include <android/log.h>
#include <jni.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <tango_client_api.h>

#define LOG_TAG "HFU_DEBUG"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

const TangoImageBuffer *latest = NULL;

void onFrameAvailable(void *context, TangoCameraId id, const TangoImageBuffer *buffer) {
    latest = buffer;
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraRenderer_getLatestBufferData(JNIEnv *env, jobject job,
                                                               jobject imageBuffer) {
    if (latest == NULL) {
        LOGE("onFrameAvailable() was not called yet");
        return;
    }
    TangoImageBuffer buffer = *latest;
    // Hole die Referenz auf die Java TangoImageBuffer Klasse
    jclass cls = (*env)->GetObjectClass(env, imageBuffer);
    // Hole die ID des data Attributs ([S = short array)
    jfieldID fid = (*env)->GetFieldID(env, cls, "data", "[S");
    if (fid == NULL) {
        LOGE("Error getting the Latest Buffer data");
        return;
    }

    jshortArray data = (*env)->GetObjectField(env, imageBuffer, fid);
    if (data == NULL) {
        LOGE("Error getting the Latest Buffer data");
        return;
    }

    // get the first element
    jshort *element = (*env)->GetShortArrayElements(env, data, 0);

    /*
    int size = 1280 * 720;

    int i;
    for(i = 0; i < 100; ++i) {
        element[i] = buffer.data[i];
    }
     */

    (*env)->ReleaseShortArrayElements(env, data, element, 0);
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraRenderer_setup(JNIEnv *env, jobject jobj) {

    int ret;

    ret = TangoService_connectOnFrameAvailable(TANGO_CAMERA_COLOR, NULL, onFrameAvailable);

    if (ret != TANGO_SUCCESS) {
        LOGE("Error connecting color frame %d", ret);
        //return ret;
    }

}