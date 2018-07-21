package in.ownmanager.googlemapsapisample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
/**Commit 1:
 *      Add Maps Fragment into xml
 *      Check permission granted or not
 *      Initialize map
 * */
public class MapActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private static final String TAG = "MapActivity";
    private final int LOCATION_PERMISSION_CODE = 1;
    GoogleMap mMap;

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
                mMap = googleMap;
                Log.d(TAG, "MapReady");
            }
        });
    }

    /**Checking permissions granted or not*/
    @AfterPermissionGranted(LOCATION_PERMISSION_CODE)
    private void locationPermission() { //Note : This method must be void and cant able to take any arguments
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE}; //Array of permission
        if (EasyPermissions.hasPermissions(this, perms)) { //check permission is granted or not
            //code if permission is granted
            initMap();
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
}
