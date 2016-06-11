package hfu.tango.main.mainapp;


import android.app.Activity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Locale;

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
    private CameraPreview mCameraPreview;

    private Processing mProcessing;

    private TextToSpeech mTTS;

    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_tango);

        mOverlayRenderer = (OverlayRenderer) findViewById(R.id.overlayRenderer);
        mTango = new Tango(this);
        mCameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        mCameraIntrinsics = mTango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_DEPTH);

        mTTS = new TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTTS.setLanguage(Locale.GERMAN);
                mTTS.speakWithDelay("rechteckiges Objekt in 3 Metern im linken Sichtbereich", 5000);
            }
        });

        mProcessing = new Processing(mCameraPreview, mTTS);
        mProcessing.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            connectTango();
        } catch (TangoOutOfDateException e) {
            e.printStackTrace();
        }

        mCameraPreview.connectToTangoCamera(mTango,
                TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            mTango.disconnect();
        } catch (TangoErrorException e) {
            e.printStackTrace();
        }
        mCameraPreview.disconnectFromTangoCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            mTango.disconnect();
        } catch (TangoErrorException e) {
            e.printStackTrace();
        }
        mCameraPreview.disconnectFromTangoCamera();
    }

    /**
     * Einstellen der Tango-Sensorik und Erzeugen des Listeners,
     * der auf Tango-Events reagiert
     */
    private void connectTango() {
        TangoConfig tangoConfig = mTango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
        mTango.connect(tangoConfig);

        final ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();

        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        mTango.connectListener(framePairs, new OnTangoUpdateListener() {

            /**
             * Wird aufgerufen wenn neue Daten des Tiefensensors verfügbar sind
             * @param xyzIj Enthält die Entfernungsangaben in Meter in der Form: x1,y1,z1,x2,y2,z2...
             */
            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                mOverlayRenderer.update(xyzIj.xyz, mCameraIntrinsics);
                mOverlayRenderer.postInvalidate();
            }

            /**
             * Wird aufgerufen wenn ein neues Kamerabild verfügbar ist
             * @param cameraId Die ID der jeweiligen Kamera
             */
            @Override
            public void onFrameAvailable(int cameraId) {
                if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
                    mCameraPreview.onFrameAvailable();

                }
            }

            @Override
            public void onPoseAvailable(final TangoPoseData pose) {
            }

            @Override
            public void onTangoEvent(final TangoEvent event) {
            }
        });
    }
}
