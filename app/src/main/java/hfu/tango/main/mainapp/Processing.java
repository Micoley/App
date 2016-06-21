package hfu.tango.main.mainapp;


import android.util.Log;

import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.tangosupport.TangoPointCloudManager;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class Processing extends Thread {
    private final CameraPreview mCameraRenderer;
    private Mat mImageBuffer;
    private OpenCvComponentInterface mObjectDetection;
    private FloatBuffer buffer;
    private TextToSpeech textToSpeech;
    private Set<String> warnings;
    private float warningDistance = 0.50f;
    private long timeOld = 0;
    private OverlayRenderer overlayRenderer;
    private TangoPointCloudManager pointCloudManager;
    private ColorCube colorCube;

    public Processing(CameraPreview cameraRenderer, TextToSpeech textToSpeech, OverlayRenderer overlayRenderer) {
        pointCloudManager = new TangoPointCloudManager();
        mCameraRenderer = cameraRenderer;
        this.overlayRenderer = overlayRenderer;
        mObjectDetection = new ObjectDetection();
        this.textToSpeech = textToSpeech;
        colorCube = new ColorCube();
        warnings = new TreeSet<String>() {
        };
    }

    public void updatePointCloudManager(TangoXyzIjData buffer) {
        pointCloudManager.updateXyzIj(buffer);
    }

    public void update(FloatBuffer buffer, List<Rectangle> rectangles, Mat mat) {
        this.buffer = buffer;
        // this.rectangles = rectangles;
        // mapPointsToObjects();
        //if (System.currentTimeMillis() > timeOld + 5000) {

        float x, y;
        for (int i = 0; i < buffer.capacity(); i += 3) {
            x = UtilitysHelper.getXasDisplayCoordinate(buffer.get(i), buffer.get(i + 2));
            y = UtilitysHelper.getYasDisplayCoordinate(buffer.get(i + 1), buffer.get(i + 2));
            checkDistanceInAreas(x, y, buffer.get(i + 2));
        }
        mapPointsToObjects(rectangles, mat.clone());
        overlayRenderer.updateRectangles(rectangles);
        overlayRenderer.postInvalidate();

        if (System.currentTimeMillis() > timeOld + 5000) {
            timeOld = System.currentTimeMillis();
            readResults(rectangles);
        }

    }

    private void readResults(List<Rectangle> rectangles) {
    /*    if (warnings.size() != 0) {

            for (String warning : warnings) {
                Log.d("output", String.valueOf(warning));
                textToSpeech.speak(warning, TextToSpeech.QUEUE_FLUSH, null);
            }
            warnings.clear();
        } else {*/
            Rectangle sr1 = new Rectangle();
            Rectangle sr2 = new Rectangle();
            sr1.setDistance(Double.MAX_VALUE);
            sr2.setDistance(Double.MAX_VALUE);
            for (Rectangle rectangle : rectangles) {
                //   Log.d("Distance", String.valueOf(rectangle.getDistance()));
                if (rectangle.getDistance() < sr1.getDistance()) {
                    sr1 = rectangle;
                } else if (rectangle.getDistance() < sr2.getDistance()) {
                    sr2 = rectangle;
                }
            }
            String ausgabe;
            if (sr1.getDistance() < Double.MAX_VALUE) {
                if (sr1.getDistance() < 1) {
                    ausgabe = "Rechteck" + sr1.getRelativePosition() + " in " + (Math.round(sr1.getDistance() * 100)) + " zentimetern Entfernung.";
                } else if (sr1.getDistance() == 1) {
                    ausgabe = "Rechteck" + sr1.getRelativePosition() + " in einem meter Entfernung.";
                } else {
                    ausgabe = "Rechteck" + sr1.getRelativePosition() + " in " + (Math.round(sr1.getDistance() * 100) / 100) + " metern Entfernung.";
                }
                ausgabe += "Farbe " + sr1.getColor() + ".";
                textToSpeech.speak(ausgabe, TextToSpeech.QUEUE_FLUSH, null);
                Log.d("Ausgabe", ausgabe);

            }
            if (sr2.getDistance() < Double.MAX_VALUE) {
                if (sr2.getDistance() < 1) {
                    ausgabe = "Rechteck" + sr2.getRelativePosition() + " in " + (Math.round(sr2.getDistance() * 100)) + " zentimetern Entfernung.";

                } else if (sr2.getDistance() == 1) {
                    ausgabe = "Rechteck" + sr2.getRelativePosition() + " in einem meter Entfernung.";
                } else {
                    ausgabe = "Rechteck" + sr2.getRelativePosition() + " in " + (Math.round(sr2.getDistance() * 100) / 100) + " metern Entfernung.";
                }
                ausgabe += "Farbe " + sr2.getColor() + ".";
                textToSpeech.speak(ausgabe, TextToSpeech.QUEUE_FLUSH, null);
                Log.d("Ausgabe", ausgabe);
            }

    //    }
        //   textToSpeech.speakWithDelay("" + rectangles.size() + " obstacles detected.", 0);
        /*for (Rectangle rectangle : rectangles) {
            textToSpeech.speakWithDelay("There is a rectangle, distance " + rectangle.getDistance() + "meters, position " + rectangle.getRelativePosition() + ".", 0);
        }*/
    }

    private void mapPointsToObjects(List<Rectangle> rectangles, Mat mat) {
        Rectangle rectangle;
        float x, y;
        for (int e = 0; e < rectangles.size(); e++) {
            rectangle = rectangles.get(e);
            Point points[] = rectangle.getPoints();
            if(points[3].x - points[0].x > 10 && points[0].y - points[2].y > 10 && points[3].x - points[0].x < UtilitysHelper.Display_Width - 10 &&  points[0].y - points[2].y < UtilitysHelper.Display_Heigth - 10) {
                for (int i = 0; i < buffer.capacity(); i += 3) {
                    x = UtilitysHelper.getXAsImageCoordinate(buffer.get(i), buffer.get(i + 2));
                    y = UtilitysHelper.getYAsImageCoordinate(buffer.get(i + 1), buffer.get(i + 2));
                    if (isPointInRectangleSimple(x, y, rectangles.get(e))) {
                        rectangle.getDistancePoints().add(x);
                        rectangle.getDistancePoints().add(y);
                        rectangle.getDistancePoints().add(buffer.get(i + 2));
                    }
                }
                setRelativePosition(rectangle);
                setMedianDistance(rectangle);
                setRectangleColor(rectangle, mat);
            }
            // updateRectangleCoordinates(rectangle);
        }
    }

    private void setRelativePosition(Rectangle rectangle) {
        Point[] points = rectangle.getPoints();
        float minDistanceX = Float.MAX_VALUE;
        float minDistanceY = Float.MAX_VALUE;
        float middleAreaWidth = 0.3f;
        float middleAreaHeight = 0.3f;
        for (int i = 0; i < points.length; i++) {
            float dx = (float) (UtilitysHelper.Image_Width / 2 - points[i].x);
            float dy = (float) (UtilitysHelper.Image_Heigth / 2 - points[i].y);
            if (Math.abs(dx) < Math.abs(minDistanceX)) {
                minDistanceX = Math.abs(dx);
            }
            if (Math.abs(dy) < Math.abs(minDistanceY)) {
                minDistanceY = Math.abs(dy);
            }
        }
        if (minDistanceX < UtilitysHelper.Image_Width * ((1 - middleAreaWidth) / 2)) {
            if (minDistanceY < UtilitysHelper.Image_Heigth * ((1 - middleAreaHeight) / 2)) {
                rectangle.setRelativePosition("links, unten");
            } else if (minDistanceY > UtilitysHelper.Image_Heigth * ((1 - middleAreaHeight) / 2) + middleAreaHeight) {
                rectangle.setRelativePosition("links, oben");
            } else {
                rectangle.setRelativePosition("links");
            }
        }
        if (minDistanceX > UtilitysHelper.Image_Width * ((1 - middleAreaWidth) / 2) + middleAreaWidth) {
            if (minDistanceY < UtilitysHelper.Image_Heigth * ((1 - middleAreaHeight) / 2)) {
                rectangle.setRelativePosition("rechts, unten");
            } else if (minDistanceY > UtilitysHelper.Image_Heigth * ((1 - middleAreaHeight) / 2) + middleAreaHeight) {
                rectangle.setRelativePosition("rechts, oben");
            } else {
                rectangle.setRelativePosition("rechts");
            }
        } else {
            if (minDistanceY < UtilitysHelper.Image_Heigth * ((1 - middleAreaHeight) / 2)) {
                rectangle.setRelativePosition("vorne, unten");
            } else if (minDistanceY > UtilitysHelper.Image_Heigth * ((1 - middleAreaHeight) / 2) + middleAreaHeight) {
                rectangle.setRelativePosition("vorne, oben");
            } else {
                rectangle.setRelativePosition("vorne");
            }
        }

    }

    private void checkDistanceInAreas(float x, float y, float z) {
        float middleAreaWidth = 0.3f;
        float middleAreaHeight = 0.3f;
        if (z < warningDistance) {
            if (x < UtilitysHelper.Display_Width * ((1 - middleAreaWidth) / 2)) {
                if (y < UtilitysHelper.Display_Heigth * ((1 - middleAreaHeight) / 2)) {
                    warnings.add("Kollisionsgefahr links oben");
                } else if (y > UtilitysHelper.Display_Heigth * ((1 - middleAreaHeight) / 2) + middleAreaHeight) {
                    warnings.add("Kollisionsgefahr links unten");
                } else {
                    warnings.add("Kollisionsgefahr links");
                }
            }
            if (x > UtilitysHelper.Display_Width * ((1 - middleAreaWidth) / 2) + middleAreaWidth) {
                if (y < UtilitysHelper.Display_Heigth * ((1 - middleAreaHeight) / 2)) {
                    warnings.add("Kollisionsgefahr rechts oben");
                } else if (y > UtilitysHelper.Display_Heigth * ((1 - middleAreaHeight) / 2) + middleAreaHeight) {
                    warnings.add("Kollisionsgefahr rechts unten");
                } else {
                    warnings.add("Kollisionsgefahr rechts");
                }
            } else {
                if (y < UtilitysHelper.Display_Heigth * ((1 - middleAreaHeight) / 2)) {
                    warnings.add("Kollisionsgefahr vorne oben");
                } else if (y > UtilitysHelper.Display_Heigth * ((1 - middleAreaHeight) / 2) + middleAreaHeight) {
                    warnings.add("Kollisionsgefahr unten");
                } else {
                    warnings.add("Kollisionsgefahr vorne");
                }
            }
        }
    }

    private void setMedianDistance(Rectangle rectangle) {
        ArrayList<Float> distancePoints;
        distancePoints = rectangle.getDistancePoints();
        float min = 0;
        float sum = 0;
        int count = 0;
        for (int i = 0; i < distancePoints.size(); i += 3) {
            if (distancePoints.get(i) < min) {
                min = distancePoints.get(i + 2);
            }
        }
        for (int i = 0; i < distancePoints.size(); i += 3) {
            if (distancePoints.get(i) > min * 0.9) {
                sum += distancePoints.get(i + 2);
                count++;
            } else {
                distancePoints.remove(i);
                distancePoints.remove(i + 1);
                distancePoints.remove(i + 2);
            }
        }
        rectangle.setDistance(sum / count);

    }


    private boolean isPointInRectangleSimple(double x, double y, Rectangle rectangle) {
        Point[] points = rectangle.getPoints();
        if (y < points[0].y && y > points[1].y) {
            if (x > points[0].x && x < points[3].x) {
                return true;
            }
        }
        return false;
    }

    public void setRectangleColor(Rectangle rectangle, Mat mat) {
        Point points[] = rectangle.points;
        int precision = 10;
        int colorCount[] = new int[3]; // r, g, b
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> count = new ArrayList<>();
        String color;
        //Map<String, Integer> colorCount = new TreeMap<>();
      //  Log.d("Color", String.valueOf(rectangle.points[0].y) + " " + String.valueOf(rectangle.points[2].y));
        for (int x = (int) points[0].x; x <= (int) rectangle.points[3].x - precision; x += precision) {

            for (int y = (int) points[2].y; y <= (int) rectangle.points[0].y - precision; y += precision) {

                double data[] = mat.get(y, x);
                int dataInt[] = new int[3];
                for(int i = 0; i < data.length; i++){
                    dataInt[i] = (int) data[i];
                }
                color = colorCube.getColor(dataInt);
                if(names.contains(color)){
                    int index = names.indexOf(color);
                    count.set(index,count.get(index) + 1);
                }else{
                    names.add(color);
                    count.add(1);
                }
              /*  if (data[0] > data[1] && data[0] > data[2]) {
                    colorCount[0] ++;
                }else if(data[1] > data[0] && data[1] > data[2]){
                    colorCount[1] ++;
                }else if(colorCount[2] > colorCount[0] && colorCount[2] > colorCount[1]){
                    colorCount[2] ++;
                }
                */
                colorCount[0] += data[0];
                colorCount[1] += data[1];
                colorCount[2] += data[2];
            }
        }
        int max = 0;
        int maxIndex = 0;
        for(int i = 0; i < count.size(); i++){
            if(count.get(i) > max){
                max = count.get(i);
                maxIndex = i;
            }
        }
        rectangle.setColor(names.get(maxIndex));
        /*if (colorCount[0] > colorCount[1] && colorCount[0] > colorCount[2]) {
            rectangle.setColor("rot");
        }else if(colorCount[1] > colorCount[0] && colorCount[1] > colorCount[2]){
            rectangle.setColor("grün");
        }else if(colorCount[2] > colorCount[0] && colorCount[2] > colorCount[1]){
            rectangle.setColor("blau");
        }/*


      /*  for(Integer value: colorCount.values()){
            if(value > max){
                max = value;
            }
        }
      */

    }

    private void updateRectangleCoordinates(Rectangle rectangle) {
        ArrayList<Float> distancePoints = rectangle.getDistancePoints();
        if (distancePoints.size() > 90) {
            Point leftTopCorner = new Point();
            leftTopCorner.x = 720;
            leftTopCorner.y = 1280;
            Point rightBottomCorner = new Point();
            for (int i = 0; i <= distancePoints.size() - 3; i += 3) {

                if (distancePoints.get(i) * distancePoints.get(i + 1) < leftTopCorner.x * leftTopCorner.y) {
                    leftTopCorner.x = distancePoints.get(i);
                    leftTopCorner.y = distancePoints.get(i + 1);
                }
                if (distancePoints.get(i) * distancePoints.get(i + 1) > rightBottomCorner.x * rightBottomCorner.y) {
                    rightBottomCorner.x = distancePoints.get(i);
                    rightBottomCorner.y = distancePoints.get(i + 1);
                }

            }
            Point[] rPoints = rectangle.getPoints();
            rPoints[0] = leftTopCorner;
            rPoints[1].x = leftTopCorner.x;
            rPoints[1].y = rightBottomCorner.y;
            rPoints[2] = rightBottomCorner;
            rPoints[3].x = rightBottomCorner.x;
            rPoints[3].y = leftTopCorner.y;
        }
    }

    private boolean isPointInRectangle(double x, double y, Rectangle rectangle) {
        double[] linarfunctions = rectangle.getLinearFunctions();
        if (y < linarfunctions[0] * x + linarfunctions[1]) {
            if (y > linarfunctions[2] * x + linarfunctions[3]) {
                if (x > y / linarfunctions[4] - linarfunctions[5]) {
                    if (x < y / linarfunctions[6] - linarfunctions[7]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("HFU_DEBUG", "processing aufgerufen");
            mImageBuffer = mCameraRenderer.getLatestBufferData();
            if (mImageBuffer != null) {
                List<Rectangle> objects = mObjectDetection.contours(mImageBuffer);
                if (OverlayRenderer.intrinsics != null) {
                    this.update(pointCloudManager.getLatestXyzIj().xyz, objects, mImageBuffer);
                }
                //List<Rectangle> objects = mObjectDetection.houghLinesP(mImageBuffer);
                Log.d("HFU_DEBUG", "Erkannte Objekte: " + String.valueOf(objects.size()));

                for (Rectangle object : objects) {
                    Log.d("HFU_DEBUG", String.valueOf(object));
                }
            }
        }
    }


}
