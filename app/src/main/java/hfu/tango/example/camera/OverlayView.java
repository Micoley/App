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
        float w = (float) intrinsics.width;
        float h = (float) intrinsics.height;
        float k1 = (float) intrinsics.distortion[0];
        float k2 = (float) intrinsics.distortion[1];
        float k3 = (float) intrinsics.distortion[2];

        for (int i = 0, j = 0; i < pBuffer.capacity(); i += 3, j += 2) {

            float x = pBuffer.get(i);
            float y = pBuffer.get(i + 1);
            float z = pBuffer.get(i + 2);
            //drawBuffer[j] = (x * fx + z * cx) / z;
            //drawBuffer[j + 1] = (y * fy + z * cy) / z;
            drawBuffer[j] = (x * fx + z * cx) / z * (this.getWidth() / w);
            drawBuffer[j + 1] = (y * fy + z * cy) / z * (this.getHeight() / h);
        }
        return drawBuffer;
    }

    private int zToColor(float z) {
        if (z * 50 >= 255)
            return Color.rgb(1, 0, 0);
        else
            return Color.rgb((int) (255 - z * 50), 0, 0);
    }
    private int zToColorFull(float z){
        int R = (int)(255 * z) / 5;
        int G = (int)(255 * (5 - z)) / 5;
        return  Color.rgb(R,G,0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (buffer != null) {
           // float[] points = translateBuffer(buffer);
            //canvas.drawPoints(translateBuffer(buffer), paint);
            drawPointperPoint(canvas, buffer);
        }
    }


    private void drawPointperPoint(Canvas canvas, FloatBuffer pBuffer) {
        float fx = (float) intrinsics.fx;
        float fy = (float) intrinsics.fy;
        float cx = (float) intrinsics.cx;
        float cy = (float) intrinsics.cy;
        float w = (float) intrinsics.width;
        float h = (float) intrinsics.height;
        float k1 = (float) intrinsics.distortion[0];
        float k2 = (float) intrinsics.distortion[1];
        float k3 = (float) intrinsics.distortion[2];

        for (int i = 0, j = 0; i < pBuffer.capacity(); i += 3, j += 2) {

            float x = pBuffer.get(i);
            float y = pBuffer.get(i + 1);
            float z = pBuffer.get(i + 2);
            //drawBuffer[j] = (x * fx + z * cx) / z;
            //drawBuffer[j + 1] = (y * fy + z * cy) / z;
            paint.setColor(zToColorFull(z));
            canvas.drawPoint((x * fx + z * cx) / z * (this.getWidth() / w), (y * fy + z * cy) / z * (this.getHeight() / h), paint);
        }
    }
}
