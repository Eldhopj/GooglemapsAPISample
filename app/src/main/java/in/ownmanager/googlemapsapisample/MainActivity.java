package in.ownmanager.googlemapsapisample;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**Commit 1: Google Maps API Setup(parts- 1,2), check permissions and onMapReady
 *      Add google play dependency
 *      Get an API key
 *      Check correct version of Google play service installed
 *      Add permissions to the manifest and meta-data's
 *      check permissions and onMapReady (Map Activity)
 *
 * Commit 2: Get device location (Map Activity)
 *
 * Commit 3: Added search (Map Activity)
 */

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001; // error to handle if the device don't have a correct version of play services

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**Play service version correct or not*/
    public boolean isServicesOK(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()); // check whether play service is available or not
        if (available == ConnectionResult.SUCCESS){
            Log.d(TAG, "Google play services working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){ // check whether updating play services will fix this issue
            Log.d(TAG, "Google play error occurred but can resolve");

            // Google will show an error dialog and solution for it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else {
            Toast.makeText(this, "Play services unavailable", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void maps(View view) {
        if (!isServicesOK()){
//            Toast.makeText(this, "Play services unavailable", Toast.LENGTH_SHORT).show();
           return;
        }
        else {
            Intent intent = new Intent(getApplicationContext(),MapActivity.class);
            startActivity(intent);
        }

    }
}
