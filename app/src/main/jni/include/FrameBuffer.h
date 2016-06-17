#ifndef APP_FRAMEBUFFER_H_
#define APP_FRAMEBUFFER_H_

#include <android/log.h>
#include <stdint.h>
#include <tango_client_api.h>
#include <opencv/cv.hpp>

/*
 * Definiere Makros für die Ausgaben von Fehler- und Debugmeldungen, sowie einen eigenen
 * Log-Tag, der hervorhebt, dass es sich um Ausgaben im nativen Code handelt
 */
#define LOG_TAG "HFU_DEBUG_NATIVE"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// Auflösung des Bildes, der Farbkamera, auf der Rückseite des Project Tango
#define WIDTH 1280
#define HEIGHT 720

/*
 * Die Größe des Framebuffers, welcher dem Callback des TangoService übergeben wird.
 * Da die Daten des Buffers im YV12-Format gespeichert sind, ergibt sich dank 4:2:0 Subsampling
 * eine Größe von: Auflösung * 1.5
 */
#define BUFFER_SIZE WIDTH * HEIGHT * 3 / 2

namespace hfu {

    // Hilfsfunktion um aus dem rohen YV12-FrameBuffer eine OpenCV-Mat im RGB-Format zu generieren
    cv::Mat createMatFromYV12(uint8_t *yv12DataBuffer);

    /*
     * Klasse die den rohen YV12-FrameBuffer der Farbkamera verwaltet.
     * Die Klasse stellt immer den aktuellsten Buffer zur Verfügung und kümmert sich um
     * sämtliche Initialisierungen und Interaktionen mit der Project Tango API
     */
    class FrameBuffer {
    private:
        FrameBuffer() { };
        static uint8_t *latestBuffer;
        static void onFrameAvailable(void *context, TangoCameraId id,
                                     const TangoImageBuffer *buffer);

    public:
        static void setup();
        static void destroy();
        static uint8_t *getLatestBuffer();

    };

}

#endif
