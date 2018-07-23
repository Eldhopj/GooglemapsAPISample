package in.ownmanager.googlemapsapisample;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
/**Commit 1:
 *      Add Maps Fragment into xml
 *      Check permission granted or not
 *      Initialize map
 *
 * Commit 2: Get device location
 *      Getting device location
 *      putting device location in map
 *
 * Commit 3:
 *      Added search functionality
 *      Added marker
 *      Added find location button
 *      Added functionality to refreshCurrentLocation
 * */
public class MapActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private static final String TAG = "MapActivity";
    private final int LOCATION_PERMISSION_CODE = 1;
    GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    EditText searchBar;

    public static final int DEFAULT_ZOOM = 15;

    public static boolean permissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        searchBar = findViewById(R.id.search);

        locationPermission();
    }

    /**Initialize map*/
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Log.d(TAG, "initMap: ");
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "MapReady");
                mMap = googleMap;
                if (permissionsGranted) {
                    getLocation(); //getting your location and showing it on map
                    try {
                        searchBarfun();
                        // shows your location with a blue dot and setLocation icon on map
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(false); // removes the setLocation icon from map , because it will block by search bar

                    } catch (SecurityException e) {
                        Log.d(TAG, "Location permission: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void getLocation() {
        Log.d(TAG, "Get device current location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext()); // Initialize fusedLocationProviderClient
        try {
            if (permissionsGranted) { // Checking if the permission is granted or not
                final com.google.android.gms.tasks.Task<Location> location = fusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) { // checks whether the task is successful or not
                            Log.d(TAG, "getting last location successful");
                            Location currentLocation = task.getResult(); // gets the current last known location

                            //Move the camera into the current location results
                            if (currentLocation != null) {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM,
                                        "My Location");
                            } else {
                                refreshCurrentLocation();
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "Location permission: " + e.getMessage());
        }
    }

    // Move camera in map
    private void moveCamera(LatLng lat_Lon, float zoom, String title) {
        Log.d(TAG, "moving Camera lat: " + lat_Lon.latitude + " lon: " + lat_Lon.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat_Lon, zoom));

        if (!title.equals("My Location")) { // in order to not put marker on my current location , already blue dot is present in there
            MarkerOptions marker = new MarkerOptions()
                    .position(lat_Lon)
                    .title(title);
            mMap.addMarker(marker);
        }
    }

    public void currentLocation(View view) { // For the GPS button on the map
        getLocation();
    }

    //permission checks starts here
    @AfterPermissionGranted(LOCATION_PERMISSION_CODE)
    private void locationPermission() { //Note : This method must be void and cant able to take any arguments
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE}; //Array of permission
        if (EasyPermissions.hasPermissions(this, perms)) { //check permission is granted or not
            //code if permission is granted
            initMap();
            permissionsGranted = true;
        } else {
            EasyPermissions.requestPermissions(this, "Permission needed for map functionality",
                    LOCATION_PERMISSION_CODE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        locationPermission();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){
            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(this,"Welcome Back", Toast.LENGTH_SHORT)
                    .show();
        }
    }
    //permission checks ends here


    private void searchBarfun() {
        //when presses enter button on keyboard it accepts the value ( like pressing submit button)
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if ((keyEvent != null &&
                        (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    geoSearch();
                }
                return false;
            }
        });
    }

    //geo searching
    private void geoSearch() {
        String search = searchBar.getText().toString();
        Geocoder geocoder = new Geocoder(getApplicationContext());//Geocoding is the process of transforming a street address or other description of a location into a (latitude, longitude) coordinate and reverse also

        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(search, 1);
        } catch (IOException e) {
            Log.d(TAG, "geoSearch error: " + e.getMessage());
        }
        if (addressList.size() > 0) {
            Address address = addressList.get(0);
            Log.d(TAG, "geoSearch: Found address" + address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                    DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    private void refreshCurrentLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
    }

    // function to hide keyboard
    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
