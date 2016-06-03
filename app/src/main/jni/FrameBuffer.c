#include <android/log.h>
#include <jni.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <tango_client_api.h>



#define LOG_TAG "HFU_DEBUG"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// YV12-Datenformat (4:2:0) => Auflösung * 1.5
#define BUFFER_SIZE 1280 * 720 * 1.5

static uint8_t *latest = NULL;
static int lock = 0;

void onFrameAvailable(void *context, TangoCameraId id, const TangoImageBuffer *buffer) {

    if(!lock)
        memcpy(latest, buffer->data, BUFFER_SIZE);
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraRenderer_getLatestBufferData(JNIEnv *env, jobject job,
                                                               jobject imageBuffer) {
    // Locke den buffer, falls neue Daten kommenen während die Alten kopiert werden
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

    // Hole den Zeiger auf das erste Element
    jshort *element = (*env)->GetShortArrayElements(env, data, 0);

    // Kopiere den aktuellen Buffer in das Java-Array
    int i;
    for(i = 0; i < BUFFER_SIZE; ++i) {
        element[i] = latest[i];
    }

    (*env)->ReleaseShortArrayElements(env, data, element, 0);
    lock = 0;
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraRenderer_setupFramebuffer(JNIEnv *env, jobject jobj) {

    int ret;

    // Registriere den onFrameAvailable-Callback
    ret = TangoService_connectOnFrameAvailable(TANGO_CAMERA_COLOR, NULL, onFrameAvailable);

    if(ret == TANGO_SUCCESS) {
        LOGD("native onFrameAvailable connected");
        latest = malloc(sizeof(uint8_t) * BUFFER_SIZE);
        LOGD("native frameBuffer created");
    } else {
        LOGE("Error connecting color frame %d", ret);
    }
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraRenderer_destroyFramebuffer(JNIEnv *env, jobject jobj) {
    free(latest);
    LOGD("native frameBuffer destroyed");
}
