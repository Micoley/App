package com.example.timo.opengltest;

/**
 * Created by Timo on 17.05.2016.
 */
import android.util.Log;

public class fps_counter {
    long startTime = System.nanoTime();
    int frames = 0;

    public void logFrame() {
        frames++;
        if(System.nanoTime() - startTime >= 1000000000) {
            Log.d("FPSCounter", "fps: " + frames);
            frames = 0;
            startTime = System.nanoTime();
        }
    }
}