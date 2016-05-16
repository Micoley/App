package hfu.tango.main.mainapp;


import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class TangoActivity extends Activity {
    /**
     * Die View auf der das Kamerabild angezeigt wird
     */
    private CameraBridgeViewBase mOpenCvCameraView;

    /**
     * Callback der aufgerufen wird wenn sich die App mit dem OpenCV-Manager verbunden hat
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch(status) {
                case BaseLoaderCallback.SUCCESS:
                    mOpenCvCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_tango);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.openCvCameraView);
        mOpenCvCameraView.setCvCameraViewListener(new OpenCvCameraListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* sorgt dafür, dass die Verbindung zum OpenCV-Manager sowie der Aufruf des Callbacks
           asynchron stattfinden
         */
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     * Klasse die auf Events des OpenCvCameraViews reagiert
     */
    private class OpenCvCameraListener implements CvCameraViewListener2 {

        @Override
        public void onCameraViewStarted(int width, int height) {}

        @Override
        public void onCameraViewStopped() {}

        /**
         * wird aufgerufen wenn ein neues Bild der Kamera verfügbar ist
         * @param inputFrame das neue Frame
         * @return die Matrix-Darstellung des Kamerabildes in RGBA bzw. Graustufen
         */
        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            return inputFrame.rgba();
        }
    }
}
