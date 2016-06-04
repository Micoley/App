package hfu.tango.main.mainapp;

import org.opencv.core.Point;

public class Rectangle {
    Point[] points;

    public Rectangle(double[] p){
        points = new Point[4];
        for(int i = 0; i < 4; i++){
            points[i] = new Point(p[2*i], p[2*i +1]);
        }
    }

    public Rectangle(){
        points = new Point[4];
    }

    public void sePoints(double[] p){
        for(int i = 0; i < 4; i++){
            points[i] = new Point(p[2*i], p[2*i+1]);
        }
    }

    public Point[] getPoints(){
        Point[] r = new Point[4];
        r = points.clone();
        return r;
    }

    @Override
    public String toString() {
        String s = "";
        for(Point p : points){
            s += p.x + " " + p.y + "\t";
        }
        return s;
    }

    public boolean compare(Rectangle r){
        Point[] p = r.getPoints();
        return (p[0].x == points[0].x && p[0].y == points[0].y
                && p[1].x == points[1].x && p[1].y == points[1].y
                && p[2].x == points[2].x && p[2].y == points[2].y
                && p[3].x == points[3].x && p[3].y == points[3].y);
    }
}
