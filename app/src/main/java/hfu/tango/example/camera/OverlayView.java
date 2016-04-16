package hfu.tango.example.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.atap.tangoservice.TangoCameraIntrinsics;

import java.nio.FloatBuffer;

public class OverlayView extends View {
    private Paint paint;

    {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
    }

    private FloatBuffer buffer;
    private TangoCameraIntrinsics intrinsics;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverlayView(Context context) {
        super(context);
    }

    public void update(FloatBuffer buffer, TangoCameraIntrinsics intrinsics) {
        // should use a buffer and an own thread instead
        this.buffer = buffer;
        this.intrinsics = intrinsics;
    }

    private float[] translateBuffer() {
        if (intrinsics.calibrationType !=
                TangoCameraIntrinsics.TANGO_CALIBRATION_POLYNOMIAL_3_PARAMETERS) {
            // should throw some kind of tango exception
            Log.d("debug", "wrong calibration type: " + String.valueOf(intrinsics.calibrationType));
            return null;
        }
        FloatBuffer buffer = this.buffer; // need a copy of the object incase the buffer updates
        float[] drawBuffer = new float[buffer.capacity() * 2 / 3];

        double fx = intrinsics.fx;
        double fy = intrinsics.fy;
        double cx = intrinsics.cx;
        double cy = intrinsics.cy;

        double k1 = intrinsics.distortion[0];
        double k2 = intrinsics.distortion[1];
        double k3 = intrinsics.distortion[2];

        for (int i = 0, j = 0; i < buffer.capacity(); i += 3, j += 2) {
            float x = buffer.get(i);
            float y = buffer.get(i + 1);
            float z = buffer.get(i + 2);
            double ru = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) / Math.pow(z, 2);
            double rd = (ru + (k1 * Math.pow(ru, 3)) + (k2 * Math.pow(ru, 5)) + (k3 * Math.pow(ru, 7)));

            drawBuffer[j] = (float) ((x / z * fx * rd / ru) + cx) * 5;
            drawBuffer[j + 1] = (float) ((y / z * fy * rd / ru) + cy) * 5;
        }
        return drawBuffer;
    }

    private int zToColor(float z) {
        if (z * 50 >= 255)
            return Color.rgb(1, 0, 0);
        else
            return Color.rgb((int) (255 - z * 50), 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (buffer != null) {
            canvas.drawPoints(translateBuffer(), paint);
        }
    }
}
