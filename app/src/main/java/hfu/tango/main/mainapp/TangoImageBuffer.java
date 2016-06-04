package hfu.tango.main.mainapp;


public class TangoImageBuffer {
    public byte[] data;

    public TangoImageBuffer() {
        data = new byte[1280 * 720 * 3 / 2];
    }

}
