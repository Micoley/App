package hfu.tango.main.mainapp;

import android.content.Context;
import android.util.AttributeSet;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraPreview;

import org.opencv.core.Mat;

/**
 * TangoCameraPreview die zur Anzeige des Kamerabildes und als Interface für benoetigte
 * OpenCV-Bilddaten benutzt wird
 */
public class CameraPreview extends TangoCameraPreview {

    /**
     * laedt die native Bibliothek und erlaubt den Zugriff auf dessen Funktionen
     */
    static {
        System.loadLibrary("native");
    }

    private native void setupFramebuffer();

    private native void destroyFramebuffer();

    /**
     * Holt die letzten verfuegbaren Bilddaten
     *
     * @param addr die Speicheradresse der Matrix in die, die
     *             verfuegbaren Bilddaten gespeichert werden
     */
    private native void getLatestBufferData(long addr);

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void connectToTangoCamera(Tango tango, int cameraId) {
        super.connectToTangoCamera(tango, cameraId);
        setupFramebuffer();
    }

    @Override
    public void disconnectFromTangoCamera() {
        super.disconnectFromTangoCamera();
        destroyFramebuffer();
    }

    /**
     * Holt die letzten verfuegbaren Bilddaten der Tango-API
     *
     * @return die Bilddaten als OpenCV-Matrix oder null falls kein Buffer verfuegbar ist
     */
    public Mat getLatestBufferData() {
        Mat mat = new Mat();
        getLatestBufferData(mat.getNativeObjAddr());
        return mat.empty() ? null : mat;
    }
}
