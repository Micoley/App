package hfu.tango.main.mainapp;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class TangoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Wird benötigt um auf die Project Tango spezifischen Sensoren zuzugreifen
     */
    private Tango tango;

    /**
     * Einstellungen und Informationen zu den einzelnen Kameras
     */
    private TangoCameraIntrinsics cameraIntrinsics;

    /**
     * Anzeigen der Punktewolke
     */
    private OverlayRenderer overlayRenderer;

    /**
     * Die View auf der das Kamerabild angezeigt wird
     */
    private CameraPreview cameraPreview;

    /**
     * Klasse, die die Bild- und Tiefendaten verarbeitet
     */
    private Processing processing;

    private TextToSpeech tts;

    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.drawer);


        overlayRenderer = (OverlayRenderer) findViewById(R.id.overlayRenderer);
        tango = new Tango(this);
        cameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        cameraIntrinsics = tango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_DEPTH);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlayRenderer.togglePointCloud();

            }
        });
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlayRenderer.toggleRectangles();

            }
        });
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        tts = new TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.GERMAN);
                processing = new Processing(cameraPreview, tts, overlayRenderer);
                processing.start();
            }
        });

        try {
            connectTango();
        } catch (TangoOutOfDateException e) {
            e.printStackTrace();
        }

        cameraPreview.connectToTangoCamera(tango,
                TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
    }

    @Override
    protected void onPause() {
        super.onPause();

        tts.shutdown();
        tts = null;

        processing.interrupt();
        processing = null;

        try {
            tango.disconnect();
        } catch (TangoErrorException e) {
            e.printStackTrace();
        }
        cameraPreview.disconnectFromTangoCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
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
                overlayRenderer.update(xyzIj.xyz, cameraIntrinsics);
                overlayRenderer.postInvalidate();
                processing.updatePointCloudManager(xyzIj);
            }

            /**
             * Wird aufgerufen wenn ein neues Kamerabild verfügbar ist
             * @param cameraId Die ID der jeweiligen Kamera
             */
            @Override
            public void onFrameAvailable(int cameraId) {
                if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
                    cameraPreview.onFrameAvailable();

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Log.d("Input", "Input");
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
