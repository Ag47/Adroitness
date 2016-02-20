package xyz.adroitness.adroitness;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

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

import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BeaconManager beaconManager;
    private String scanId;
    private BeaconListAdapter adapter;

    // TODO connection beacon 2&3
    private BeaconConnection connection1;
    private BeaconConnection connection2;
    private BeaconConnection connection3;
    private Beacon beacon1 = null;
    private Beacon beacon2 = null;
    private Beacon beacon3 = null;

    boolean firstConnection;

    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstConnection = true;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
                    if (motionState == MotionState.MOVING)
                        Log.i("motion", "In Motion");
                    else
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
}
