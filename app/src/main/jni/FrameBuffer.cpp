#include <cstdlib>
#include "include/FrameBuffer.h"

// Kommentare sind in FrameBuffer.h

namespace hfu {

    uint8_t *FrameBuffer::latestBuffer = NULL;

    cv::Mat createMatFromYV12(uint8_t *yv12DataBuffer) {
        cv::Mat mat = cv::Mat(HEIGHT * 3 / 2, WIDTH, CV_8UC1, yv12DataBuffer);
        cv::cvtColor(mat, mat, CV_YUV2RGB_NV21);
        return mat;
    }

    void FrameBuffer::onFrameAvailable(void *context, TangoCameraId id,
                                       const TangoImageBuffer *buffer) {
        memcpy(FrameBuffer::latestBuffer, buffer->data, BUFFER_SIZE);
    }

    void FrameBuffer::setup() {
        int ret = TangoService_connectOnFrameAvailable(TANGO_CAMERA_COLOR, NULL, onFrameAvailable);
        if (ret == TANGO_SUCCESS) {
            LOGD("onFrameAvailable connected");
            if (FrameBuffer::latestBuffer == NULL) {
                FrameBuffer::latestBuffer = new uint8_t[BUFFER_SIZE];
            } else {
                LOGE("frameBuffer is already initialized");
            }
        } else {
            LOGE("Error connecting color frame %d", ret);
        }
    }

    void FrameBuffer::destroy() {
        if (FrameBuffer::latestBuffer != NULL) {
            delete[] FrameBuffer::latestBuffer;
            FrameBuffer::latestBuffer = NULL;
            LOGD("frameBuffer destroyed");
        }
        LOGE("frameBuffer was not initialized");
    }

    uint8_t *FrameBuffer::getLatestBuffer() {
        if (FrameBuffer::latestBuffer != NULL) {
            return FrameBuffer::latestBuffer;
        } else {
            LOGE("framebuffer is null, call FrameBuffer::setup() first");
        }
        return NULL;
    }

}
