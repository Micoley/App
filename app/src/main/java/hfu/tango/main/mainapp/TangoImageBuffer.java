package hfu.tango.main.mainapp;


public class TangoImageBuffer {
    public short[] data; // uint8_t

    public TangoImageBuffer() {
        data = new short[(int) (1280 * 720 * 1.5)];
    }

}
