package hfu.tango.example.distance;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private final ArrayList<TangoCoordinateFramePair> framePairs =
            new ArrayList<>();

    private final UpdatableBlockingBuffer<FloatBuffer> pointBuffer =
            new UpdatableBlockingBuffer<>();

    private Tango tango;
    private PointCloudParser pointParser;
    private TextView out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tango = new Tango(this);
        out = (TextView) findViewById(R.id.textView);

        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            connectTango();
        } catch (TangoOutOfDateException e) {
            e.printStackTrace();
        }
        pointParser = new PointCloudParser(pointBuffer, out, this);
        pointParser.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            tango.disconnect();
        } catch (TangoErrorException e) {
            e.printStackTrace();
        }
        pointParser.interrupt();
    }

    private void connectTango() {
        TangoConfig tangoConfig = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        tangoConfig.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        tango.connect(tangoConfig);

        tango.connectListener(framePairs, new OnTangoUpdateListener() {

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                pointBuffer.update(xyzIj.xyz);
            }

            @Override
            public void onFrameAvailable(int i) {}

            @Override
            public void onPoseAvailable(final TangoPoseData pose) {}

            @Override
            public void onTangoEvent(final TangoEvent event) {}
        });
    }
}
