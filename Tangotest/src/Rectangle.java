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
			points[i] = new Point(p[2*i], p[2*i +1]);
		}
	}
	
	public double[] getPoints(){
		double[] r = new double[8];
		for(int i = 0; i < 4; i++){
			r[2*i] = points[i].x;
			r[2*i+1] = points[i].y;
		}
		return r;
	}
	
	public void print(){
		for(Point p : points){
			System.out.print(p.x+" "+p.y+"\t");
		}
		System.out.println();
	}
}
