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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.ownmanager.googlemapsapisample.ModelClass.PlacesModel;
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
 * Commit 4:
 *      implement GoogleApiClient.OnConnectionFailedListener
 *      Codes for Google places API autoCompleteSuggestions
 * */
public class MapActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    /**Note to get location update when location changes (Continuously checks location look GoogleLocationAPINEW)*/
    private static final String TAG = "MapActivity";
    private final int LOCATION_PERMISSION_CODE = 1;
    GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    AutoCompleteTextView searchBar;
    PlaceAutocompleteAdapter placeAutocompleteAdapter;
    protected GeoDataClient mGeoDataClient;
    private GoogleApiClient mGoogleApiClient;

    private PlacesModel placesModel;

    public static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));

    public static final int DEFAULT_ZOOM = 15;

    public static boolean permissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        searchBar = findViewById(R.id.search);

        locationPermission();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
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
                final Task<Location> location = fusedLocationProviderClient.getLastLocation();

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
                                refreshCurrentLocation();  // if there is no last saved location on the phone
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


    private void searchBarfun() {

        mGeoDataClient = Places.getGeoDataClient(getApplicationContext()); //provides access to Google's database of local place and business information
        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(getApplicationContext(),
                mGeoDataClient, LAT_LNG_BOUNDS, null); //passing values into AutocompleteAdapter
        searchBar.setAdapter(placeAutocompleteAdapter); // set Adapter into search bar

        searchBar.setOnItemClickListener(autoCompleteClickListener);
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

    /**Fetches location from the GPS for one time*/
    private void refreshCurrentLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
    }

    // function to hide keyboard
    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    //---------------Google places API autoCompleteSuggestions----------------//
    private AdapterView.OnItemClickListener autoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // we need to get the place id and we need to submit the request to places geo data API to retrieve the place object

            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(position); // Get the item
            final String placeID; // Get the place ID
            placeID = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi //submit the request
                    .getPlaceById(mGoogleApiClient, placeID); // we can submit a list of place id
            placeResult.setResultCallback(updatePlaceDetailsCallbacks); // submits a request
        }
    };

    //This callback interface will give the place object we are looking for
    private ResultCallback<PlaceBuffer> updatePlaceDetailsCallbacks = new ResultCallback<PlaceBuffer>() { // create callback
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.d(TAG, "place query unsuccessful : " + places.getStatus().getStatusMessage());
                places.release(); // must release the place in order to prevent the memory leaks
                return;
            }
            final Place place = places.get(0);//Place object contains info like address, website, phone number....

            try {
                placesModel = new PlacesModel();
                //you can get anything you need from places object  some are given below
                placesModel.setName(place.getName().toString()); // save data into model class inorder to prevent loosing it while releasing the object
                Log.d(TAG, "onResult: name: " + place.getName());
                placesModel.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
                placesModel.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                placesModel.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                placesModel.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                placesModel.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                placesModel.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + placesModel.toString());
            } catch (NullPointerException e) {
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage());
            }
            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, placesModel.getName());

            places.release();// release the object inorder to prevent memory leak
            // We Successfully save data's in a model class, so releasing of object wont effect
        }
    };
    //---------------Google places API autoCompleteSuggestions----------------//

    //-------------------------------------------permission checks-----------------------------------------------//
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
    //-------------------------------------------permission checks-----------------------------------------------//

}
