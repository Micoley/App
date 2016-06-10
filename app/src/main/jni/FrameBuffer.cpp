#include <android/log.h>
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <tango_client_api.h>
#include <opencv/cv.hpp>


#define LOG_TAG "HFU_DEBUG"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// YV12-Datenformat (4:2:0) => Auflösung * 1.5
#define BUFFER_SIZE 1280 * 720 * 1.5

extern "C" {

static uint8_t *latest = NULL;
static int lock = 0;

void onFrameAvailable(void *context, TangoCameraId id, const TangoImageBuffer *buffer) {

    if (!lock)
        memcpy(latest, buffer->data, BUFFER_SIZE);
}

cv::Mat createMatfromYV12(uint8_t *yv12DataBuffer) {
    cv::Mat picYV12 = cv::Mat(720 * 3 / 2, 1280, CV_8UC1, yv12DataBuffer);
    cv::imwrite("/storage/emulated/0/Download/yv12_test.bmp", picYV12);
    cv::Mat picBGR;
    cv::cvtColor(picYV12, picBGR, CV_YUV2BGR_YV12);
    return picBGR;
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraRenderer_getLatestBufferData(JNIEnv *env, jobject job,
                                                               jlong matPtr) {
    // Locke den buffer, falls neue Daten kommenen während die Alten kopiert werden
    lock = 1;

    if (latest == NULL) {
        LOGE("onFrameAvailable() was not called yet");
        return;
    }

    cv::Mat *mat = (cv::Mat *) matPtr;


    cv::Mat bgr = createMatfromYV12(latest);

    *mat = bgr;

    lock = 0;
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraRenderer_setupFramebuffer(JNIEnv *env, jobject jobj) {

    int ret;

    // Registriere den onFrameAvailable-Callback
    ret = TangoService_connectOnFrameAvailable(TANGO_CAMERA_COLOR, NULL, onFrameAvailable);

    if (ret == TANGO_SUCCESS) {
        LOGD("native onFrameAvailable connected");
        latest = (uint8_t *) malloc(sizeof(uint8_t) * BUFFER_SIZE);
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

}