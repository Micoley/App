package hfu.tango.main.mainapp;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.ArrayList;

public class TangoActivity extends Activity {

    /**
     * Wird benötigt um auf die Project Tango spezifischen Sensoren zuzugreifen
     */
    private Tango mTango;

    /**
     * Einstellungen und Informationen zu den einzelnen Kameras
     */
    private TangoCameraIntrinsics mCameraIntrinsics;

    /**
     * Anzeigen der Punktewolke
     */
    private OverlayRenderer mOverlayRenderer;

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
            switch (status) {
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
        mOverlayRenderer = (OverlayRenderer) findViewById(R.id.overlayRenderer);
        mTango = new Tango(this);
        mCameraIntrinsics = mTango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_DEPTH);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            connectTango();
        } catch (TangoOutOfDateException e) {
            e.printStackTrace();
        }

        /* sorgt dafür, dass die Verbindung zum OpenCV-Manager sowie der Aufruf des Callbacks
           asynchron stattfinden
         */
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            mTango.disconnect();
        } catch (TangoErrorException e) {
            e.printStackTrace();
        }

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            mTango.disconnect();
        } catch (TangoErrorException e) {
            e.printStackTrace();
        }

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    /**
     * Einstellen der Tango-Sensorik und Erzeugen des Listeners,
     * der auf Tango-Events reagiert
     */
    private void connectTango() {
        TangoConfig tangoConfig = mTango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
        mTango.connect(tangoConfig);

        final ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();

        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        Log.d("HFU_DEBUG", "Listener connected");
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {

            /**
             * Wird aufgerufen wenn neue Daten des Tiefensensors verfügbar sind
             * @param xyzIj Enthält die Entfernungsangaben in Meter in der Form: x1,y1,z1,x2,y2,z2...
             */
            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                Log.d("HFU_DEBUG", "onXyz..  ausgerufen");
                mOverlayRenderer.update(xyzIj.xyz, mCameraIntrinsics);
                mOverlayRenderer.postInvalidate();
            }

            /**
             * Wird aufgerufen wenn ein neues Kamerabild verfügbar ist
             * @param cameraId Die ID der jeweiligen Kamera
             */
            @Override
            public void onFrameAvailable(int cameraId) {
            }

            @Override
            public void onPoseAvailable(final TangoPoseData pose) {
            }

            @Override
            public void onTangoEvent(final TangoEvent event) {
            }
        });
    }


    /**
     * Klasse die auf Events des OpenCvCameraViews reagiert
     */
    private class OpenCvCameraListener implements CvCameraViewListener2 {

        @Override
        public void onCameraViewStarted(int width, int height) {
        }

        @Override
        public void onCameraViewStopped() {
        }

        /**
         * wird aufgerufen wenn ein neues Bild der Kamera verfügbar ist
         *
         * @param inputFrame das neue Frame
         * @return die Matrix-Darstellung des Kamerabildes in RGBA bzw. Graustufen
         */
        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            return inputFrame.rgba();
        }
    }
}
