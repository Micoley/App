package hfu.tango.main.mainapp;


import android.content.Context;
import android.util.AttributeSet;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraPreview;


public class CameraRenderer extends TangoCameraPreview {
    private TangoImageBuffer mImageBuffer;

    static {
        System.loadLibrary("framebuffer");
    }

    private native void setupFramebuffer();

    private native void destroyFramebuffer();

    private native void getLatestBufferData(TangoImageBuffer imageBuffer);

    public CameraRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void connectToTangoCamera(Tango tango, int cameraId) {
        super.connectToTangoCamera(tango, cameraId);
        mImageBuffer = new TangoImageBuffer();
        setupFramebuffer();
    }


    @Override
    public void disconnectFromTangoCamera() {
        super.disconnectFromTangoCamera();
        mImageBuffer = null;
        destroyFramebuffer();
    }

    public TangoImageBuffer getLatestBufferData() {
        if(mImageBuffer != null)
            getLatestBufferData(mImageBuffer);
        return mImageBuffer;
    }
}
