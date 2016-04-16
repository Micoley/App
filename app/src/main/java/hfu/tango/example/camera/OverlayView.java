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
    private FloatBuffer buffer;
    private TangoCameraIntrinsics intrinsics;

    public OverlayView(Context context) {
        super(context);

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
    }

    public void update(FloatBuffer buffer, TangoCameraIntrinsics intrinsics) {
        this.buffer = buffer;
        this.intrinsics = intrinsics;
    }

    private float[] translateBuffer(FloatBuffer pBuffer) {
        Log.d("debug", "calibration type: " + String.valueOf(intrinsics.calibrationType));
        float[] drawBuffer = new float[pBuffer.capacity() * 2 / 3];

        float fx = (float) intrinsics.fx;
        float fy = (float) intrinsics.fy;
        float cx = (float) intrinsics.cx;
        float cy = (float) intrinsics.cy;

        float k1 = (float) intrinsics.distortion[0];
        float k2 = (float) intrinsics.distortion[1];
        float k3 = (float) intrinsics.distortion[2];

        for(int i = 0, j = 0; i < pBuffer.capacity(); i += 3, j += 2) {

            float x = pBuffer.get(i);
            float y = pBuffer.get(i + 1);
            float z = pBuffer.get(i + 2);
            float ru = (float) ((Math.sqrt(Math.pow(x,2) + Math.pow(y,2)) / Math.pow(z,2)));
            float rd = (float) (ru + k1 * Math.pow(ru,3) + k2 * Math.pow(ru,5) + k3 * Math.pow(ru,7));

            //float rd = (float) (1 / k1 * Math.atan(2 * ru * Math.tan(k1)/2));

            drawBuffer[j] = (x / z * fx * rd / ru + cx);
            drawBuffer[j + 1] = (x / z * fy * rd / ru + cy);

            Log.d("debug", "ru: " + String.valueOf(ru) + " rd: " + String.valueOf(rd) + " fx: "
                    + String.valueOf(fx) + " cx: " + String.valueOf(cx));

            Log.d("debug", "[METER] x: " + String.valueOf(pBuffer.get(i)) + " y: " +
                    String.valueOf(pBuffer.get(i + 1)) + " z: " + String.valueOf(pBuffer.get(i + 2)) +
            " ratio: " + String.valueOf(pBuffer.get(i) / pBuffer.get(i + 1)));

            Log.d("debug", "[KOORDINATEN] x: " + String.valueOf(drawBuffer[j]) +  " y: " + String.valueOf(drawBuffer[j + 1]) +
            " ratio: " + String.valueOf(drawBuffer[j] / drawBuffer[j + 1]));
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
            float[] points = translateBuffer(buffer);
            canvas.drawPoints(translateBuffer(buffer), paint);
        }
    }
}
