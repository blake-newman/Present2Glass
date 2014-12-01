package present.to.glass.blakenewman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;

import java.util.Timer;
import java.util.TimerTask;

import present.to.glass.blakenewman.controllers.Client;
import present.to.glass.blakenewman.controllers.Server;

public class Presenter extends Activity {

    static public Activity context;

    private GestureDetector mGestureDetector;


    /** Audio manager used to play system sound effects. */
    static private AudioManager audioManager;


    private static CardBuilder card;
    private static View view;

    private static Timer screenTimer;
    private static TimerTask screenTask;
    public static Boolean alive = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = this;
        card = new CardBuilder(this, CardBuilder.Layout.TEXT);
        card.setText("Loading");
        view = card.getView();
        context.setContentView(view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = createGestureDetector(this);

        alive = true;
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.SWIPE_RIGHT) {
                    Main.client.nextNote();
                    return true;
                } else if (gesture == Gesture.TWO_SWIPE_RIGHT) {
                    Main.client.nextSlide();
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    Main.client.prevNote();
                    return true;
                } else if (gesture == Gesture.TWO_SWIPE_LEFT) {
                    Main.client.prevSlide();
                    return true;
                } else if (gesture == Gesture.SWIPE_DOWN) {
                    audioManager.playSoundEffect(Sounds.DISMISSED);
                    Main.client.stopPresentation();
                    finish();
                    return true;
                }
                audioManager.playSoundEffect(Sounds.DISALLOWED);
                return false;
            }
        });

        return gestureDetector;
    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector != null && mGestureDetector.onMotionEvent(event);
    }


    public static void update(final String content, Long time){
        wake();
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                card.setText(content);
                view = card.getView();
                context.setContentView(view);
            }
        });
        if(time != null && time != 0){
            screenTimer.schedule(screenTask, time);
        }
    }

    public static void wake(){
        try {
            screenTask.cancel();
            screenTimer.cancel();
        } catch (Exception ignored) {

        }
        // for some reason the timer wouldn't reschedule unless it was recreated.
        screenTimer = new Timer();
        screenTask = new TimerTask() {
            @Override
            public void run() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager.LayoutParams params = context.getWindow().getAttributes();
                        params.screenBrightness = 0.0f;
                        params.alpha = 0.0f;
                        context.getWindow().setAttributes(params);
                    }
                });
            }
        };
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WindowManager.LayoutParams params = context.getWindow().getAttributes();
                params.screenBrightness = 1.0f;
                params.alpha = 1.0f;
                context.getWindow().setAttributes(params);
            }
        });
    }

    @Override
    public void finish(){
        alive = false;
        super.finish();
    }
}
