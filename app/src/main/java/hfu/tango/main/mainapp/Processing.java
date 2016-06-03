package hfu.tango.main.mainapp;


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

            }
        }
    }
}