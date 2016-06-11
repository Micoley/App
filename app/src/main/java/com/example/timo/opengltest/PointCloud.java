package com.example.timo.opengltest;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Timo on 17.05.2016.
 */
public class PointCloud {


    private FloatBuffer mTriangle1Vertices;
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
    private float[] pointCloudVerticesData;


    // Set color with red, green, blue and alpha (opacity) values
//    private float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f, 0.83671875f, 0.76953125f, 0.22265625f, 1.0f};

    public PointCloud(OverlayRenderer or) {
        pointCloudVerticesData = new float[7000];

        for (int i = 0; i < pointCloudVerticesData.length; i += 14) {
            pointCloudVerticesData[i] = (float) (Math.random() * 2 - 1);
            pointCloudVerticesData[i + 1] = (float) (Math.random() * 2 - 1);
            pointCloudVerticesData[i + 2] = (float) (Math.random());
            pointCloudVerticesData[i + 3] = (float) (Math.random());
            pointCloudVerticesData[i + 4] = (float) (Math.random());
            pointCloudVerticesData[i + 5] = (float) (Math.random());
            pointCloudVerticesData[i + 6] = 1.0f;
            pointCloudVerticesData[i + 7] = pointCloudVerticesData[i] +0.04f;
            pointCloudVerticesData[i + 8] = pointCloudVerticesData[i + 1];
            pointCloudVerticesData[i + 9] =   pointCloudVerticesData[i + 2];
            pointCloudVerticesData[i + 10] = pointCloudVerticesData[i + 3];
            pointCloudVerticesData[i + 11] = pointCloudVerticesData[i + 4];
            pointCloudVerticesData[i + 12] = pointCloudVerticesData[i + 5];
            pointCloudVerticesData[i + 13] = 1.0f;
        }
        mTriangle1Vertices = ByteBuffer.allocateDirect(pointCloudVerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle1Vertices.put(pointCloudVerticesData).position(0);


    }

    public void draw(OverlayRenderer overlayRenderer) {
       /* for (int i = 0; i < pointCloudVerticesData.length; i += 7) {
            pointCloudVerticesData[i] = (float) (Math.random() * 2 - 1);
            pointCloudVerticesData[i + 1] = (float) (Math.random() * 2 - 1);
            pointCloudVerticesData[i + 2] = (float) (Math.random());
            pointCloudVerticesData[i + 3] = (float) (Math.random());
            pointCloudVerticesData[i + 4] = (float) (Math.random());
            pointCloudVerticesData[i + 5] = (float) (Math.random());
            pointCloudVerticesData[i + 6] = 1.0f;
        }
        mTriangle1Vertices.clear();
        mTriangle1Vertices.put(pointCloudVerticesData).position(0);*/
        //  aTriangleBuffer.position(mPositionOffset);
        mTriangle1Vertices.position(mPositionOffset);
        GLES20.glVertexAttribPointer(overlayRenderer.mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, mTriangle1Vertices);

        GLES20.glEnableVertexAttribArray(overlayRenderer.mPositionHandle);

        // Pass in the color information
        mTriangle1Vertices.position(mColorOffset);
        GLES20.glVertexAttribPointer(overlayRenderer.mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, mTriangle1Vertices);

        GLES20.glEnableVertexAttribArray(overlayRenderer.mColorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, overlayRenderer.mViewMatrix, 0, overlayRenderer.mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, overlayRenderer.mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glLineWidth(5000.0f);
        GLES20.glUniformMatrix4fv(overlayRenderer.mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 1000);


    }

}