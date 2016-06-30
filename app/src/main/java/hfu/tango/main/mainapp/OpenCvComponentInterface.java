package hfu.tango.main.mainapp;


import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface fuer den Zugriff auf die Bildverarbeitung
 */
public interface OpenCvComponentInterface {

    List<Rectangle> contours(Mat m1);

    List<Rectangle> houghLinesP(Mat m1);

}
