package present.to.glass.blakenewman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import com.google.android.glass.app.Card;

import java.util.Timer;
import java.util.TimerTask;

import present.to.glass.blakenewman.controllers.Client;
import present.to.glass.blakenewman.controllers.Server;

public class Presenter extends Activity {

    static public Activity context;

    private static Card card;
    private static View view;

    private static Timer screenTimer;
    private static TimerTask screenTask;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = this;
        card = new Card(this);
        card.setText("Loading");
        view = card.getView();
        context.setContentView(view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    public static void update(final String content, Boolean stream, Long time){
        wake();
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                card.setText(content);
                view = card.getView();
                context.setContentView(view);
            }
        });
        if(!stream && time != null && time != 0){
            screenTimer.schedule(screenTask, time);
        }
    }

    public static void wake(){
        try {
            screenTask.cancel();
            screenTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
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
        Main.client.stopPresentation();
        super.finish();
    }
}
