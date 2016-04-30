package hfu.tango.example.camera;

public class UpdatableBlockingBuffer<T> {
    private T t;
    private boolean available = false;

    public synchronized void update(T t) {
        this.t = t;
        available = true;
        notifyAll();
    }

    public synchronized T get() {
        while (!available) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                break;
            }
        }
        available = false;
        return t;
    }
}