import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
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
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	public static void main(String[] args) {
		  int x1 = 1;
		  Mat mask = new Mat();
		  
		  BufferedImage img = null;
		  try {
			    img = ImageIO.read(new File("test3.jpg"));
			} catch (IOException e) {
				System.out.print("fail");
			}
		  Mat frame = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
		  byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		  frame.put(0, 0, pixels);
		  Imgproc.cvtColor(frame,frame,Imgproc.COLOR_BGR2GRAY);
		  //Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HSV);
		  
		  switch (x1){
		  case 0:
			  //FindContours
			  Imgproc.blur(frame, frame, new Size(3, 3));
			  Scalar minValues = new Scalar(0, 100, 100);
			  Scalar maxValues = new Scalar(100, 255, 255);
			 
			  Core.inRange(frame, minValues, maxValues, mask);
			  
			  List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			  Mat hierarchy = new Mat();
			  
			  Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
			  
			  Imgproc.cvtColor(frame,frame,Imgproc.COLOR_GRAY2BGR);
			  
			  if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
			  {
				  for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
			      {
					  System.out.println("check");
			          Imgproc.drawContours(frame, contours, idx, new Scalar(0, 255, 0), 2);
			      }
			  }
		  break;
		  //HoughLinesP
		  
		  //Imgproc.GaussianBlur(frame, frame, new Size(5,5), 0);
		  //Imgproc.adaptiveThreshold(frame, frame, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 0);
		  //Core.bitwise_not(frame, frame);
		  case 1:
			  Imgproc.Canny(frame,frame,150,600);
	
			  Mat lines = new Mat();
			  Mat copy = frame.clone();
			  Imgproc.HoughLinesP(copy, lines, 1, Math.PI/180, 200, 100, 20);
			  Imgproc.cvtColor(frame,frame,Imgproc.COLOR_GRAY2BGR);
			  System.out.println(lines.total());
			  for (int x = 0; x < lines.cols(); x++) {
			        double[] vec = lines.get(0, x);
			        for(double i : vec){
			        	System.out.println(i);
			        }
			        Imgproc.line(frame, new Point(vec[0],vec[1]), new Point(vec[2],vec[3]), new Scalar(0,255,0), 2);
			        System.out.println("check");
			  }
			  break;
			  default: break;
		  }
		  
		  
		  img = getImage(frame, ".jpg");
		  try {
			    ImageIO.write(img, "jpg", new File("saved.jpg"));
			} catch (IOException e) {
			}

	  }

}