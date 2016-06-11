package com.example.timo.opengltest;

/**
 * Created by Timo on 04.06.2016.
 */

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


import java.nio.ShortBuffer;

/**
 * Created by Timo on 17.05.2016.
 */
public class Rectangle_Drawable {


    private FloatBuffer rectangleVerticesBuffer;
    private int mBytesPerFloat = 4;
    // New class members
    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private float[] mMVPMatrix = new float[16];

    /**
     * How many elements per vertex.
     */
    private final int mStrideBytes = 7 * mBytesPerFloat;

    /**
     * Offset of the position data.
     */
    private final int mPositionOffset = 0;

    /**
     * Size of the position data in elements.
     */
    private final int mPositionDataSize = 3;

    /**
     * Offset of the color data.
     */
    private final int mColorOffset = 3;


    /**
     * Size of the color data in elements.
     */
    private final int mColorDataSize = 4;
    private ShortBuffer drawListBuffer;
    private short drawOrder[] = { 0, 1, 2, 2, 3, 0 }; // order to draw vertices


    // Set color with red, green, blue and alpha (opacity) values
//    private float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f, 0.83671875f, 0.76953125f, 0.22265625f, 1.0f};

    public Rectangle_Drawable(OverlayRenderer or) {
        final float[] rectangleVerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, 0.5f, 0.5f,
                1.0f, 0.0f, 0.0f, 1.0f,

                -0.5f, -0.5f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.5f, 0.5f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, 0.5f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

              };
        rectangleVerticesBuffer = ByteBuffer.allocateDirect(rectangleVerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        rectangleVerticesBuffer.put(rectangleVerticesData).position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


    }

    public void draw(OverlayRenderer overlayRenderer) {
        rectangleVerticesBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(overlayRenderer.mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, rectangleVerticesBuffer);

        GLES20.glEnableVertexAttribArray(overlayRenderer.mPositionHandle);

        // Pass in the color information
        rectangleVerticesBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(overlayRenderer.mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, rectangleVerticesBuffer);

        GLES20.glEnableVertexAttribArray(overlayRenderer.mColorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, overlayRenderer.mViewMatrix, 0, overlayRenderer.mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, overlayRenderer.mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glLineWidth(5.0f);
        GLES20.glUniformMatrix4fv(overlayRenderer.mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
       // GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDrawElements(GLES20.GL_LINE_LOOP, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

    }

}