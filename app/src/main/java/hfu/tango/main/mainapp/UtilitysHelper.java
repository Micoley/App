package hfu.tango.main.mainapp;

/**
 * Created by Timo on 03.06.2016.
 */
public class UtilitysHelper {
    private static float fx = (float) OverlayRenderer.intrinsics.fx;
    private static float fy = (float) OverlayRenderer.intrinsics.fy;
    private static float cx = (float) OverlayRenderer.intrinsics.cx;
    private static float cy = (float) OverlayRenderer.intrinsics.cy;
    private static float w = (float) OverlayRenderer.intrinsics.width;
    private static float h = (float) OverlayRenderer.intrinsics.height;

    public static int Display_Heigth = 720;
    public static int Display_Width = 1280;
    public static int Image_Heigth = 720;
    public static int Image_Width = 1280;


    public static float getXasDisplayCoordinate(float x, float z) {
        return (x * fx + z * cx) / z * (Display_Width / w);
    }

    public static float getYasDisplayCoordinate(float y, float z) {
        return (y * fy + z * cy) / z * (Display_Heigth / h);
    }

    public static float getXAsImageCoordinate(float x, float z) {
        return (x * fx + z * cx) / z * (1280 / w);
    }

    public static float getYAsImageCoordinate(float y, float z) {
        return (y * fy + z * cy) / z * (720 / h);
    }
}
