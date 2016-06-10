package hfu.tango.main.mainapp;


import android.content.Context;
import android.util.AttributeSet;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraPreview;

import org.opencv.core.Mat;


public class CameraRenderer extends TangoCameraPreview {
    private Mat latestMat;

    static {
        System.loadLibrary("native");
    }

    private native void setupFramebuffer();

    private native void destroyFramebuffer();

    private native void getLatestBufferData(long addr);

    public CameraRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
        latestMat = new Mat();
    }

    @Override
    public void connectToTangoCamera(Tango tango, int cameraId) {
        super.connectToTangoCamera(tango, cameraId);
        setupFramebuffer();
    }


    @Override
    public void disconnectFromTangoCamera() {
        super.disconnectFromTangoCamera();
        destroyFramebuffer();
    }

    public Mat getLatestBufferData() {
        getLatestBufferData(latestMat.getNativeObjAddr());
        return latestMat;
    }
}
