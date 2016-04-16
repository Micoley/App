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

    private Tango tango;
    private TangoCameraPreview cameraPreview;
    private TangoCameraIntrinsics cameraIntrinsics;
    private OverlayView overlayView;
    private TangoPoseData poseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraPreview = (TangoCameraPreview) findViewById(R.id.cameraPreview);
        tango = new Tango(this);
        overlayView = (OverlayView) findViewById(R.id.overlayView);
        cameraIntrinsics = tango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_DEPTH);

    }

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

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                overlayView.update(xyzIj.xyz, tango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_DEPTH));
                overlayView.postInvalidate();
            }

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
