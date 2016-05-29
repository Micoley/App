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

    private native void setup();

    private native void getLatestBufferData(TangoImageBuffer imageBuffer);

    public CameraRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);

        mImageBuffer = new TangoImageBuffer();

        setup();
    }

    @Override
    public void connectToTangoCamera(Tango tango, int cameraId) {
        super.connectToTangoCamera(tango, cameraId);
    }

    public TangoImageBuffer getLatestBufferData() {
        getLatestBufferData(mImageBuffer);
        return mImageBuffer;
    }

}
