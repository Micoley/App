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

    public Processing(CameraPreview cameraRenderer, TextToSpeech textToSpeech, OverlayRenderer overlayRenderer) {
        pointCloudManager = new TangoPointCloudManager();
        mCameraRenderer = cameraRenderer;
        this.overlayRenderer = overlayRenderer;
        mObjectDetection = new ObjectDetection();
        this.textToSpeech = textToSpeech;
        warnings = new TreeSet<String>() {
        };
    }

    public void updatePointCloudManager(TangoXyzIjData buffer) {
        pointCloudManager.updateXyzIj(buffer);
    }

    public void update(FloatBuffer buffer, List<Rectangle> rectangles) {
        this.buffer = buffer;
        // this.rectangles = rectangles;
        // mapPointsToObjects();
        //if (System.currentTimeMillis() > timeOld + 5000) {
        timeOld = System.currentTimeMillis();

        float x, y;
        for (int i = 0; i < buffer.capacity(); i += 3) {
            x = UtilitysHelper.getXasDisplayCoordinate(buffer.get(i), buffer.get(i + 2));
            y = UtilitysHelper.getYasDisplayCoordinate(buffer.get(i + 1), buffer.get(i + 2));
            checkDistanceInAreas(x, y, buffer.get(i + 2));
        }
        mapPointsToObjects(rectangles);
        overlayRenderer.updateRectangles(rectangles);
        overlayRenderer.postInvalidate();

        readResults(rectangles);
        //  }

    }

    private void readResults(List<Rectangle> rectangles) {
      /*  if (warnings != null) {

            for (String warning : warnings) {
                Log.d("output", String.valueOf(warning));
                textToSpeech.speak(warning, TextToSpeech.QUEUE_FLUSH, null);
            }
            warnings.clear();
        }*/
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
                ausgabe = "Rechteck"  + sr1.getRelativePosition() + " in " + (Math.round(sr1.getDistance() * 100)) + " centimetern Entfernung.";
            } else {
                ausgabe = "Rechteck"  + sr1.getRelativePosition() + " in " + (Math.round(sr1.getDistance() * 100) / 100) + " metern Entfernung.";
            }
            textToSpeech.speak(ausgabe, TextToSpeech.QUEUE_FLUSH, null);
            Log.d("Processing", ausgabe);

        }
        if (sr2.getDistance() < Double.MAX_VALUE) {
            if (sr1.getDistance() < 1) {
                ausgabe = "Rechteck"  + sr2.getRelativePosition() + " in " + (Math.round(sr2.getDistance() * 100)) + " centimetern Entfernung.";
            } else {
                ausgabe = "Rechteck"  + sr2.getRelativePosition() + " in " + (Math.round(sr2.getDistance() * 100) / 100) + " metern Entfernung.";
            }
            textToSpeech.speak(ausgabe, TextToSpeech.QUEUE_FLUSH, null);
            Log.d("Processing", ausgabe);

        }
        //   textToSpeech.speakWithDelay("" + rectangles.size() + " obstacles detected.", 0);
        /*for (Rectangle rectangle : rectangles) {
            textToSpeech.speakWithDelay("There is a rectangle, distance " + rectangle.getDistance() + "meters, position " + rectangle.getRelativePosition() + ".", 0);
        }*/
    }

    private void mapPointsToObjects(List<Rectangle> rectangles) {
        Rectangle rectangle;
        float x, y;
        for (int e = 0; e < rectangles.size(); e++) {
            rectangle = rectangles.get(e);
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
            // updateRectangleCoordinates(rectangle);
        }
    }

    private void setRelativePosition(Rectangle rectangle) {
        Point[] points = rectangle.getPoints();
        float minDistanceX = Float.MAX_VALUE;
        float minDistanceY = Float.MAX_VALUE;
        for (int i = 0; i < points.length; i++) {
            float dx = (float) (UtilitysHelper.Image_Width / 2 - points[i].x);
            float dy = (float) (UtilitysHelper.Image_Heigth / 2 - points[i].y);
            if (Math.abs(dx) < Math.abs(minDistanceX)) {
                minDistanceX = dx;
            }
            if (Math.abs(dy) < Math.abs(minDistanceY)) {
                minDistanceY = dy;
            }
        }
        if (minDistanceX < UtilitysHelper.Image_Width * 0.2) {
            if (minDistanceY < UtilitysHelper.Image_Heigth * 0.1) {
                rectangle.setRelativePosition("links, oben");
            } else if (minDistanceY > UtilitysHelper.Image_Heigth * 0.9) {
                rectangle.setRelativePosition("links, unten");
            } else {
                rectangle.setRelativePosition("links");
            }
        }
        if (minDistanceX > UtilitysHelper.Image_Width * 0.8) {
            if (minDistanceY < UtilitysHelper.Image_Heigth * 0.1) {
                rectangle.setRelativePosition("rechts, oben");
            } else if (minDistanceY > UtilitysHelper.Image_Heigth * 0.9) {
                rectangle.setRelativePosition("rechts, unten");
            } else {
                rectangle.setRelativePosition("rechts");
            }
        } else {
            if (minDistanceY < UtilitysHelper.Image_Heigth * 0.1) {
                rectangle.setRelativePosition("vorne, oben");
            } else if (minDistanceY > UtilitysHelper.Image_Heigth * 0.9) {
                rectangle.setRelativePosition("vorne, unten");
            } else {
                rectangle.setRelativePosition("vorne");
            }
        }

    }

    private void checkDistanceInAreas(float x, float y, float z) {
        if (z < warningDistance) {
            if (x < UtilitysHelper.Display_Width * 0.2) {
                if (y < UtilitysHelper.Display_Heigth * 0.1) {
                    warnings.add("Collision warning left, top");
                } else if (y > UtilitysHelper.Display_Heigth * 0.9) {
                    warnings.add("Collision warning left, bottom");
                } else {
                    warnings.add("Collision warning left");
                }
            }
            if (x > UtilitysHelper.Display_Width * 0.8) {
                if (y < UtilitysHelper.Display_Heigth * 0.1) {
                    warnings.add("Collision warning right, top");
                } else if (y > UtilitysHelper.Display_Heigth * 0.9) {
                    warnings.add("Collision warning right, bottom");
                } else {
                    warnings.add("Collision warning right");
                }
            } else {
                if (y < UtilitysHelper.Display_Heigth * 0.1) {
                    warnings.add("Collision warning front, Top");
                } else if (y > UtilitysHelper.Display_Heigth * 0.9) {
                    warnings.add("Collision warning front, bottom");
                } else {
                    warnings.add("Collision warning front");
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
            Log.d("Processing", "Left Top x/y: " + String.valueOf(leftTopCorner.x) + "/" + String.valueOf(leftTopCorner.y) + ", Right Bottom x/y: " + String.valueOf(rightBottomCorner.x) + "/" + String.valueOf(rightBottomCorner.y));
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
                    this.update(pointCloudManager.getLatestXyzIj().xyz, objects);
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
