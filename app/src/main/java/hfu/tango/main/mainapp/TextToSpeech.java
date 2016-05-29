package hfu.tango.main.mainapp;


import android.content.Context;

public class TextToSpeech extends android.speech.tts.TextToSpeech {

    public TextToSpeech(Context context, OnInitListener listener, String engine) {
        super(context, listener, engine);
    }

    public TextToSpeech(Context context, OnInitListener listener) {
        super(context, listener);
    }

    public void speakWithDelay(String message, int delay) {
        new DelayedSpeaker(message, delay).start();
    }

    private class DelayedSpeaker extends Thread {
        private final String mMessage;
        private final int mDelay;

        public DelayedSpeaker(String message, int delay) {
            mMessage = message;
            mDelay = delay;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(mDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            speak(mMessage, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
