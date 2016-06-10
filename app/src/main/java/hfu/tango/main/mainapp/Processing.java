package hfu.tango.main.mainapp;


import android.os.Environment;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

public class Processing extends Thread {
    private final CameraRenderer mCameraRenderer;
    private Mat mat;
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
            Log.d("HFU_DEBUG", "Aufruf von getLatestBufferData()");
            mat = mCameraRenderer.getLatestBufferData();

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String filename = "test.png";
            File file = new File(path, filename);

            filename = file.toString();
            Imgcodecs.imwrite(filename, mat);

            List<Rectangle> objects = mObjectDetection.contours(mat);
            for(Rectangle object: objects) {
                Log.d("HFU_DEBUG", object.toString());
            }
        }
    }
}