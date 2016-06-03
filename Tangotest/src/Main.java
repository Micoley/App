import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.image.DataBufferByte;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

class Main {
	
	static public BufferedImage getImage(Mat matrix, String fileExten){
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(fileExten,matrix,mob); 
		 byte[] byteArray = mob.toArray();
		 BufferedImage bufImage = null;
		 try {
		        InputStream in = new ByteArrayInputStream(byteArray);
		        bufImage = ImageIO.read(in);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		 return bufImage;
	}
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
	}
	
	private Mat getMat(BufferedImage img){
		Mat m = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
		byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		System.out.println(pixels[126]);
		m.put(0, 0, pixels);
		Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
		return m;
	}
	
	public List<Rectangle> contours(Mat m1){
		List<MatOfPoint> contours;
		List<Rectangle> output = new ArrayList<Rectangle>();
		List<Rectangle> output2 = new ArrayList<Rectangle>();
		Mat hierarchy = new Mat();
		Mat m = m1.clone();

		Imgproc.Canny(m, m, 150, 450);
	 
		contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(m.clone(), contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.cvtColor(m, m, Imgproc.COLOR_GRAY2BGR);
		if (hierarchy.size().height > 0 && hierarchy.size().width > 0){
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]){
				int left = m.width();
				int right = 0;
				int up = 0;
				int down = m.height();
				for (int y = 0; y < contours.get(idx).total(); y++) { 
					for (int z = 0; z < contours.get(idx).cols(); z++) {
						double[] vec = contours.get(idx).get(y, z);
				        if(vec[0] < left) left = (int) vec[0];
				        if(vec[0] > right) right = (int) vec[0];
				        if(vec[1] < down) down = (int) vec[1];
				        if(vec[1] > up) up = (int) vec[1];
					}
				}
				if((right - left) * (up - down) > 100){
					double [] rec = {left, up, left, down, right, down, right, up};
					output.add(new Rectangle(rec));
					//Ausgabe
					Imgproc.drawContours(m, contours, idx, new Scalar(0, 0, 255), 1);
				}
			}
		}
		System.out.println(output.size());
		output2 = filter(output);
		System.out.println(output2.size());
		for(Rectangle a : output2){
			Point[] p = a.getPoints();
			Imgproc.rectangle(m, p[0], p[2], new Scalar(0, 255, 0), 1);
		}
		try {
			ImageIO.write(getImage(m, ".jpg"), "jpg", new File("saved.jpg"));
		} catch (IOException e) {}
		return output2;
	}
	
	public List<Rectangle> houghLinesP(Mat m1){
		List<Rectangle> rec = new ArrayList<Rectangle>();
		Mat lines = new Mat();
		Mat m = m1.clone();
		
//		Mat m2 = m.clone();
//		m.convertTo(m2, CvType.CV_32FC1);
//		Imgproc.Laplacian(m, m2, 5);
//		m2.convertTo(m, CvType.CV_8UC1);
		
		Imgproc.Canny(m, m, 150, 450);
		
		Imgproc.GaussianBlur(m, m, new Size(3,3), 0);
		Imgproc.threshold(m, m, 127, 255, Imgproc.THRESH_BINARY);
		
		Imgproc.GaussianBlur(m, m, new Size(3,3), 0);
		Imgproc.threshold(m, m, 0, 127, Imgproc.THRESH_BINARY);
		
		Imgproc.HoughLinesP(m, lines, 1, Math.PI/180, 80, 100, 20);
		double maxDistance = 10;
		double minAngle = 40;
		//Ausgabe
		Imgproc.cvtColor(m, m, Imgproc.COLOR_GRAY2BGR);
		List<double[]> twoLines = new ArrayList<double[]>();
		for (int y1 = 0; y1 < lines.total(); y1++) {
			for (int x1 = 0; x1 < lines.cols(); x1++) {
				double[] vec1 = lines.get(y1, x1);
				Imgproc.line(m, new Point(vec1[0],vec1[1]), new Point(vec1[2],vec1[3]), new Scalar(0,0,255), 1);
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
		for(Rectangle r : rec){
			Point[] d = r.getPoints();
			Imgproc.line(m, d[0], d[1], new Scalar(0,255,0), 1);
			Imgproc.line(m, d[1], d[2], new Scalar(0,255,0), 1);
			Imgproc.line(m, d[2], d[3], new Scalar(0,255,0), 1);
			Imgproc.line(m, d[3], d[0], new Scalar(0,255,0), 1);
		}
		try {
			ImageIO.write(getImage(m, ".jpg"), "jpg", new File("saved.jpg"));
		} catch (IOException e) {}
		return rec;
	}
	
	private double distance(double ax, double ay, double bx, double by){
		return Math.sqrt(Math.pow(ax-bx,2)+Math.pow(ay-by,2));
	}
	
	private double angle(double ax, double ay, double bx, double by, double cx, double cy){
		if((Math.sqrt(Math.pow(bx-ax,2)+Math.pow(by-ay,2))*Math.sqrt(Math.pow(bx-cx,2)+Math.pow(by-cy,2)))*(180/Math.PI) != 0){
			return Math.acos(((bx-ax)*(bx-cx)+(by-ay)*(by-cy))/(Math.sqrt(Math.pow(bx-ax,2)+Math.pow(by-ay,2))*Math.sqrt(Math.pow(bx-cx,2)+Math.pow(by-cy,2))))*(180/Math.PI);
		}
		return 0;
	}
	
	private List<Rectangle> filter(List<Rectangle> list){
		List<Rectangle> res1 = new ArrayList<Rectangle>();
		List<Rectangle> res2 = new ArrayList<Rectangle>();
		boolean flag = true;
		for(Rectangle a : list){
			res1.add(a);
		}
		for(Rectangle a : list){
			res2.add(a);
		}
		while(flag){
			res1.clear();
			for(Rectangle x : res2){
				res1.add(x);
			}
			res2.clear();
			for(Rectangle x : res1){
				res2.add(x);
			}
			flag = false;
			for(Rectangle a : res1){
				Point[] aa = a.getPoints();
				double areaA = (aa[2].x - aa[0].x) * (aa[0].y - aa[2].y);
				for(Rectangle b : res1){
					if(!a.compare(b)){
						Point[] bb = b.getPoints();
						double areaB = (bb[2].x - bb[0].x) * (bb[0].y - bb[2].y);
						if(aa[2].x > bb[0].x && aa[0].x < bb[2].x && aa[2].y < bb[0].y && aa[0].y > bb[2].y){
							Point upleft1 = new Point(aa[0].x, aa[0].y);
							Point downright1 = new Point(aa[2].x, aa[2].y);
							Point upleft2 = new Point(bb[0].x, bb[0].y);
							Point downright2 = new Point(bb[2].x, bb[2].y);
							if(aa[0].x < bb[0].x){
								upleft1.x = bb[0].x;
								upleft2.x = aa[0].x;
							}
							if(aa[0].y > bb[0].y){
								upleft1.y = bb[0].y;
								upleft2.y = aa[0].y;
							}
							if(aa[2].x > bb[2].x){
								downright1.x = bb[2].x;
								downright2.x = aa[2].x;
							}
							if(aa[2].y < bb[2].y){
								downright1.y = bb[2].y;
								downright2.y = aa[2].y;
							}
							double areaC = (downright1.x - upleft1.x) * (upleft1.y - downright1.y);
							if(areaC*2 > areaA || areaC*2 > areaB){
								
								Rectangle r = new Rectangle(new double[]{upleft2.x, upleft2.y, downright2.x, upleft2.y, downright2.x, downright2.y, upleft2.x, downright2.y});
								res2.remove(a);
								res2.remove(b);
								boolean add = true;
								for(Rectangle x : res2){
									if(x.compare(r))
										add = false;
								}
								if(add){
									res2.add(r);
									System.out.println("check");
								}
								flag = true;
							}
						}
					}
				}
			}
		}
		return res2;
	}
	
	public static void main(String[] args) {
		Main main = new Main();
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("test3.jpg"));
		} catch (IOException e) {
			System.out.print("Datei konnte nicht geladen werden.");
		};
		//List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		//Mat houghLinesP = new Mat();
		
		for(Rectangle r : main.contours(main.getMat(img))){
			r.print();
		};
//		System.out.println("______");
//		for(Rectangle r : main.houghLinesP(main.getMat(img))) {
//			r.print();
//		};
		
		/*for(Mat x : contours){
			System.out.println(x.toString());
		}
		System.out.println("_________________________");
		System.out.println(houghLinesP.toString());*/
		
		//Ausgabe als Datei
		/*img = getImage(m, ".jpg");
		try {
			ImageIO.write(img, "jpg", new File("saved.jpg"));
		} catch (IOException e) {
		}*/
	}
}