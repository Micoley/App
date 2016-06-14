#include <android/log.h>
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <tango_client_api.h>
#include <opencv/cv.hpp>


#define LOG_TAG "HFU_DEBUG_NATIVE"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// YV12-Datenformat (4:2:0) => Auflösung * 1.5
#define BUFFER_SIZE 1280 * 720 * 1.5

extern "C" {

static uint8_t *latest = NULL;
static int lock = 0;
static int width = 1280;
static int height = 720;


void onFrameAvailable(void *context, TangoCameraId id, const TangoImageBuffer *buffer) {

    if (!lock)
        memcpy(latest, buffer->data, BUFFER_SIZE);
}

cv::Mat createMatFromYV12(uint8_t *yv12DataBuffer) {
    unsigned char* rgb_img = new unsigned char[width * height * 3];
    cv::Mat picNew = cv::Mat(height+height/2, width, CV_8UC1, (uchar *)yv12DataBuffer);
    cv::cvtColor(picNew, picNew, CV_YUV2RGB_NV21);
    cv::imwrite("/storage/emulated/0/Download/rgb_test.bmp", picNew);
    return picNew;
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraPreview_getLatestBufferData(JNIEnv *env, jobject job,
                                                               jlong matPtr) {
    // Locke den buffer, falls neue Daten kommenen während die Alten kopiert werden
    lock = 1;

    if (latest == NULL) {
        LOGE("framebuffer is null, call setupFramebuffer first");
        lock = 0;
        return;
    }

    cv::Mat *mat = (cv::Mat *) matPtr;


    cv::Mat bgr = createMatFromYV12(latest);

    *mat = bgr;

    lock = 0;
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraPreview_setupFramebuffer(JNIEnv *env, jobject jobj) {

    int ret;

    // Registriere den onFrameAvailable-Callback
    ret = TangoService_connectOnFrameAvailable(TANGO_CAMERA_COLOR, NULL, onFrameAvailable);

    if (ret == TANGO_SUCCESS) {
        LOGD("onFrameAvailable connected");
        latest = (uint8_t *) malloc(sizeof(uint8_t) * BUFFER_SIZE);
        LOGD("frameBuffer created");
    } else {
        LOGE("Error connecting color frame %d", ret);
    }
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraPreview_destroyFramebuffer(JNIEnv *env, jobject jobj) {
    free(latest);
    latest = NULL;
    LOGD("frameBuffer destroyed");
}

}