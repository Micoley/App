#include <jni.h>
#include <opencv/cv.hpp>
#include "include/FrameBuffer.h"

/*
 * JNI (Java Native Interface) stellt die Verbindung zwischen Java und nativem Code her.
 * Die Funktionsnamen müssen mit Java_ anfangen und den vollständigen Package-Pfad inklusive
 * Klassen- und Methodenname enthalten
 */

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Es wird die Speicheradresse, der in Java erzeugten Mat übergeben und darunter die neu erzeugte
 * Mat mit den aktuellen RGB-Daten des Kamerabildes abgespeichert
 */
JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraPreview_getLatestBufferData(JNIEnv *env, jobject job,
                                                              jlong matPtr) {
    cv::Mat *mat = (cv::Mat *) matPtr;
    uint8_t *buffer = hfu::FrameBuffer::getLatestBuffer();
    if(buffer != NULL) {
        cv::Mat rgb = hfu::createMatFromYV12(buffer);
        *mat = rgb;
    } else {
        LOGE("buffer is not available (app is most likely onPause)");
    }
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraPreview_setupFramebuffer(JNIEnv *env, jobject jobj) {
    hfu::FrameBuffer::setup();
}

JNIEXPORT void JNICALL
Java_hfu_tango_main_mainapp_CameraPreview_destroyFramebuffer(JNIEnv *env, jobject jobj) {
    hfu::FrameBuffer::destroy();
}

#ifdef __cplusplus
}
#endif