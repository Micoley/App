#include <android/log.h>
#include <jni.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <tango_client_api.h>

#define LOG_TAG "HFU_DEBUG"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define BUFFER_SIZE 1280 * 720 * 1.5

uint8_t *latest = NULL;
int lock = 0;

void onFrameAvailable(void *context, TangoCameraId id, const TangoImageBuffer *buffer) {
    // kopieren in eigenen Speicherplatz (der andere wird warsch. freigegeben)
    if(!lock)
        memcpy(latest, buffer->data, BUFFER_SIZE);
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraRenderer_getLatestBufferData(JNIEnv *env, jobject job,
                                                               jobject imageBuffer) {
    lock = 1;
    if (latest == NULL) {
        LOGE("onFrameAvailable() was not called yet");
        return;
    }
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

    int i;
    for(i = 0; i < BUFFER_SIZE; ++i) {
        element[i] = latest[i];
    }

    (*env)->ReleaseShortArrayElements(env, data, element, 0);
    lock = 0;
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraRenderer_setup(JNIEnv *env, jobject jobj) {

    int ret;

    ret = TangoService_connectOnFrameAvailable(TANGO_CAMERA_COLOR, NULL, onFrameAvailable);

    if (ret != TANGO_SUCCESS) {
        LOGE("Error connecting color frame %d", ret);
        //return ret;
    }

    latest = malloc(sizeof(uint8_t) * BUFFER_SIZE);

}