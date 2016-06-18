package hfu.tango.main.mainapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.atap.tangoservice.TangoCameraIntrinsics;

import org.opencv.core.Point;

import java.nio.FloatBuffer;
<<<<<<< HEAD
import java.util.Map;
import java.util.TreeMap;
=======
import java.util.List;
>>>>>>> a62041b94aecd3de90b3356c24c3f3bed4502c89

public class OverlayRenderer extends View implements Runnable {
    private ColorMapper colorMapper = new ColorMapper(0, 5, 100);
    private Paint paint;
    private FloatBuffer buffer;
    public static TangoCameraIntrinsics intrinsics;
    private Paint paintR;
    private List<Rectangle> rectangles;


    {
        paint = new Paint();
        paint.setStrokeWidth(5);
        paintR = new Paint();
        paintR.setStrokeWidth(2);
        paintR.setStyle(Paint.Style.STROKE);
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
    public synchronized void update(FloatBuffer buffer, TangoCameraIntrinsics intrinsics) {
        this.buffer = buffer;
        this.intrinsics = intrinsics;
    }
    /**
     * Die Overlayview wird mit erkannten Objekten von opencv geupdatet
     */
    public void updateRectangles(List<Rectangle> rectangles) {
        this.rectangles = rectangles;
    }

    /**
     * Berechnet aus den Tiefeninformationen die betroffenen Pixel auf dem Kamerabild
     * @param canvas Die Canvas auf der gezeichnet wird
     */
    private synchronized void drawPointBuffer(Canvas canvas) {
        float fx = (float) intrinsics.fx;
        float fy = (float) intrinsics.fy;
        float cx = (float) intrinsics.cx;
        float cy = (float) intrinsics.cy;
        float w  = (float) intrinsics.width;
        float h  = (float) intrinsics.height;

        for (int i = 0, j = 0; i <= buffer.limit() - 3; i += 3, j += 2) {
            float x = buffer.get(i);
            float y = buffer.get(i + 1);
            float z = buffer.get(i + 2);

            paint.setColor(zToColorSimple(z));
            canvas.drawPoint((x * fx + z * cx) / z * (this.getWidth() / w), (y * fy + z * cy) / z * (this.getHeight() / h), paint);
        }
    }
    /**
     * Zeichnet die Rechtecke von ObjectDetection
     * @param canvas Die Canvas auf der gezeichnet wird
     */
    private void drawRectangles(Canvas canvas){
        float ratioX = (float)this.getWidth() / (float)1280;
        float ratioY = (float)this.getHeight() / (float)720;
        for(Rectangle rectangle : rectangles){
            Point points[] = rectangle.getPoints();


            Path path = new Path();
            path.moveTo((float)points[0].x * ratioX, (float)points[0].y * ratioY);
            path.lineTo((float) points[1].x * ratioX, (float) points[1].y * ratioY);
            path.lineTo((float) points[2].x * ratioX, (float) points[2].y * ratioY);
            path.lineTo((float) points[3].x * ratioX, (float) points[3].y * ratioY);
            path.lineTo((float)points[0].x * ratioX, (float)points[0].y * ratioY);
            //paint.setColor(zToColor((float)rectangle.getDistance()));
            paintR.setColor(zToColorSimple(4));
            canvas.drawPath(path, paintR);
        }
    }


    /**
     * Bildet aus einem reellen Wert die Farbe ab
     * @param z Ein z-Wert des Tiefenbildes (0-5.0)
     * @return Eine Farbe von rgb(0,0,0) bis rgb(255,255,0)
     */

    private int zToColor(float z){
        return colorMapper.mapToColor(z);
    }
    private int zToColorSimple(float z){
        int r = (int) (z * 255);
        int g = 255 - ((int) (z * 255));
        return  Color.rgb(r,g,0);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);



        if (buffer != null) {
            drawPointBuffer(canvas);
        }
        if(rectangles != null){
            drawRectangles(canvas);
        }
    }

    @Override
    public void run() {}
}
