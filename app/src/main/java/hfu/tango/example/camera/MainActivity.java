package hfu.tango.example.camera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoCameraPreview;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.tangosupport.TangoSupport;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class MainActivity extends Activity {

    /*private final ArrayList<TangoCoordinateFramePair> framePairs =
            new ArrayList<>();*/

    private final UpdatableBlockingBuffer<FloatBuffer> pointBuffer =
            new UpdatableBlockingBuffer<>();

    /**
     * @attribute tango: wird zum initialisieren des Tablets benötigt.
     * @attribute cameraPreview: Ausgabe des Kamerabilds
     * @attribute cameraIntrinsics: Einstellungenen unf Informationen zu den Kameras
     * @attribute overlayView: Aus- /Weitergabe der Punktwolke
     */
    private Tango tango;
    private TangoCameraPreview cameraPreview;
    private TangoCameraIntrinsics cameraIntrinsics;
    private OverlayView overlayView;
    private TangoPoseData poseData;

    /**
     * onCreate wird beim Programmstart aufgerufen und initialisiert alle benötigten Variabeln
     * ruft außerdem Android onCreate mit den zuständen auf
     *
     * @param savedInstanceState: Vom System zur Verfügung gestellt, beinhaltet Zustände ??
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraPreview = (TangoCameraPreview) findViewById(R.id.cameraPreview);
        tango = new Tango(this);
        overlayView = (OverlayView) findViewById(R.id.overlayView);
        cameraIntrinsics = tango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_DEPTH);

    }

    /**
     * Beim wiederaufrufen der App, während sie noch nicht geschlossen wurde
     * muss das tablet wieder mit dem tango service verbunden werden
     */
    @Override
    protected void onResume() {
        super.onResume();
        cameraPreview.connectToTangoCamera(tango, TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
        try {
            connectTango();
        } catch (TangoOutOfDateException e) {
            e.printStackTrace();
        }
        Log.d("debug",String.valueOf(cameraIntrinsics.calibrationType));

        //pointParser = new PointCloudParser(pointBuffer, out, this);
        //pointParser.start();
    }

    /**
     * Beim pausieren (Home button) der App wird die app vom tango service getrennt, damit die
     * von anderen systemen genutzt werden können
     */
    @Override
    protected void onPause() {
        super.onPause();
        try {
            tango.disconnect();
        } catch (TangoErrorException e) {
            e.printStackTrace();
        }
        cameraPreview.disconnectFromTangoCamera();
        //pointParser.interrupt();
    }

    /**
     * Verbindet die app mit dem tango service, damit die app laufen kann und auf die hardware
     * zugregriffen werden kann.
     *
     * @atribute tangoConfig: beinhaltet die Einstellungen zur Nutzung des Tiefensensors und
     * Motiontracking
     */
    private void connectTango() {
        TangoConfig tangoConfig = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
        tango.connect(tangoConfig);

        /**
         * speichert frame_start_of_service ?????????????
         */
        final ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        /**
         * @attribute OnTangoUpdateListener(): Anonyme Innere Klasse um die Ausgaben bei
         * Verfügbarkeit auszuführen.
         */
        tango.connectListener(framePairs, new OnTangoUpdateListener() {

            /**
             * Gibt die Punktwolke aus
             * @param xyzIj: die Daten der Punktwolke
             */
            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                overlayView.update(xyzIj.xyz, tango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_DEPTH));
                overlayView.postInvalidate();
            }

            /**
             * Gibt das Kamerabild aus
             * @param cameraId die Kamera von welcher das Bild genommen werden soll
             */
            @Override
            public void onFrameAvailable(int cameraId) {
                if(cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR)
                    cameraPreview.onFrameAvailable();

            }


            @Override
            public void onPoseAvailable(final TangoPoseData pose) {}


            @Override
            public void onTangoEvent(final TangoEvent event) {}
        });
    }
}
