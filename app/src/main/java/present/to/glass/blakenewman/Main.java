package present.to.glass.blakenewman;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;

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
import android.widget.TextView;

import present.to.glass.blakenewman.controllers.Client;
import present.to.glass.blakenewman.controllers.Server;

public class Main extends Activity implements View.OnClickListener{

    private final Handler mHandler = new Handler();
    private static Handler UIHandler = new Handler(Looper.getMainLooper());

    /** Audio manager used to play system sound effects. */
    private AudioManager audioManager;

    /** Sound pool used to play the game winning/losing sound effects. */
    private SoundPool soundPool;

    private static View view;

    /**
     * Stores the standard margin for a card, which is used when dynamically creating the table
     * rows for the result cards.
     */
    private int cardMargin;

    private static TextView tvIP;

    public static Server server;
    public static Client client;
    public static Activity context;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        cardMargin = (int) getResources().getDimension(R.dimen.card_margin);

        context = this;
        createSplashLayout();
        server = new Server();
        client = new Client();

    }

    public static void createPresenter(){
        Intent intent = new Intent(context, Presenter.class);
        context.startActivity(intent);
        context.finish();
    }

    private void createSplashLayout(){
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = vi.inflate(R.layout.splash, null);
        view.setOnClickListener(this);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        tvIP = (TextView) view.findViewById(R.id.glass_ip);
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
        item.setEnabled(!client.ip.isEmpty());
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
    public void finish(){
        server.destroy();
        super.finish();
    }
}
