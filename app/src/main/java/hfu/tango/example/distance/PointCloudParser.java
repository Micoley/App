package hfu.tango.example.distance;

import android.app.Activity;
import android.widget.TextView;

import java.nio.FloatBuffer;

public class PointCloudParser extends Thread {
    private UpdatableBlockingBuffer<FloatBuffer> pointBuffer;
    private Activity activity;
    private TextView out;

    public PointCloudParser(UpdatableBlockingBuffer<FloatBuffer> pointBuffer, TextView out, Activity activity) {
        this.pointBuffer = pointBuffer;
        this.activity = activity;
        this.out = out;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            FloatBuffer points = pointBuffer.get();
            float minZ = Float.MAX_VALUE;
            for (int i = 0; i < points.capacity(); i += 3) {
                float f = points.get(i + 2);
                if (f < minZ) {
                    minZ = f;
                }
            }
            activity.runOnUiThread(new TextViewUpdater(minZ));
        }
    }

    private class TextViewUpdater implements Runnable {
        private final float distance;

        public TextViewUpdater(float distance) {
            this.distance = distance;
        }

        @Override
        public void run() {
            float f = ((int) (distance * 1000)) / 10f;
            String output = String.valueOf(f) + " cm";
            out.setText(output);
        }
    }
}
