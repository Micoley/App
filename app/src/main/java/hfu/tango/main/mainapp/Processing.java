package hfu.tango.main.mainapp;


import android.util.Log;

public class Processing extends Thread {
    private final CameraRenderer mCameraRenderer;
    private TangoImageBuffer mImageBuffer;

    public Processing(CameraRenderer cameraRenderer) {
        mCameraRenderer = cameraRenderer;
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
            Log.d("HFU_DEBUG", String.valueOf(mImageBuffer.data[0]));
        }
    }
}
