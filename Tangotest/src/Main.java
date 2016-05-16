import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
	public Mat getMat(BufferedImage img){
		Mat m = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
		byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		m.put(0, 0, pixels);
		return m;
	}
	
	public List<MatOfPoint> contours(BufferedImage img){
		Mat mask = new Mat();
		Scalar minValues;
		Scalar maxValues;
		List<MatOfPoint> contours;
		List<MatOfPoint> output = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Mat m = getMat(img);
		
		Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
		Imgproc.blur(m, m, new Size(3, 3));
		 
		for(int i = 0; i <= 190; i = i + 64){
			contours = new ArrayList<MatOfPoint>();
			minValues = new Scalar(i, i, i);
			maxValues = new Scalar(i+64, i+64, i+64);
			Core.inRange(m, minValues, maxValues, mask);
			  
			Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
			  
			if (hierarchy.size().height > 0 && hierarchy.size().width > 0){
				for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]){
					if(contours.get(idx).total() > 30){
						output.add(contours.get(idx));
						//Ausgabe
						/*for (int y = 0; y < contours.get(idx).total(); y++) { 
							for (int z = 0; z < contours.get(idx).cols(); z++) {
								double[] vec = contours.get(idx).get(y, z);
						        for(double a : vec){
						        	System.out.print(a + " ");
						        }
						        System.out.println(" ");
							}
						}
						Imgproc.drawContours(m, contours, idx, new Scalar(0, 255, 0), 1);*/
					}
				}
			}
		}
		return output;
	}
	public List<Rectangle> houghLinesP(Mat m1){
		List<Rectangle> rec = new ArrayList<Rectangle>();
		Mat lines = new Mat();
		Mat m = m1.clone();
		
		Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
		Imgproc.Canny(m, m, 30, 600);
//		Imgproc.GaussianBlur(m, m, new Size(3,3), 0);
		Imgproc.HoughLinesP(m, lines, 1, Math.PI/180, 100, 100, 10);
		//Ausgabe
		/*for (int y = 0; y < lines.total(); y++) {
			for (int x = 0; x < lines.cols(); x++) {
				double[] vec = lines.get(y, x);
					Imgproc.line(frame, new Point(vec[0],vec[1]), new Point(vec[2],vec[3]), new Scalar(0,255,0), 2);
			}
		}
		Imgproc.cvtColor(m, m, Imgproc.COLOR_GRAY2BGR);*/
		List<double[]> twoLines = new ArrayList<double[]>();
		for (int y1 = 0; y1 < lines.total(); y1++) {
			for (int x1 = 0; x1 < lines.cols(); x1++) {
				double[] vec1 = lines.get(y1, x1);
				/*for(double d : vec1){			
					System.out.print(d+ "|");
				}
				System.out.println();*/
				for (int y2 = y1; y2 < lines.total(); y2++) {
					for (int x2 = x1; x2 < lines.cols(); x2++) {
						if(y1 != y2 || x1 != x2){
							double[] vec2 = lines.get(y2, x2);
							double[] res = new double[6];
							boolean flag = false;
							if(Math.abs(vec1[0] - vec2[0]) < 3 && Math.abs(vec1[1] - vec2[1]) < 3){
								flag = !flag;
								res[0] = vec1[2];
								res[1] = vec1[3];
								res[2] = vec1[0];
								res[3] = vec1[1];
								res[4] = vec2[2];
								res[5] = vec2[3];
							}
							if(Math.abs(vec1[2] - vec2[2]) < 3 && Math.abs(vec1[3] - vec2[3]) < 3){
								flag = !flag;
								res[0] = vec1[0];
								res[1] = vec1[1];
								res[2] = vec1[2];
								res[3] = vec1[3];
								res[4] = vec2[0];
								res[5] = vec2[1];
							}
							if(Math.abs(vec1[0] - vec2[2]) < 3 && Math.abs(vec1[1] - vec2[3]) < 3){
								flag = !flag;
								res[0] = vec1[2];
								res[1] = vec1[3];
								res[2] = vec1[0];
								res[3] = vec1[1];
								res[4] = vec2[0];
								res[5] = vec2[1];
							}
							if(Math.abs(vec1[2] - vec2[0]) < 3 && Math.abs(vec1[3] - vec2[1]) < 3){
								flag = !flag;
								res[0] = vec1[0];
								res[1] = vec1[1];
								res[2] = vec1[2];
								res[3] = vec1[3];
								res[4] = vec2[2];
								res[5] = vec2[3];
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
					if(Math.abs(a[0] - b[0]) < 5 && Math.abs(a[1] - b[1]) < 5 && Math.abs(a[4] - b[4]) < 5 && Math.abs(a[5] - b[5]) < 5){
						flag = !flag;
					}
					if(Math.abs(a[0] - b[4]) < 5 && Math.abs(a[1] - b[5]) < 5 && Math.abs(a[4] - b[0]) < 5 && Math.abs(a[5] - b[1]) < 5){
						flag = !flag;
					}
					if(flag && Math.sqrt(Math.pow(a[2]-b[2],2)+Math.pow(a[3]-b[3],2)) > 15){
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
			for(double x : a){
				System.out.print(x+"|");
			}
			System.out.println();
		}
		for(Rectangle r : rec){
			r.print();
		}
		return rec;
	}
	
	public static void main(String[] args) {
		Main main = new Main();
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("test2.jpg"));
		} catch (IOException e) {
			System.out.print("Datei konnte nicht geladen werden.");
		};
		//List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		//Mat houghLinesP = new Mat();
		
		//contours = main.contours(img);
		main.houghLinesP(main.getMat(img));
		
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