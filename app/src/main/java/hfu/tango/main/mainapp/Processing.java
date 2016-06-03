package hfu.tango.main.mainapp;


import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class Processing extends Thread {
    private final CameraRenderer mCameraRenderer;
    private TangoImageBuffer mImageBuffer;
    private OpenCvComponentInterface mObjectDetection;

    public Processing(CameraRenderer cameraRenderer) {
        mCameraRenderer = cameraRenderer;
        mObjectDetection = new ObjectDetection();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mImageBuffer = mCameraRenderer.getLatestBufferData();
            if(mImageBuffer != null) {
                // Umwandlung & Aufruf von objectDetection
                Mat m = yv12ToRgb(mImageBuffer.data);
                List<Rectangle> objects = mObjectDetection.contours(m);
            }
        }
    }

    public Mat yv12ToRgb(short[] yv12Buffer) {
        byte[] test = new byte[1280 * 720 * 3 / 2];
        for(int i = 0; i < 1280 * 720 * 3 / 2; ++i) {
            test[i] = (byte) yv12Buffer[i];
        }

        Mat yv12 = new Mat(720 + 720 / 2, 1280, CvType.CV_8UC1);
        yv12.put(0, 0, test);

        // opencv uses bgr instead of rgb
        Mat bgr = new Mat(720, 1280, CvType.CV_8UC3);
        Imgproc.cvtColor(yv12, bgr, Imgproc.COLOR_YUV2BGR_YV12);
        return bgr;
    }
}