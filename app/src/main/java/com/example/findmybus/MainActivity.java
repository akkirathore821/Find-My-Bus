package com.example.findmybus;

import static android.content.ContentValues.TAG;
import static android.location.LocationManager.GPS_PROVIDER;
import static com.example.findmybus.Constants.ERROR_DIALOG_REQUEST;
import static com.example.findmybus.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.findmybus.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.findmybus.Constants.PERMISSION_FINE_LOCATION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity{

    private RecyclerView mBusesRecyclerView;
    private BusesRecyclerAdapter mBusRecyclerAdapter;
    private ListenerRegistration mBusesEventListener;
    private ArrayList<Bus> mBuses = new ArrayList<>();

    private FirebaseFirestore mDb;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    public static FirebaseUser currentUser;
    private ProgressBar mProgressBar;
    private TextInputLayout mFrom;
    private TextInputLayout mTo;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDb = FirebaseFirestore.getInstance();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        isBus();

//        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //todo
//        mProgressBar = (ProgressBar) findViewById(R.id.mainProgressBar);
        mBusesRecyclerView = findViewById(R.id.buses_recycler_view);

        mFrom = findViewById(R.id.from);
        mTo = findViewById(R.id.to);

        Button mSearchBtn = findViewById(R.id.search);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String from = Objects.requireNonNull(mFrom.getEditText()).getText().toString();
                String to = Objects.requireNonNull(mTo.getEditText()).getText().toString();

                if(!TextUtils.isEmpty(from) || !TextUtils.isEmpty(to)){
                    mBuses = new ArrayList<>();
                    searchBuses(from,to);
                    initBusesRecyclerView();
                }else{
                    Toast.makeText(MainActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }

            }
        });


//        Toolbar mToolbar = findViewById(R.id.mainToolBar);
//        setSupportActionBar(mToolbar);
//        Objects.requireNonNull(getSupportActionBar()).setTitle("FindMyBus");
    }

    private void isBus() {
        CollectionReference BusIdCollection = mDb.collection("Buses");
//        String curUid = currentUser;
        BusIdCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                boolean isBus = false;
                if(value != null){
                    for (QueryDocumentSnapshot doc : value) {
                        Bus bus = doc.toObject(Bus.class);
                        if(bus.getBusId().equals(currentUser.toString())){
                            isBus = true;
                            break;
                        }
                    }

                }
                if(isBus){
                    sendtoBusMap();
                }
            }
        });
    }

    private void sendtoBusMap() {
        startActivity(new Intent(this,BusMapActivity.class));
    }

    private void sendtoUserMap() {
        startActivity(new Intent(this,UserMapActivity.class));
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if (currentUser == null) {
            sendToLoginActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                //Todo After the permission is granted
//                getLastKnownLocation();
            }
            else{
                getLocationPermission();
            }
        }
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            return isMapsEnabled();
        }
        return false;
    }

    private void sendToLoginActivity() {
        Intent startIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.mainLogoutBtn) {
            FirebaseAuth.getInstance().signOut();
            sendToLoginActivity();
        }
//        if(item.getItemId() == R.id.mainAccountSettingsBtn){
//            Intent accountSettingIntent = new Intent(MainActivity.this, SettingsActivity.class);
//            startActivity(accountSettingIntent);
//        }
//        if(item.getItemId() == R.id.mainAllUserBtn){
//            Intent allUsersIntent = new Intent(MainActivity.this, UsersActivity.class);
//            startActivity(allUsersIntent);
//        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    public boolean isServicesOK() {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //every thing is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            assert dialog != null;
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        if (requestCode == PERMISSIONS_REQUEST_ENABLE_GPS) {
            if (mLocationPermissionGranted) {
                //Todo After the permission is granted
//                getLastKnownLocation();
            } else {
                getLocationPermission();
            }
        }

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
//            Todo After the permission is granted
//            getLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
//
//    private void getLastKnownLocation() {

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
//            mLocationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, (LocationListener) this);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            mFusedLocationProviderClient.getCurrentLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//                    if (location == null) {
//                        Toast.makeText(MainActivity.this, "Location is null", Toast.LENGTH_LONG).show();
//                    } else {
//                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());
//                        saveUserLocation(geoPoint);
//
//                    }
//                }
//            });
//        }
//        else {
//            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
//            }
//        }
//
//    }

    private void saveUserLocation(GeoPoint geoPoint){
        if(geoPoint != null){
            DocumentReference locationRef = mDb.collection("Users Locations")
                    .document(currentUser.getUid());
            UserLocation userLocation = new UserLocation(geoPoint,null);
            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG,"latitude: " + userLocation.getGeoPoint().getLatitude() +
                                "\nlongitude: " + userLocation.getGeoPoint().getLongitude());
                    }
                }
            });
        }
    }

    private void searchBuses(String from, String to){

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);

        String f = from.toLowerCase();
        String t = to.toLowerCase();

        CollectionReference BusCollection = mDb.collection("Buses");
        mBuses.clear();

        BusCollection.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "onEvent: called.");

                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if(queryDocumentSnapshots != null){
                    ArrayList<Bus> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(Bus.class));
                    }

                    for (Bus bu : list) {
                        String[] stops = bu.getRoute().split("-");
                        int a = -1;
                        int b = -1;
                        for(int i = 0; i < stops.length; i++){
                            if(stops[i].equals(f))      a = i;
                            else if(stops[i].equals(t))   b = i;
                            if(a != -1 && b != -1)  break;
                        }
                        if(a != -1 && b != -1 && a < b){
                            mBuses.add(bu);
                        }
                    }

                    Log.d(TAG, "onEvent: number of Buses: " + mBuses.size());
                    mBusesRecyclerView.removeAllViewsInLayout();
                    mBusRecyclerAdapter.notifyDataSetChanged();
                }

            }
        });
    }

    private void initBusesRecyclerView(){
        mBusRecyclerAdapter = new BusesRecyclerAdapter(mBuses, this);
        mBusesRecyclerView.setAdapter(mBusRecyclerAdapter);
        mBusesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


}