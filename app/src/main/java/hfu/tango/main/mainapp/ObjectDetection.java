package hfu.tango.main.mainapp;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasse zum Analysieren eines Bildes
 */
public class ObjectDetection implements OpenCvComponentInterface {

    /**
     * Konturenerkennung
     * Ablauf:
     * 1. Mat wird von farbig zu grau konvertiert
     * 2. Cannyfilter sucht Kanten
     * 3. Linienbegradigung und Invertierung
     * 4. findContours() sucht Konturen
     * 5. Aus Konturen werden umschließende Rechtecke errechnet
     * 6. Gibt Liste von Rechtecken zurueck
     *
     * @param m1 Eingabebild als Mat
     */
    @Override
    public List<Rectangle> contours(Mat m1) {
        List<MatOfPoint> contours;
        List<Rectangle> output = new ArrayList<Rectangle>();
        Mat hierarchy = new Mat();

        Mat m = m1.clone();

        Imgproc.cvtColor(m, m, Imgproc.COLOR_RGB2GRAY); //Mat wird von farbig zu grau konvertiert

        Imgproc.Canny(m, m, 50, 150); //Cannyfilter sucht Kanten

        //Linienbegradigung und Invertierung
        Imgproc.GaussianBlur(m, m, new Size(3, 3), 0);
        Imgproc.threshold(m, m, 70, 255, Imgproc.THRESH_BINARY);

        Imgproc.GaussianBlur(m, m, new Size(3, 3), 0);
        Imgproc.threshold(m, m, 0, 80, Imgproc.THRESH_BINARY);

        Imgproc.GaussianBlur(m, m, new Size(3, 3), 0);
        Imgproc.threshold(m, m, 70, 255, Imgproc.THRESH_BINARY);

        Imgproc.GaussianBlur(m, m, new Size(3, 3), 0);
        Imgproc.threshold(m, m, 0, 80, Imgproc.THRESH_BINARY_INV);
        Imgproc.threshold(m, m, 70, 255, Imgproc.THRESH_BINARY);

        //findContours() sucht Konturen
        contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(m.clone(), contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
                //Aus Konturen werden umschließende Rechtecke errechnet
                int left = m.width();
                int right = 0;
                int up = 0;
                int down = m.height();
                for (int y = 0; y < contours.get(idx).total(); y++) {
                    for (int z = 0; z < contours.get(idx).cols(); z++) {
                        double[] vec = contours.get(idx).get(y, z);
                        if (vec[0] < left) left = (int) vec[0];
                        if (vec[0] > right) right = (int) vec[0];
                        if (vec[1] < down) down = (int) vec[1];
                        if (vec[1] > up) up = (int) vec[1];
                    }
                }
                if ((right - left) * (up - down) > 5000 && (right - left) * (up - down) < 1200 * 700) {
                    double[] rec = {left, up, left, down, right, down, right, up};
                    output.add(new Rectangle(rec));
                }
            }
        }
        return output; //Gibt Liste von Rechtecken zurueck
    }

    /**
     * Viereckerkennung
     * Ablauf:
     * 1. Mat wird von farbig zu grau konvertiert
     * 2. Cannyfilter sucht Kanten
     * 3. Linienbegradigung und Linienverdickung
     * 4. houghlinesP sucht Linien
     * 5. Jeweils 2 der gefundenen Linien werden untersucht und wenn moeglich verbunden.
     * 6. Jeweils 2 der verbundenen Linien werden untersucht und wenn moeglich verbunden.
     * 7. Gefundene Vierecke werden verglichen und zu ähnliche werden aussortiert.
     * 8. Gibt Liste von Vierecken zurück
     *
     * @param m1 Eingabebild as Mat
     */
    @Override
    public List<Rectangle> houghLinesP(Mat m1) {
        List<Rectangle> rec = new ArrayList<Rectangle>();
        List<Rectangle> output = new ArrayList<Rectangle>();
        Mat lines = new Mat();
        Mat m = m1.clone();

        Imgproc.cvtColor(m, m, Imgproc.COLOR_RGB2GRAY); //Mat wird von farbig zu grau konvertiert

        Imgproc.Canny(m, m, 50, 150); //Cannyfilter sucht Kanten

        //Linienbegradigung und Linienverdickung
        for (int i = 0; i < 5; i++) {
            Imgproc.GaussianBlur(m, m, new Size(3, 3), 0);
            Imgproc.threshold(m, m, 70, 255, Imgproc.THRESH_BINARY);

            Imgproc.GaussianBlur(m, m, new Size(3, 3), 0);
            Imgproc.threshold(m, m, 0, 80, Imgproc.THRESH_BINARY);
        }

        Imgproc.HoughLinesP(m, lines, 1, Math.PI / 180, 80, 80, 10); //houghlinesP sucht Linien

        //Jeweils 2 der gefundenen Linien werden untersucht und wenn moeglich verbunden.
        double maxDistance = 10;
        double minAngle = 40;
        List<double[]> twoLines = new ArrayList<double[]>();
        for (int y1 = 0; y1 < lines.total(); y1++) {
            for (int x1 = 0; x1 < lines.cols(); x1++) {
                double[] vec1 = lines.get(y1, x1);
                for (int y2 = y1; y2 < lines.total(); y2++) {
                    for (int x2 = x1; x2 < lines.cols(); x2++) {
                        double[] vec2 = lines.get(y2, x2);
                        if (y1 != y2 || x1 != x2) {
                            double[] res = new double[6];
                            boolean flag = false;
                            if (distance(vec1[0], vec1[1], vec2[0], vec2[1]) < maxDistance) {
                                if (angle(vec1[2], vec1[3], vec2[0], vec2[1], vec2[2], vec2[3]) > minAngle) {
                                    flag = true;
                                    res[0] = vec1[2];
                                    res[1] = vec1[3];
                                    res[2] = vec1[0];
                                    res[3] = vec1[1];
                                    res[4] = vec2[2];
                                    res[5] = vec2[3];
                                }
                            }
                            if (distance(vec1[2], vec1[3], vec2[2], vec2[3]) < maxDistance) {
                                if (angle(vec1[0], vec1[1], vec2[2], vec2[3], vec2[0], vec2[1]) > minAngle) {
                                    flag = true;
                                    res[0] = vec1[0];
                                    res[1] = vec1[1];
                                    res[2] = vec1[2];
                                    res[3] = vec1[3];
                                    res[4] = vec2[0];
                                    res[5] = vec2[1];
                                }
                            }
                            if (distance(vec1[0], vec1[1], vec2[2], vec2[3]) < maxDistance) {
                                if (angle(vec1[2], vec1[3], vec2[2], vec2[3], vec2[0], vec2[1]) > minAngle) {
                                    flag = true;
                                    res[0] = vec1[2];
                                    res[1] = vec1[3];
                                    res[2] = vec1[0];
                                    res[3] = vec1[1];
                                    res[4] = vec2[0];
                                    res[5] = vec2[1];
                                }
                            }
                            if (distance(vec1[2], vec1[3], vec2[0], vec2[1]) < maxDistance) {
                                if (angle(vec1[0], vec1[1], vec1[2], vec1[3], vec2[2], vec2[3]) > minAngle) {
                                    flag = true;
                                    res[0] = vec1[0];
                                    res[1] = vec1[1];
                                    res[2] = vec1[2];
                                    res[3] = vec1[3];
                                    res[4] = vec2[2];
                                    res[5] = vec2[3];
                                }
                            }
                            if (flag) {
                                twoLines.add(res);
                            }
                        }
                    }
                }
            }
        }

        //Jeweils 2 der verbundenen Linien werden untersucht und wenn moeglich verbunden.
        for (double[] a : twoLines) {
            for (double[] b : twoLines) {
                if ((a[2] != b[2]) && (a[3] != b[3])) {
                    boolean flag = false;
                    if (angle(b[2], b[3], a[0], a[1], a[2], a[3]) > minAngle && angle(b[2], b[3], a[4], a[5], a[2], a[3]) > minAngle) {
                        if (distance(a[0], a[1], b[0], b[1]) < maxDistance && distance(a[4], a[5], b[4], b[5]) < maxDistance) {
                            flag = true;
                        }
                        if (distance(a[0], a[1], b[4], b[5]) < maxDistance && distance(a[4], a[5], b[0], b[1]) < maxDistance) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        double[] c = new double[8];
                        c[0] = a[0];
                        c[1] = a[1];
                        c[2] = a[2];
                        c[3] = a[3];
                        c[4] = a[4];
                        c[5] = a[5];
                        c[6] = b[2];
                        c[7] = b[3];
                        Rectangle r = new Rectangle(c);
                        rec.add(r);
                    }
                }
            }
        }
        output = filter(rec); //Gefundene Vierecke werden verglichen und zu aehnliche werden aussortiert.
        return output; //Gibt Liste von Vierecken zurück
    }

    /**
     * Abstandsberechnung für 2 Punkte
     */
    private double distance(double ax, double ay, double bx, double by) {
        return Math.sqrt(Math.pow(ax - bx, 2) + Math.pow(ay - by, 2));
    }

    /**
     * Winkelberechnung für 3 Punkte
     */
    private double angle(double ax, double ay, double bx, double by, double cx, double cy) {
        if ((Math.sqrt(Math.pow(bx - ax, 2) + Math.pow(by - ay, 2)) * Math.sqrt(Math.pow(bx - cx, 2) + Math.pow(by - cy, 2))) * (180 / Math.PI) > 0) {
            return Math.acos(((bx - ax) * (bx - cx) + (by - ay) * (by - cy)) / (Math.sqrt(Math.pow(bx - ax, 2) + Math.pow(by - ay, 2)) * Math.sqrt(Math.pow(bx - cx, 2) + Math.pow(by - cy, 2)))) * (180 / Math.PI);
        }
        return 0;
    }

    /**
     * Nahezu gleich Vierecke von houghLinesP aussortieren
     */
    private List<Rectangle> filter(List<Rectangle> list) {
        List<Rectangle> res1 = new ArrayList<Rectangle>();
        List<Rectangle> res2 = new ArrayList<Rectangle>();
        boolean flag = true;
        for (Rectangle a : list) {
            res1.add(a);
        }
        for (Rectangle a : list) {
            res2.add(a);
        }
        while (flag) {
            res1.clear();
            for (Rectangle x : res2) {
                res1.add(x);
            }
            res2.clear();
            for (Rectangle x : res1) {
                res2.add(x);
            }
            flag = false;
            for (Rectangle a : res1) {
                Point[] aa = a.getPoints();
                for (Rectangle b : res1) {
                    if (!a.compare(b)) {
                        Point[] bb = b.getPoints();
                        boolean[] compare = new boolean[4];
                        for (int i = 0; i <= 3; i++) {
                            compare[i] = false;
                            for (Point b1 : bb) {
                                if (distance(aa[i].x, aa[i].y, b1.x, b1.y) < 10) {
                                    compare[i] = true;
                                }
                            }
                        }
                        if (compare[0] && compare[1] && compare[2] && compare[3]) {
                            res2.remove(a);
                            res2.remove(b);
                            res2.add(a);
                        }
                    }
                }
            }
        }
        return res2;
    }

}
