package present.to.glass.blakenewman;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import present.to.glass.blakenewman.controllers.Client;
import present.to.glass.blakenewman.controllers.Server;

public class Main extends Activity implements View.OnClickListener{

    private final Handler mHandler = new Handler();
    private GestureDetector mGestureDetector;

    /** Audio manager used to play system sound effects. */
    static private AudioManager audioManager;

    public static Activity context;

    public static Server server;
    public static Client client;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mGestureDetector = createGestureDetector(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        context = this;
        createSplashLayout();
        server = new Server();
        client = new Client();

    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.SWIPE_DOWN) {
                    finish();
                    return true;
                }
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

    public static void createPresenter(){
        Intent intent = new Intent(context, Presenter.class);
        context.startActivity(intent);
        audioManager.playSoundEffect(Sounds.SUCCESS);
    }

    private void createSplashLayout(){
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.splash, null);
        view.setOnClickListener(this);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        TextView tvIP = (TextView) view.findViewById(R.id.glass_ip);
        tvIP.setText(Net.getIPAddress(true));

        setContentView(view);
    }


    @Override
    public void onClick(View v){
        audioManager.playSoundEffect(Sounds.TAP);
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.getItem(0);
        item.setEnabled(!client.ip.equals(""));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The startPresentation() method starts a new activity, and if we call it directly here then
        // the new activity will start without giving the menu a chance to slide back down first.
        // By posting the call to a handler instead, it will be processed on an upcoming pass
        // through the message queue, after the animation has completed, which results in a
        // smoother transition between activities.
        if (item.getItemId() == R.id.start) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startPresentation();
                }
            });
            return true;
        } else if (item.getItemId() == R.id.exit) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
            return true;
        } else {
            return false;
        }
    }

    private void startPresentation() {
        client.startPresentation();
    }

    @Override
    public void onStop(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                server.destroy();
                client.endConnection();
                audioManager.playSoundEffect(Sounds.DISMISSED);
            }
        }).run();
        /* Delay the killing of the whole app, to make sure all connections are closed*/
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onStop();
    }
}
