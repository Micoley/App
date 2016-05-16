package hfu.tango.main.mainapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.atap.tangoservice.TangoCameraIntrinsics;

import java.nio.FloatBuffer;

public class OverlayRenderer extends View implements Runnable {
    private Paint paint;
    private FloatBuffer buffer;
    private TangoCameraIntrinsics intrinsics;

    {
        paint = new Paint();
        paint.setStrokeWidth(5);
    }

    public OverlayRenderer(Context context) {
        super(context);
    }

    public OverlayRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Die Overlayview wird mit den neuen Informationen des Tiefensensors geupdatet
     */
    public void update(FloatBuffer buffer, TangoCameraIntrinsics intrinsics) {
        Log.d("HFU_DEBUG", "update() aufgerufen");
        this.buffer = buffer;
        this.intrinsics = intrinsics;
    }

    /**
     * Berechnet aus den Tiefeninformationen die betroffenen Pixel auf dem Kamerabild
     * @param canvas Die Canvas auf der gezeichnet wird
     */
    private void drawPointBuffer(Canvas canvas) {
        float fx = (float) intrinsics.fx;
        float fy = (float) intrinsics.fy;
        float cx = (float) intrinsics.cx;
        float cy = (float) intrinsics.cy;
        float w = (float) intrinsics.width;
        float h = (float) intrinsics.height;

        for (int i = 0, j = 0; i < buffer.capacity(); i += 3, j += 2) {
            float x = buffer.get(i);
            float y = buffer.get(i + 1);
            float z = buffer.get(i + 2);

            paint.setColor(zToColor(z));
            canvas.drawPoint((x * fx + z * cx) / z * (this.getWidth() / w), (y * fy + z * cy) / z * (this.getHeight() / h), paint);
        }
    }

    /**
     * Bildet aus einem reellen Wert die Farbe ab
     * @param z Ein z-Wert des Tiefenbildes (0-5.0)
     * @return Eine Farbe von rgb(0,0,0) bis rgb(255,255,0)
     */

    private int zToColor(float z){
        int R = (int)(255 * z) / 5;
        int G = (int)(255 * (5 - z)) / 5;
        return  Color.rgb(R,G,0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d("HFU_DEBUG", "onDraw() aufgerufen");

        if (buffer != null) {
            drawPointBuffer(canvas);
        }
    }

    @Override
    public void run() {}
}
