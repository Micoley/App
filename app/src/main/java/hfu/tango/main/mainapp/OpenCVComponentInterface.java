package hfu.tango.main.mainapp;


import org.opencv.core.Mat;

import java.util.ArrayList;

public interface OpenCvComponentInterface {

     ArrayList<Object> getObjectsInImage(Mat mat);

}
