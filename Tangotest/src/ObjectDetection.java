import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class ObjectDetection { //implements OpenCVComponentInterface
	public List<Rectangle> contours(Mat m1){
		List<MatOfPoint> contours;
		List<Rectangle> output = new ArrayList<Rectangle>();
		Mat hierarchy = new Mat();
		Mat m = m1.clone();

		Imgproc.Canny(m, m, 150, 450);
	 
		contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(m.clone(), contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.cvtColor(m, m, Imgproc.COLOR_GRAY2BGR);
		if (hierarchy.size().height > 0 && hierarchy.size().width > 0){
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]){
				int up = m.width();
				int down = 0;
				int left = 0;
				int right = m.height();
				for (int y = 0; y < contours.get(idx).total(); y++) { 
					for (int z = 0; z < contours.get(idx).cols(); z++) {
						double[] vec = contours.get(idx).get(y, z);
				        if(vec[0] < up) up = (int) vec[0];
				        if(vec[0] > down) down = (int) vec[0];
				        if(vec[1] < right) right = (int) vec[1];
				        if(vec[1] > left) left = (int) vec[1];
					}
				}
				double [] rec = {up, left, up, right, down, right, down, left};
				output.add(new Rectangle(rec));
			}
		}
		return output;
	}
	
	public List<Rectangle> houghLinesP(Mat m1){
		List<Rectangle> rec = new ArrayList<Rectangle>();
		Mat lines = new Mat();
		Mat m = m1.clone();
		
		Imgproc.Canny(m, m, 150, 450);
		
		Imgproc.GaussianBlur(m, m, new Size(3,3), 0);
		Imgproc.threshold(m, m, 1, 255, Imgproc.THRESH_BINARY);
		
		Imgproc.HoughLinesP(m, lines, 1, Math.PI/180, 50, 80, 20);
		double maxDistance = 10;
		double minAngle = 30;
		List<double[]> twoLines = new ArrayList<double[]>();
		for (int y1 = 0; y1 < lines.total(); y1++) {
			for (int x1 = 0; x1 < lines.cols(); x1++) {
				double[] vec1 = lines.get(y1, x1);
				for (int y2 = y1; y2 < lines.total(); y2++) {
					for (int x2 = x1; x2 < lines.cols(); x2++) {
						double[] vec2 = lines.get(y2, x2);
						if(y1 != y2 || x1 != x2){
							double[] res = new double[6];
							boolean flag = false;
							if(distance(vec1[0], vec1[1], vec2[0], vec2[1]) < maxDistance){
								if(angle(vec1[2], vec1[3], vec2[0], vec2[1], vec2[2], vec2[3])> minAngle){
									flag = true;
									res[0] = vec1[2];
									res[1] = vec1[3];
									res[2] = vec1[0];
									res[3] = vec1[1];
									res[4] = vec2[2];
									res[5] = vec2[3];
								}
							}
							if(distance(vec1[2], vec1[3], vec2[2], vec2[3]) < maxDistance){
								if(angle(vec1[0], vec1[1], vec2[2], vec2[3], vec2[0], vec2[1])> minAngle){
									flag = true;
									res[0] = vec1[0];
									res[1] = vec1[1];
									res[2] = vec1[2];
									res[3] = vec1[3];
									res[4] = vec2[0];
									res[5] = vec2[1];
								}
							}
							if(distance(vec1[0], vec1[1], vec2[2], vec2[3]) < maxDistance){
								if(angle(vec1[2], vec1[3], vec2[2], vec2[3], vec2[0], vec2[1])> minAngle){
									flag = true;
									res[0] = vec1[2];
									res[1] = vec1[3];
									res[2] = vec1[0];
									res[3] = vec1[1];
									res[4] = vec2[0];
									res[5] = vec2[1];
								}
							}
							if(distance(vec1[2], vec1[3], vec2[0], vec2[1]) < maxDistance){
								if(angle(vec1[0], vec1[1], vec1[2], vec1[3], vec2[2], vec2[3])> minAngle){
									flag = true;
									res[0] = vec1[0];
									res[1] = vec1[1];
									res[2] = vec1[2];
									res[3] = vec1[3];
									res[4] = vec2[2];
									res[5] = vec2[3];
								}
							}
							if(flag){
								twoLines.add(res);
							}
						}
					}
				}
			}
		}
		for(double[] a : twoLines){
			for(double[] b : twoLines){
				if((a[2] != b[2])&&(a[3] != b[3])){
					boolean flag = false;
					if(angle(b[2],b[3],a[0],a[1],a[2],a[3]) > minAngle && angle(b[2],b[3],a[4],a[5],a[2],a[3]) > minAngle){
						if(distance(a[0], a[1], b[0], b[1]) < maxDistance && distance(a[4], a[5], b[4], b[5]) < maxDistance){
							flag = true;
						}
						if(distance(a[0], a[1], b[4], b[5]) < maxDistance && distance(a[4], a[5], b[0], b[1]) < maxDistance){
							flag = true;
						}
					}
					if(flag){
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
		return rec;
	}
	
	private double distance(double ax, double ay, double bx, double by){
		return Math.sqrt(Math.pow(ax-bx,2)+Math.pow(ay-by,2));
	}
	
	private double angle(double ax, double ay, double bx, double by, double cx, double cy){
		if((Math.sqrt(Math.pow(bx-ax,2)+Math.pow(by-ay,2))*Math.sqrt(Math.pow(bx-cx,2)+Math.pow(by-cy,2)))*(180/Math.PI) > 0){
			return Math.acos(((bx-ax)*(bx-cx)+(by-ay)*(by-cy))/(Math.sqrt(Math.pow(bx-ax,2)+Math.pow(by-ay,2))*Math.sqrt(Math.pow(bx-cx,2)+Math.pow(by-cy,2))))*(180/Math.PI);
		}
		return 0;
	}
}
