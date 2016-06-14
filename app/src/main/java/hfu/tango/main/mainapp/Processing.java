package hfu.tango.main.mainapp;


import android.util.Log;

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
    private ArrayList<Rectangle> rectangles;
    private TextToSpeech textToSpeech;
    private Set<String> warnings;
    private float warningDistance = 0.50f;
    private long timeOld = 0;

    public Processing(CameraPreview cameraRenderer, TextToSpeech textToSpeech) {
        mCameraRenderer = cameraRenderer;
        mObjectDetection = new ObjectDetection();
        this.textToSpeech = textToSpeech;
        warnings = new TreeSet<String>() {
        };
    }

    public void update(FloatBuffer buffer) {
        this.buffer = buffer;
        //this.rectangles = rectangles;
        // mapPointsToObjects();
        if (System.currentTimeMillis() > timeOld + 5000) {
            timeOld = System.currentTimeMillis();

            float x, y;
            for (int i = 0; i < buffer.capacity(); i += 3) {
                x = UtilitysHelper.getXasDisplayCoordinate(buffer.get(i), buffer.get(i + 2));
                y = UtilitysHelper.getYasDisplayCoordinate(buffer.get(i + 1), buffer.get(i + 2));
                checkDistanceInAreas(x, y, buffer.get(i + 2));
            }
            readResults();
        }

    }

    private void readResults() {
        if (warnings != null) {

            for (String warning : warnings) {
                Log.d("output", String.valueOf(warning));
                textToSpeech.speak(warning, TextToSpeech.QUEUE_FLUSH, null);
            }
            warnings.clear();
        }
        //   textToSpeech.speakWithDelay("" + rectangles.size() + " obstacles detected.", 0);
        /*for (Rectangle rectangle : rectangles) {
            textToSpeech.speakWithDelay("There is a rectangle, distance " + rectangle.getDistance() + "meters, position " + rectangle.getRelativePosition() + ".", 0);
        }*/
    }

    private void mapPointsToObjects() {
        Rectangle rectangle;
        float x, y;
        for (int i = 0; i < buffer.capacity(); i += 3) {
            x = UtilitysHelper.getXasDisplayCoordinate(buffer.get(i), buffer.get(i + 2));
            y = UtilitysHelper.getYasDisplayCoordinate(buffer.get(i + 1), buffer.get(i + 2));
            checkDistanceInAreas(x, y, buffer.get(i + 2));
            for (int e = 0; i < rectangles.size(); e++) {
                rectangle = rectangles.get(e);
                if (isPointInRectangleSimple(x, y, rectangles.get(e))) {
                    rectangle.getDistancePoints().add(buffer.get(i + 2));
                }
                setRelativePosition(rectangle);
                setMedianDistance(rectangle);
                updateRectangleCoordinates(rectangle);
            }
        }
    }

    private void setRelativePosition(Rectangle rectangle) {
        Point[] points = rectangle.getPoints();
        float minDistanceX = Float.MAX_VALUE;
        float minDistanceY = Float.MAX_VALUE;
        for (int i = 0; i < points.length; i++) {
            float dx = (float) (UtilitysHelper.Display_Width / 2 - points[i].x);
            float dy = (float) (UtilitysHelper.Display_Heigth / 2 - points[i].y);
            if (Math.abs(dx) < Math.abs(minDistanceX)) {
                minDistanceX = dx;
            }
            if (Math.abs(dy) < Math.abs(minDistanceY)) {
                minDistanceY = dy;
            }
        }
        if (minDistanceX < UtilitysHelper.Display_Width * 0.2) {
            if (minDistanceY < UtilitysHelper.Display_Heigth * 0.1) {
                rectangle.setRelativePosition("left, top");
            } else if (minDistanceY > UtilitysHelper.Display_Heigth * 0.9) {
                rectangle.setRelativePosition("left, bottom");
            } else {
                rectangle.setRelativePosition("left");
            }
        }
        if (minDistanceX > UtilitysHelper.Display_Width * 0.8) {
            if (minDistanceY < UtilitysHelper.Display_Heigth * 0.1) {
                rectangle.setRelativePosition("right, top");
            } else if (minDistanceY > UtilitysHelper.Display_Heigth * 0.9) {
                rectangle.setRelativePosition("right, bottom");
            } else {
                rectangle.setRelativePosition("right");
            }
        } else {
            if (minDistanceY < UtilitysHelper.Display_Heigth * 0.1) {
                rectangle.setRelativePosition("front, top");
            } else if (minDistanceY > UtilitysHelper.Display_Heigth * 0.9) {
                rectangle.setRelativePosition("front, bottom");
            } else {
                rectangle.setRelativePosition("front");
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
        for (int i = 0; i < distancePoints.size(); i++) {
            if (distancePoints.get(i) < min) {
                min = distancePoints.get(i);
            }
        }
        for (int i = 0; i < distancePoints.size(); i++) {
            if (distancePoints.get(i) > min * 0.9) {
                sum += distancePoints.get(i);
                count++;
            } else {
                distancePoints.remove(i);
            }
        }
        rectangle.setDistance(sum / count);

    }


    private boolean isPointInRectangleSimple(double x, double y, Rectangle rectangle) {
        Point[] points = rectangle.getPoints();
        if (y > points[0].y && y < points[1].y) {
            if (x > points[0].x && x < points[3].x) {
                return true;
            }
        }
        return false;
    }

    private void updateRectangleCoordinates(Rectangle rectangle) {
        ArrayList<Float> distancePoints = rectangle.getDistancePoints();
        Point leftTopCorner = new Point();
        Point rightBottomCorner = new Point();
        for (int i = 0; i < distancePoints.size(); i++) {

            if (distancePoints.get(i) * distancePoints.get(i + 1) < leftTopCorner.x * leftTopCorner.y)
            {
                leftTopCorner.x = distancePoints.get(i);
                leftTopCorner.y = distancePoints.get(i + 1);
            }
            if (distancePoints.get(i) * distancePoints.get(i + 1) > rightBottomCorner.x * rightBottomCorner.y) {
                rightBottomCorner.x = distancePoints.get(i);
                rightBottomCorner.y = distancePoints.get(i + 1);
                rightBottomCorner.y = distancePoints.get(i + 1);
            }

        }
        Point[] rPoints = rectangle.getPoints();
        rPoints[0] = leftTopCorner;
        rPoints[1].x = rightBottomCorner.x;
        rPoints[1].y = leftTopCorner.y;
        rPoints[2] = rightBottomCorner;
        rPoints[3].x = leftTopCorner.x;
        rPoints[4].y = rightBottomCorner.y;
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
            List<Rectangle> objects = mObjectDetection.contours(mImageBuffer);
            //List<Rectangle> objects = mObjectDetection.houghLinesP(mImageBuffer);
            Log.d("HFU_DEBUG", "Erkannte Objekte: " + String.valueOf(objects.size()));

            for(Rectangle object: objects) {
                Log.d("HFU_DEBUG", String.valueOf(object));
            }
        }
    }

    public ArrayList<Rectangle> getRectangles() {
        return rectangles;
    }

}
