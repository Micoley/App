package com.example.timo.opengltest;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by Timo on 07.05.2016.
 */
public class TestView extends GLSurfaceView{
    OverlayRenderer ovr;
    public TestView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
       // setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        ovr = new OverlayRenderer();
        setRenderer(ovr);



    }
}
