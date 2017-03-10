package calebpaul.quietpocket.ui;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import calebpaul.quietpocket.R;
import calebpaul.quietpocket.models.Place;
import calebpaul.quietpocket.services.GeofenceTransitionService;
import calebpaul.quietpocket.services.GooglePlacesService;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static calebpaul.quietpocket.R.id.geofence;
import static calebpaul.quietpocket.services.GooglePlacesService.findPlaces;

//TODO - Find where to close realms
//TODO - Add boolean to model
//TODO - drop markers, delete non selected markers from db


public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Realm realm;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private TextView textLat, textLong;
    private MapFragment mapFragment;
    private String queryString;
    private String userLocationString;
    private boolean firstMapLoad = true;
    private ArrayList<Place> allPlaces;
//    private LatLng fenceLimitLatLng;



    private static final String NOTIFICATION_MSG = "QUIET POCKET";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textLat = (TextView) findViewById(R.id.lat);
        textLong = (TextView) findViewById(R.id.lon);

        // initialize db
        Realm.init(this);
        realm = Realm.getDefaultInstance();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConfiguration);

        //THREAD TEST
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            Log.v(TAG, "onCreate() MAIN THREAD");
        } else {
            Log.v(TAG, "onCreate() NOT MAIN THREAD");
        }

        Intent queryIntent = getIntent();
        queryString = queryIntent.getStringExtra("query");
        userLocationString = queryIntent.getStringExtra("location");

        deleteAllFromRealm(); //TODO - Modify/Delete later

        // initialize GoogleMaps
        initGMaps();

        // create GoogleApiClient
        createGoogleApi();
        findPlaces(queryString, userLocationString, new Callback() {
            //TODO - Validate zero response from api
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v(TAG, queryString);
                Log.v(TAG, userLocationString);
                Log.v(TAG, "FAILURE IN findPlaces()");
                e.printStackTrace();
                Log.v(TAG, "end stacktrace print");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.v(TAG, "FindPlaces->()onResponse()");
                Log.v(TAG, "QUERY: " + queryString);

                List<Place> mPlaces = GooglePlacesService.processPlaces(response);

                Log.v(TAG, "ON RESPONSE: "+mPlaces.get(0).getmName() + ": " + mPlaces.get(0).getmLatitude() + ", " + mPlaces.get(0).getmLongitude());

                //THREAD TEST
                if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                    Log.v(TAG, "onResponse() MAIN THREAD");
                } else {
                    Log.v(TAG, "onResponse() NOT MAIN THREAD");
                }

                for (Place place: mPlaces) {
                        Log.v(TAG, "Save to DB loop in onResponse()");
                        savePlaceInDatabase(place);

                }
                dropMarkers();
            }
        });

    }

    private void deleteAllFromRealm() {
        Log.v(TAG, "DELEEEEEEETE!!!");
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                //THREAD TEST
                if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                    Log.v(TAG, "deleteAllFromRealm() MAIN THREAD");
                } else {
                    Log.v(TAG, "deleteAllFromRealm() NOT MAIN THREAD");
                }
                RealmResults<Place> deletedPlaces = realm.where(Place.class).findAll();
                deletedPlaces.deleteAllFromRealm();
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RealmResults<Place> deletedPlacesUiThread = realm.where(Place.class).findAll();
                deletedPlacesUiThread.deleteAllFromRealm();
            }
        });
    }

    private void dropMarkers() {
        final double[] newLat = new double[1];
        final double[] newLng = new double[1];
        //THREAD TEST
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            Log.v(TAG, "dropMarkers() MAIN THREAD");
        } else {
            Log.v(TAG, "dropMarkers() NOT MAIN THREAD");
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                allPlaces = getPlaces();
                Log.v(TAG, "dropMarkers() ->Length of allPlaces: " + String.valueOf(allPlaces.size()));
                for (Place newPlace: allPlaces) {
                    if (newPlace.getmName() == null) {
                        continue;
                    } else {
                        Log.v(TAG, "in dropMarkers() loop");
                        Log.v(TAG, newPlace.getmName());
                        newLat[0] = Double.valueOf(newPlace.getmLatitude());
                        newLng[0] = Double.valueOf(newPlace.getmLongitude());

                        LatLng newLatLng = new LatLng(newLat[0], newLng[0]);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.v(TAG, "dropMarkers()->pre-markerForGeofence() call");
                                markerForGeofence(newLatLng);
                            }
                        });
                    }
                }
            }
        });



    }

    private void savePlaceInDatabase(Place place) {
        Log.v(TAG, "savePlaceInDatabase() ->"+place.getmName());
        String lat = place.getmLatitude();
        String lng = place.getmLongitude();
        String name = place.getmName();

//        Log.v(TAG, "path: "+realm.getPath());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        //THREAD TEST
//                        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
//                            Log.v(TAG, "saveInDB() Runnable MAIN THREAD?");
//                        } else {
//                            Log.v(TAG, "saveInDB() Runnable NOT MAIN THREAD");
//                        }
                        Place savePlace = realm.createObject(Place.class);
                        savePlace.setmLatitude(lat);
                        savePlace.setmLongitude(lng);
                        savePlace.setmName(name);
                    }
                });
            }
        });
    }

    private ArrayList<Place> getPlaces() {
        Log.v(TAG, "getPlaces()");
        ArrayList<Place> newPlaces = new ArrayList<>();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RealmResults<Place> newRealmPlaces = realm.where(Place.class).findAll();
                Log.v(TAG, "Realm length of newRealmPlaces: " + String.valueOf(newRealmPlaces.size()));
                Log.v(TAG, "Realm Object Test: " + String.valueOf(newRealmPlaces.get(0).getmName()));
                newPlaces.addAll(realm.copyFromRealm(newRealmPlaces));
                Log.v(TAG, "Realm length of newPlaces: " + String.valueOf(newRealmPlaces.size()));
            }
        });
//        } finally {
//            if (realm != null) {
//                realm.close();
//            }
//        }
        return newPlaces;
    }

    protected void onDestroy() {
        super.onDestroy();
        realm.close();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        if (client != null) {
            if (client.isConnected()) {
                client.disconnect();
            }
        }
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        realm.close();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        if (client != null) {
            if (client.isConnected()) {
                client.disconnect();
            }
        }

    }

    //    Options Dropdown Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    //DROPDOWN MENU SELECTIONS
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case geofence: {
                startGeofence();
                return true;
            }
            case R.id.clear: {
                clearGeofence();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private final int REQ_PERMISSION = 999;

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        // TODO close app and warn user
    }

    // Initialize GoogleMaps
    private void initGMaps() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        //TODO - Add click behaviour
        Log.d(TAG, "onMapClick("+latLng +")");
        markerForGeofence(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
//        fenceLimitLatLng = marker.getPosition();
        geoFenceLimits.setCenter(marker.getPosition());
        geoFenceMarker.setPosition(marker.getPosition());
        return false;
    }

    private LocationRequest locationRequest;
    // Defined in milliseconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL = 15000;
    private final int FASTEST_INTERVAL = 3000;

    // Start location Updates
    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged [" + location + "]");
        lastLocation = location;
        writeActualLocation(location);
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
        recoverGeofenceMarker(); //TODO - find all where bool
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();
    }

    private void writeActualLocation(Location location) {
        textLat.setText("Lat: " + location.getLatitude());
        textLong.setText("Long: " + location.getLongitude());

        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }

    private Marker locationMarker;

    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if (map != null) {
            if (locationMarker != null)
                locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            float zoom = 16f; //Default zoom is 14f, close view is 18f
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
//            map.animateCamera(cameraUpdate);

            if (firstMapLoad) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(zoom)
                        .bearing(0)
                        .tilt(35)
                        .build();

                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }


        }
    }


    private Marker geoFenceMarker;

    private void markerForGeofence(LatLng latLng) {

        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.smartphone);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .title(title);
        if (map != null) {
            // Remove last geoFenceMarker
//            if (geoFenceMarker != null) {
//                geoFenceMarker.remove();
//            }

            geoFenceMarker = map.addMarker(markerOptions);

        }
    }

    private Marker queryGeoFenceMarker;


    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if (geoFenceMarker != null) {
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            Log.v(TAG, "GEO DEETS: "+geofence.toString());
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    //    private static final long GEO_DURATION = 60 * 60 * 1000; // 1 HR??
    private static final long GEO_DURATION = Geofence.NEVER_EXPIRE; // 1 HR??
    private static final long GEO_LOITER = 8 * 1000;
    private static final String GEOFENCE_REQ_ID = "Quiet Pocket";
    private static final float GEOFENCE_RADIUS = 18.0f; // in meters

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius) {

//        Long timeStampLong = System.currentTimeMillis();
//        String timeStampString = timeStampLong.toString();

        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
//                .setLoiteringDelay() ADD THIS WHEN MORE STABLE
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent; //Lint is wrong, this is definitely assigned.
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null) {
            return geoFencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
//            saveGeofence();
            drawGeofence();
            Log.v(TAG, "Bob Ross");

        } else {
            // inform about fail
            Log.v(TAG, "Didn't work because reasons.");
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;

    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if (geoFenceLimits != null) {
            geoFenceLimits.remove();
        }

        if (geoFenceLimits == null) {
            Log.v(TAG, "NO LIMIT SOULJA");
        } else {
            Log.v(TAG, "GEOFENCE LIMITS EXIST: "+geoFenceLimits.getCenter().toString());
        }

        Log.v(TAG, "drawGeofence()->GEOFENCE MARKER POSITION"+geoFenceMarker.getPosition());
        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = map.addCircle(circleOptions);
    }

    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

    // Saving GeoFence marker with prefs mng
    private void saveGeofence() {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong(KEY_GEOFENCE_LAT, Double.doubleToRawLongBits(geoFenceMarker.getPosition().latitude));
        editor.putLong(KEY_GEOFENCE_LON, Double.doubleToRawLongBits(geoFenceMarker.getPosition().longitude));
        editor.apply();
    }

    // Recovering last Geofence marker
    private void recoverGeofenceMarker() {
        Log.d(TAG, "recoverGeofenceMarker");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        if (sharedPref.contains(KEY_GEOFENCE_LAT) && sharedPref.contains(KEY_GEOFENCE_LON)) {
            double lat = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LAT, -1));
            double lon = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LON, -1));
            LatLng latLng = new LatLng(lat, lon);
            markerForGeofence(latLng);
            drawGeofence();
        }
    }

    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    // remove drawing
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw() {
        Log.d(TAG, "removeGeofenceDraw()");
        if (geoFenceMarker != null)
            geoFenceMarker.remove();
        if (geoFenceLimits != null)
            geoFenceLimits.remove();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
}
