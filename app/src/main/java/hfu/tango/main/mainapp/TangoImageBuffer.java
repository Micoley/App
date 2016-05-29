package hfu.tango.main.mainapp;


public class TangoImageBuffer {
    public short[] data; // uint8_t

    public TangoImageBuffer() {
        data = new short[1280 * 720];
    }

}
