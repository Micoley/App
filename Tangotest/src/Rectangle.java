import org.opencv.core.Point;

public class Rectangle {
	Point[] points;
	
	//Übergabe = 4 Punkte als double[] in der Form x1, y1, x2, y2, ...
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
	
	//Gibt 4 Punkte zurück
	public Point[] getPoints(){
		Point[] r = new Point[4];
		r = points.clone();
		return r;
	}
	
	//Ausgabe der Punkte, nur für Tests
	public void print(){
		for(Point p : points){
			System.out.print(p.x+" "+p.y+"\t");
		}
		System.out.println();
	}
	
	//vergleicht alle 4 Punkte eines Rechtecks (Reihenfolge der Punkte wichtig)
	public boolean compare(Rectangle r){
		Point[] p = r.getPoints();
		return (p[0].x == points[0].x && p[0].y == points[0].y 
				&& p[1].x == points[1].x && p[1].y == points[1].y 
				&& p[2].x == points[2].x && p[2].y == points[2].y 
				&& p[3].x == points[3].x && p[3].y == points[3].y);
	}
}
