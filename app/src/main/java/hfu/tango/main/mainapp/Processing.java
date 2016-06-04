package hfu.tango.main.mainapp;


import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class Processing extends Thread {
    private final CameraRenderer mCameraRenderer;
    private TangoImageBuffer mImageBuffer;
    private Mat mImageMat;
    private OpenCvComponentInterface mObjectDetection;

    public Processing(CameraRenderer cameraRenderer) {
        mCameraRenderer = cameraRenderer;
        mObjectDetection = new ObjectDetection();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
/*
            mImageMat = mCameraRenderer.getImageMat();
            List<Rectangle> objects = mObjectDetection.contours(mImageMat.getNativeObjAddr());
            for(Rectangle rectangle: objects) {
                Log.d("HFU_DEBUG", rectangle.toString());
                */
        }
    }

    public Mat yv12ToRgb(byte[] yv12Buffer) {
        Mat yv12 = new Mat(720 + 720 / 2, 1280, CvType.CV_8UC1);
        yv12.put(0, 0, yv12Buffer);

        // opencv uses bgr instead of rgb
        Mat bgr = new Mat(720, 1280, CvType.CV_8UC3);
        Imgproc.cvtColor(yv12, bgr, Imgproc.COLOR_YUV2BGR_YV12);
        return bgr;
    }
}