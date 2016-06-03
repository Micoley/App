/**
 * Methode zur Umrechnung von YUV to RGB Format
 * 
 * @author Michael
 * Datum: 03.06.2016
 * 
 */

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Umrechnung {
		public Umrechnung()
		{
			super();
		}
		
		/*
		 * Wandelt das Buffer vom Datentyp yuv12 int ein RGB Mat um
		 * 
		 */
		public Mat yuvToRgb(short[] yuvBuffer)
		{
			int width = 1280; //Auflösung X-Achse Tango
			int height = 720; //Auflösung Y-Achse Tango
			int size = width*height;
//			int sizeYValues = size;
//			int sizeUVValues = (1280/2)*(720/2);
			int yValue, uValue, vValue; //Extrahierte Werte aus yuvBuffer werden hier gespeichert
			int[] data = new int[3]; //Speichern der RGB-Werte
			Mat rgb = new Mat(width, height, CvType.CV_8UC3); //CvType.CV_8UC3 = RGB
			
			/*
			 * R = 1.164(Y - 16) + 1.596(V - 128)
			 * G = 1.164(Y - 16) - 0.813(V - 128) - 0.391(U - 128)
			 * B = 1.164(Y - 16)                  + 2.018(U - 128)
			 */
			
			//Schleifen über X/Y Werte entsprechend Auflösung
			for(int x = 0;  x < width; x++)
			{
				for(int y = 0; y < height; y++)
				{
					yValue = yuvBuffer[y * width + x]; //Zugriff auf Y(yuv) Wert an x/y(rgb) stelle
					uValue = yuvBuffer[(y / 2) * (width / 2) + (x / 2) + size]; //Zugriff auf U(yuv) Wert an x/y stelle
					vValue = yuvBuffer[(y / 2) * (width / 2) + (x / 2) + size + (size / 4)]; //Zugriff auf V(yuv) Wert an x/y stelle
					data[0] = (int) (1.164*(yValue - 16) + 1.596*(vValue - 128)); //Berechnung R wert and x/y stelle
					data[1] = (int) (1.164*(yValue - 16) - 0.813*(vValue - 128) - 0.391*(uValue - 128)); //Berechnung G wert and x/y stelle
					data[2] = (int) (1.164*(yValue - 16) + 2.018*(uValue - 128)); //Berechnung B wert and x/y stelle
					rgb.put(x, y, data); //speichern des berechneten Punkts mit RGB Werten
				}
			}
			return rgb;
		}
}
