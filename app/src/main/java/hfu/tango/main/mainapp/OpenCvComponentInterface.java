package hfu.tango.main.mainapp;


import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public interface OpenCvComponentInterface {

     List<Rectangle> contours(Mat m1);

}
