package in.ownmanager.googlemapsapisample;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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
 * */
public class MapActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private static final String TAG = "MapActivity";
    private final int LOCATION_PERMISSION_CODE = 1;
    GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;

    public static boolean permissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    15f);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "Location permission: " + e.getMessage());
        }
    }

    // Move camera in map
    private void moveCamera(LatLng lat_Lon, float zoom) {
        Log.d(TAG, "moving Camera lat: " + lat_Lon.latitude + " lon: " + lat_Lon.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat_Lon, zoom));
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

}
