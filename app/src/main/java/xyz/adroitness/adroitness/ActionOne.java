package xyz.adroitness.adroitness;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.att.m2x.android.main.M2XAPI;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.cloud.model.BeaconInfo;
import com.estimote.sdk.connection.BeaconConnection;
import com.estimote.sdk.connection.MotionState;
import com.estimote.sdk.connection.Property;
import com.estimote.sdk.exception.EstimoteDeviceException;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class ActionOne extends YouTubeBaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, YouTubePlayer.OnInitializedListener {

    private BeaconManager beaconManager;
    private String scanId;
    private BeaconListAdapter adapter;
    private Thread thread;
    Timer timer;
    // TODO connection beacon 2&3
    private BeaconConnection connection1;
    private BeaconConnection connection2;
    private BeaconConnection connection3;
    private Beacon beacon1 = null;
    private Beacon beacon2 = null;
    private Beacon beacon3 = null;
    double initRighthand, moveRightHand, difference, distance;
    boolean actionDone = false;
    int count = 0;
    boolean firstStart = true;
    boolean firstConnection;
    TextView current;
    ImageView next;
    boolean onClicked = false;

    Button btnIntentActOne;
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    // YouTube player view
    private YouTubePlayerView youTubeView;

    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
// set an enter transition
        getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
// set an exit transition
        getWindow().setExitTransition(new Slide(Gravity.LEFT));

        setContentView(R.layout.activity_one);
        firstConnection = true;
        next = (ImageView) findViewById(R.id.next);
        current = (TextView) findViewById(R.id.current);
        next.setVisibility(View.INVISIBLE);
        current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClicked = true;
                next.setVisibility(View.INVISIBLE);
                current.setText("Action Count: 0");
                count = 0;
            }
        });

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/playfair.otf");
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(tf);

        // inside your activity (if you did not enable transitions in your theme)

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

        beaconManager = new BeaconManager(this);

        // Configure device list.
        adapter = new BeaconListAdapter(this);
        ListView list = (ListView) findViewById(R.id.device_list);
        list.setAdapter(adapter);

        // Configure BeaconManager.
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Note that beacons reported here are already sorted by estimated
                        // distance between device and beacon.
                        adapter.replaceWith(beacons);
                        beacon1 = beacons.get(0);
                        if (firstStart) {
                            initRighthand = Utils.computeAccuracy(beacon1);
                            Log.v("actionOne", "first right hand : " + initRighthand + " " + beacon1.getMacAddress().toString());
                            firstStart = false;
                        }

                        moveRightHand = Utils.computeAccuracy(beacon1);
                        distance = Utils.computeAccuracy(beacon1);
                        if (onClicked) {
                            if (distance < 1.1 && actionDone == false) {
                                actionDone = true;
                                count++;
                                Log.i("Actioncount", Integer.toString(count));
                                current.setText("Action Count: " + count);
                                if (count > 3) {
                                    current.setText("DONE");
                                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.adoitnesstone);
                                    mp.start();
                                    next.setVisibility(View.VISIBLE);
                                }

                            }
                            if (distance > 0.9 && actionDone == true) {
                                actionDone = false;
                            }
                        }

                        Log.v("actionOne", "move right hand : " + moveRightHand);
                        difference = initRighthand - moveRightHand;
                        Log.v("actionOne", "different : " + difference);
                        if (moveRightHand > 0.2 && moveRightHand < 0.3)
//                            Toast.makeText(getApplication(), "Done it", Toast.LENGTH_LONG).show();
                            Log.i("COMPUTE", "Distance: " + Double.toString(Utils.computeAccuracy(beacon1)));
                        if (firstConnection && beacon1 != null) {
                            firstConnection = false;
                            setConnection();
                        }
                    }
                });
            }
        });

        M2XAPI.initialize(getApplicationContext(), "8edf4e632982a3e56ee099c2847c9139");


//        btnIntentActOne = (Button) findViewById(R.id.ActionOne);
//        btnIntentActOne.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, ActivitOne.class));
//
//            }
//        });
        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);

        // Initializing video player with developer key
        youTubeView.initialize(Config.DEVELOPER_KEY, this);
        runOnUiThread(new Runnable() {

            public void run() {


            }

        });


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActionOne.this, ActionTwo.class);
// Pass data object in the bundle and populate details activity.
//                intent.putExtra("CONTACT", contact);
                YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(ActionOne.this, (YouTubePlayerView) youTubePlayerView, "profile");
                startActivity(intent, options.toBundle());
            }
        });

    }

    private void setConnection() {
        connection1 = new BeaconConnection(this, beacon1, new BeaconConnection.ConnectionCallback() {
            @Override
            public void onAuthorized(BeaconInfo beaconInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        statusView.setText("Authorized. Connecting...");
                    }
                });
            }

            @Override
            public void onConnected(BeaconInfo beaconInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        statusView.setText("Connected");
                    }
                });
                // First step after connection is to enable motion detection on beacon. Otherwise no
                // motion notifications will be sent.
                connection1.edit().set(connection1.motionDetectionEnabled(), true).commit(new BeaconConnection.WriteCallback() {
                    @Override
                    public void onSuccess() {
                        // After on beacon connect all values are read so we can read them immediately and update UI.
                        setMotionText(connection1.motionDetectionEnabled().get() ? connection1.motionState().get() : null);
//                        setTemperature(connection.temperature().get());
                        // Motion sensor sends status updates on physical state change.
                        enableMotionListner();
                        // Temperature must be read periodically.
//                        refreshTemperature();
                    }

                    @Override
                    public void onError(EstimoteDeviceException exception) {
//                        showToast("Failed to enable motion detection");
                    }
                });
            }

            @Override
            public void onAuthenticationError(final EstimoteDeviceException exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        statusView.setText("Status: Cannot connect to beacon. \n" +
//                                "Error: " + exception.getMessage() + "\n" +
//                                "Did you change App ID and App Token in DemosApplication?");
                    }
                });
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        statusView.setText("Status: Disconnected from beacon");
                    }
                });
            }
        });
        if (!connection1.isConnected()) {
            connection1.authenticate();
        } else {
            enableMotionListner();

        }
    }

    private void enableMotionListner() {
        connection1.setMotionListener(new Property.Callback<MotionState>() {
            @Override
            public void onValueReceived(final MotionState value) {
                setMotionText(value);
            }

            @Override
            public void onFailure() {
                Log.i("onFailure", "Unable to register motion listener");
            }
        });
    }

    private void setMotionText(final MotionState motionState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (motionState != null) {
                    if (motionState == MotionState.MOVING) {
                        Log.i("motion", "In Motion");
                        count++;
                    } else
                        Log.i("motion", "Not in motion");
                } else {
                    Log.i("motion", "Disabled");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Should be invoked in #onStart.
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                scanId = beaconManager.startNearableDiscovery();
                connectToService();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Should be invoked in #onStop.
        beaconManager.stopEddystoneScanning(scanId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // When no longer needed. Should be invoked in #onDestroy.
        beaconManager.disconnect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void connectToService() {
        adapter.replaceWith(Collections.<Beacon>emptyList());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
            }
        });
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = errorReason.toString();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {

            // loadVideo() will auto play video
            // Use cueVideo() method, if you don't want to play it automatically
            player.loadVideo(Config.YOUTUBE_VIDEO_CODE);

            // Hiding player controls
            player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(Config.DEVELOPER_KEY, this);
        }
    }

    private YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtube_view);
    }
}
