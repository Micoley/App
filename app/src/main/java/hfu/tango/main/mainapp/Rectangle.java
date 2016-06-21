package hfu.tango.main.mainapp;

import org.opencv.core.Point;

import java.util.ArrayList;

public class Rectangle {
    Point[] points;


    private ArrayList<Float> distancePoints;
    private double[] linearFunctions; //d,b
    private double distance;
    private String relativePosition = "";
    private String color = "konnte nicht erkannt werden";


    public Rectangle(double[] p) {
        points = new Point[4];
        distance = Double.MAX_VALUE;
        distancePoints = new ArrayList<Float>();
        linearFunctions = new double[8];
        for (int i = 0; i < 4; i++) {
            points[i] = new Point(p[2 * i], p[2 * i + 1]);
        }
        //  pointsToLinearFunctions();

    }

    public Rectangle() {
        points = new Point[4];
        distance = Double.MAX_VALUE;
        distancePoints = new ArrayList<Float>();

    }

    public void sePoints(double[] p) {
        for (int i = 0; i < 4; i++) {
            points[i] = new Point(p[2 * i], p[2 * i + 1]);
        }
        //  pointsToLinearFunctions();
    }

    public Point[] getPoints() {
        Point[] r = new Point[4];
        r = points.clone();
        return r;
    }

    public void print() {
        for (Point p : points) {
            System.out.print(p.x + " " + p.y + "\t");
        }
        System.out.println();
    }

    @Override
    public String toString() {
        String s = "";
        for(Point p: points) {
            s += p.x + " " + p.y + "\t";
        }
        return s;
    }

    public boolean compare(Rectangle r) {
        Point[] p = r.getPoints();
        return (p[0].x == points[0].x && p[0].y == points[0].y
                && p[1].x == points[1].x && p[1].y == points[1].y
                && p[2].x == points[2].x && p[2].y == points[2].y
                && p[3].x == points[3].x && p[3].y == points[3].y);
    }

    private void pointsToLinearFunctions() {

        double w, h;

        //oben
        w = points[1].x - points[0].x;
        h = points[1].y - points[0].y;
        linearFunctions[0] = w / h;
        linearFunctions[1] = points[0].y - points[0].x * linearFunctions[0];

        //unten
        w = points[3].x - points[4].x;
        h = points[3].y - points[4].y;
        linearFunctions[2] = w / h;
        linearFunctions[3] = points[3].y - points[3].x * linearFunctions[2];

        //links
        w = points[0].x - points[3].x;
        h = points[0].y - points[3].y;
        linearFunctions[4] = w / h;
        linearFunctions[5] = points[0].y - points[0].x * linearFunctions[4];

        //rechts
        w = points[2].x - points[3].x;
        h = points[2].y - points[3].y;
        linearFunctions[6] = w / h;
        linearFunctions[7] = points[2].y - points[2].x * linearFunctions[6];


    }

    public double[] getLinearFunctions() {
        return linearFunctions;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public ArrayList<Float> getDistancePoints() {
        return distancePoints;
    }

    public void setDistancePoints(ArrayList<Float> distancePoints) {
        this.distancePoints = distancePoints;
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

    public void setLinearFunctions(double[] linearFunctions) {
        this.linearFunctions = linearFunctions;
    }

    public String getRelativePosition() {
        return relativePosition;
    }

    public void setRelativePosition(String relativePosition) {
        this.relativePosition = relativePosition;
    }
    public void setColor(String color){
        this.color = color;
    }
    public String getColor(){
        return color;
    }

}