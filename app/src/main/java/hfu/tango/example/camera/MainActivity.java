package hfu.tango.example.camera;

import android.app.Activity;
import android.os.Bundle;

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

import java.util.ArrayList;

public class MainActivity extends Activity {
    /**
     * Wird benötigt um auf die Project Tango spezifischen Sensoren zuzugreifen
     */
    private Tango tango;
    /**
     * Ausgabe des Kamerabilds
     */
    private TangoCameraPreview cameraPreview;
    /**
     * Einstellungen und Informationen zu den einzelnen Kameras
     */
    private TangoCameraIntrinsics cameraIntrinsics;
    /**
     * Anzeigen der Punktewolke
     */
    private OverlayView overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraPreview = (TangoCameraPreview) findViewById(R.id.cameraPreview);
        overlayView = (OverlayView) findViewById(R.id.overlayView);
        tango = new Tango(this);
        cameraIntrinsics = tango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_DEPTH);
    }

    /**
     * Wenn die App aktiv ist soll die cameraPreview wieder das Bild der Farbkamera anzeigen
     * und Tango verbunden werden
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
    }

    /**
     * Sobald die App pausiert wird sollen Tango und cameraPreview ausgeschaltet werden,
     * um Performance und Energie zu sparen
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
    }

    /**
     * Einstellen der Tango-Sensorik und Erzeugen des Listeners,
     * der auf Tango-Events reagiert
     */
    private void connectTango() {
        TangoConfig tangoConfig = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
        tango.connect(tangoConfig);

        final ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();

        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        tango.connectListener(framePairs, new OnTangoUpdateListener() {

            /**
             * Wird aufgerufen wenn neue Daten des Tiefensensors verfügbar sind
             * @param xyzIj Enthält die Entfernungsangaben in Meter in der Form: x1,y1,z1,x2,y2,z2...
             */
            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                overlayView.update(xyzIj.xyz, tango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_DEPTH));
                overlayView.postInvalidate();
            }

            /**
             * Wird aufgerufen wenn ein neues Kamerabild verfügbar ist
             * @param cameraId Die ID der jeweiligen Kamera
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
