package present.to.glass.blakenewman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;

import java.util.Timer;
import java.util.TimerTask;

import present.to.glass.blakenewman.controllers.Client;
import present.to.glass.blakenewman.controllers.Server;

public class Presenter extends Activity {

    static public Activity context;

    public static Server server;
    public static Client client;
    private static Card card;

    private static Boolean isAwake = true;

    private static Timer screenTimer = new Timer();
    private static TimerTask screenTask = new TimerTask() {

        @Override
        public void run() {
            WindowManager.LayoutParams params = context.getWindow().getAttributes();
            params.screenBrightness = 0;
            context.getWindow().setAttributes(params);
            isAwake = false;
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = this;
        card = new Card(this);
        server = new Server();
        client = new Client();
    }


    public static void update(String content, Boolean stream, Long time){
        wake();
        card.setText(content);
        context.setContentView(card.getView());

        if(!stream && time.equals(Long.getLong("0"))){
            screenTimer.schedule(screenTask, time);
        }
    }

    public static void wake(){
        screenTask.cancel();
        screenTimer.purge();
        if(isAwake) return;
        WindowManager.LayoutParams params = context.getWindow().getAttributes();
        params.screenBrightness = -1;
        context.getWindow().setAttributes(params);
        isAwake = true;
    }
}
