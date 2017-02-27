package calebpaul.quietpocket.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import butterknife.Bind;
import butterknife.ButterKnife;
import calebpaul.quietpocket.R;

public class QueryActivity extends AppCompatActivity
    implements
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener{

    //TODO - Validate form w snackbar feedback

    private static final String TAG = QueryActivity.class.getSimpleName();

    private GoogleApiClient googleApiClient;
    private static Location currentLocation;
    private static String userLocation;
    private static final int REQ_PERMISSION = 999;

    @Bind(R.id.queryEditText) EditText mQueryEditText;
    @Bind(R.id.titleTextView) TextView mTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        ButterKnife.bind(this);

        Typeface ptSans = Typeface.createFromAsset(getAssets(), "fonts/PTSans.ttf");
        mTitleTextView.setTypeface(ptSans);

        createGoogleApi();

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            String queryString = mQueryEditText.getText().toString();
            Intent intent = new Intent(QueryActivity.this, MainActivity.class);
            intent.putExtra("query", queryString);
            intent.putExtra("location", userLocation);
//            Log.v(TAG, userLocation);
            startActivity(intent);
            return true;
        }
        return false;
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
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

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    // Get last known location
    private void getCurrentLocation() {
        String currentLatLong = "";
        Log.d(TAG, "getLastKnownLocation()");
        if ( checkPermission() ) {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( currentLocation != null ) {
                currentLatLong =  currentLocation.getLatitude() +
                        "," + currentLocation.getLongitude();
                userLocation = currentLatLong;
            } else {
                Log.w(TAG, "No location retrieved yet");
//                startLocationUpdates();
            }
        }
        else askPermission();
    }

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(QueryActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                QueryActivity.this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                REQ_PERMISSION
        );
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
