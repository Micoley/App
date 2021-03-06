package hfu.tango.main.mainapp;


import android.util.Log;

import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.tangosupport.TangoPointCloudManager;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Klasse zur Verarbeitung von Informationen aus der Bildverarbeitung mit den Informatinen vom
 * Tiefensensor
 */
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
    private boolean showWarnings;

    public Processing(CameraPreview cameraRenderer, TextToSpeech textToSpeech, OverlayRenderer overlayRenderer) {
        pointCloudManager = new TangoPointCloudManager();
        mCameraRenderer = cameraRenderer;
        this.overlayRenderer = overlayRenderer;
        mObjectDetection = new ObjectDetection();
        this.textToSpeech = textToSpeech;
        colorCube = new ColorCube();
        showWarnings = false;
        warnings = new TreeSet<String>() {
        };
    }

    /**
     * Punktewolke an Processing uebergeben
     *
     * @param buffer Punktewolke von Tiefensensor
     */
    public void updatePointCloudManager(TangoXyzIjData buffer) {
        pointCloudManager.updateXyzIj(buffer);
    }

    /**
     * Aktualisiert Processing
     *
     * @param buffer     Punktewolke von Tiefensensor
     * @param rectangles Liste mit Rechtecken
     * @param mat        Mat Objekt von OpenCV
     */

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

        if (System.currentTimeMillis() > timeOld + 5000) {
            timeOld = System.currentTimeMillis();
            readResults(rectangles, mat);
        }

    }

    /**
     * Gibt Warnungen und das naechste Objekt aus
     *
     * @param rectangles Liste mit Rechtecken
     */

    private void readResults(List<Rectangle> rectangles, Mat mat) {
        if (warnings.size() != 0 && showWarnings) {

            for (String warning : warnings) {
                Log.d("output", String.valueOf(warning));
                textToSpeech.speak(warning, TextToSpeech.QUEUE_FLUSH, null);
            }
            warnings.clear();
        } else {
            Rectangle sr = new Rectangle();
            sr.setDistance(Double.MAX_VALUE);
            for (Rectangle rectangle : rectangles) {
                //   Log.d("Distance", String.valueOf(rectangle.getDistance()));
                if (rectangle.getDistance() < sr.getDistance()) {
                    sr = rectangle;
                }
            }
            String ausgabe;
            if (sr.getDistance() < Double.MAX_VALUE) {
                double distance = (Math.round(sr.getDistance() * 100));
                setRectangleColor(sr, mat);
                if (distance < 100) {
                    ausgabe = "Rechteck " + sr.getRelativePosition() + " in " + (int) distance + " zentimetern Entfernung.";
                } else if (distance == 100) {
                    ausgabe = "Rechteck " + sr.getRelativePosition() + " in einem meter Entfernung.";
                } else {
                    ausgabe = "Rechteck " + sr.getRelativePosition() + " in " + (distance / 100) + " metern Entfernung.";
                }
                ausgabe += "Farbe " + sr.getColor() + ".";
                textToSpeech.speak(ausgabe, TextToSpeech.QUEUE_FLUSH, null);
                Log.d("Ausgabe", ausgabe);

            }

            //    }
            //   textToSpeech.speakWithDelay("" + rectangles.size() + " obstacles detected.", 0);
        /*for (Rectangle rectangle : rectangles) {
            textToSpeech.speakWithDelay("There is a rectangle, distance " + rectangle.getDistance() + "meters, position " + rectangle.getRelativePosition() + ".", 0);
        }*/
        }
    }

    /**
     * Weist den Rechtecken die dazugehoerigen Punkte aus der Punktwolke zu
     *
     * @param rectangles Liste mit Rechtecken
     * @param mat        Mat Objekt von OpenCV
     */

    private void mapPointsToObjects(List<Rectangle> rectangles, Mat mat) {
        Rectangle rectangle;
        float x, y;
        for (int e = 0; e < rectangles.size(); e++) {
            rectangle = rectangles.get(e);
            Point points[] = rectangle.getPoints();
            if (points[3].x - points[0].x > 10 && points[0].y - points[2].y > 10 && points[3].x - points[0].x < UtilitysHelper.Display_Width - 10 && points[0].y - points[2].y < UtilitysHelper.Display_Heigth - 10) {
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
                //  setRectangleColor(rectangle, mat);
            }
            // updateRectangleCoordinates(rectangle);
        }
    }

    /**
     * Weist dem Rechteck eine Position Relativ zum Gesamtbild zu
     *
     * @param rectangle Rechteck
     */

    public void setRelativePosition(Rectangle rectangle) {
        String position = "";
        double imageWidth = UtilitysHelper.Image_Width;
        double imageHeigth = UtilitysHelper.Image_Heigth;
        double[] middle = {imageWidth / 3, imageHeigth / 3, imageWidth / 3, imageHeigth * 2 / 3, imageWidth * 2 / 3, imageHeigth * 2 / 3, imageWidth * 2 / 3, imageHeigth / 3};
        double[] left = {0, 0, 0, imageHeigth, imageWidth / 3, imageHeigth, imageWidth / 3, 0};
        double[] right = {imageWidth * 2 / 3, 0, imageWidth * 2 / 3, imageHeigth, imageWidth, imageHeigth, imageWidth, 0};
        double[] top = {0, 0, 0, imageHeigth / 3, imageWidth, imageHeigth / 3, imageWidth, 0};
        double[] bottom = {0, imageHeigth * 2 / 3, 0, imageHeigth, imageWidth, 0, imageWidth, imageWidth * 2 / 3};

        if ((rectangle.isOverlapping(left) && rectangle.isOverlapping(right)) || (rectangle.isOverlapping(top) && rectangle.isOverlapping(bottom))) {
            position = "vorne";
        } else {
            if (rectangle.isOverlapping(middle)) {
                position += "vorne";
            }
            if (rectangle.isOverlapping(left)) {
                position += " links";
            }
            if (rectangle.isOverlapping(right)) {
                position += " rechts";
            }
            if (rectangle.isOverlapping(top)) {
                position += " oben";
            }
            if (rectangle.isOverlapping(bottom)) {
                position += " unten";
            }
        }
        rectangle.setRelativePosition(position);
    }

    /**
     * Ueberprüft die Punktwolke auf zu nahe Objekte und traegt Warungen in eine Liste ein
     *
     * @param x,y,z Koordinaten
     */

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

    /**
     * Ueberprueft ob sich ein Punkt im Rechteck befindet
     *
     * @param x,y       Koordinaten
     * @param rectangle Rechteck
     * @return true wenn der Punkt sich im Rechteck befindet, ansonsten false
     */

    private boolean isPointInRectangleSimple(double x, double y, Rectangle rectangle) {
        Point[] points = rectangle.getPoints();
        if (y < points[0].y && y > points[1].y) {
            if (x > points[0].x && x < points[3].x) {
                return true;
            }
        }
        return false;
    }

    private boolean isPointInRectangleSimple(double x, double y, double[] array) {
        if (y < array[1] && y > array[3]) {
            if (x > array[0] && x < array[6]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Weist einem Rechteck eine Farbe zu
     *
     * @param rectangle Liste mit Rechtecken
     * @param mat       Mat Objekt von OpenCV
     */

    public void setRectangleColor(Rectangle rectangle, Mat mat) {
        Point points[] = rectangle.points;
        int precision = 1;
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> count = new ArrayList<>();
        String color;
        int borderX = (int) ((points[3].x - points[0].x) * 0.1);
        int borderY = (int) ((points[0].y - points[2].y) * 0.1);

        for (int x = (int) points[0].x + borderX; x <= (int) rectangle.points[3].x - precision - borderX; x += precision) {

            for (int y = (int) points[2].y + borderY; y <= (int) rectangle.points[0].y - precision - borderY; y += precision) {

                double data[] = mat.get(y, x);
                int dataInt[] = new int[3];
                for (int i = 0; i < data.length; i++) {
                    dataInt[i] = (int) data[i];
                }
                color = colorCube.getColor(dataInt);
                if (names.contains(color)) {
                    int index = names.indexOf(color);
                    count.set(index, count.get(index) + 1);
                } else {
                    names.add(color);
                    count.add(1);
                }
            }
        }
        int max = 0;
        int maxIndex = 0;
        for (int i = 0; i < count.size(); i++) {
            if (count.get(i) > max) {
                max = count.get(i);
                maxIndex = i;
            }
        }
        Log.d("color", "new rectangle");
        for (int i = 0; i < names.size(); i++) {
            Log.d("color", names.get(i) + " count" + String.valueOf(count.get(i)));
        }
        Log.d("color", "-------------------------------------");
        if (names.size() > 0) {
            rectangle.setColor(names.get(maxIndex));
        }


    }

    public void setRectangleColorTest(Rectangle rectangle, Mat mat) {
        Point points[] = rectangle.points;
        int precision = 5;
        int count = 0;
        int borderX = (int) ((points[3].x - points[0].x) * 0.1);
        int borderY = (int) ((points[0].y - points[2].y) * 0.1);
        int dataInt[] = new int[3];
        for (int x = (int) points[0].x + borderX; x <= (int) rectangle.points[3].x - precision - borderX; x += precision) {
            for (int y = (int) points[2].y + borderY; y <= (int) rectangle.points[0].y - precision - borderY; y += precision) {
                double data[] = mat.get(y, x);
                for (int i = 0; i < data.length; i++) {
                    dataInt[i] += (int) data[i];
                }
                count++;
            }
        }
        if (count != 0) {
            for (int i = 0; i < dataInt.length; i++) {
                dataInt[i] = dataInt[i] / count;
            }
            rectangle.setColor(colorCube.getColor(dataInt));
        } else {
            rectangle.setColor("undefiniert");
        }
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
                    overlayRenderer.updateRectangles(objects);
                    overlayRenderer.postInvalidate();
                }
                //List<Rectangle> objects = mObjectDetection.houghLinesP(mImageBuffer);
                Log.d("HFU_DEBUG", "Erkannte Objekte: " + String.valueOf(objects.size()));

                for (Rectangle object : objects) {
                    Log.d("HFU_DEBUG", String.valueOf(object));
                }
            }
        }
    }

    public boolean toggleWarnings() {
        showWarnings = !showWarnings;
        return showWarnings;
    }


}
