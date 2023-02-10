package com.example.findmybus;

import static android.content.ContentValues.TAG;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;

import org.checkerframework.checker.nullness.qual.NonNull;

public class UserMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap map;
    private String mBusId;
    private FirebaseFirestore mDb;
    private static FirebaseUser currentUser;

    private final Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map);

        mBusId = getIntent().getStringExtra("bus");
        mDb = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.user_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
    }
    @Override
    public void onResume() {
        super.onResume();
        startBusLocationsRunnable(); // update user locations every 'LOCATION_UPDATE_INTERVAL'
    }

    @Override
    public void onPause() {
        stopLocationUpdates(); // stop updating user locations
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }


    private void getLocation(String busId){

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);

        DocumentReference busLocationRef = FirebaseFirestore.getInstance()
                .collection("Buses Location").document(busId);

        busLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    BusLocation busLocation = task.getResult().toObject(BusLocation.class);
                    assert busLocation != null;
                    LatLng updatedLatLng = new LatLng(
                            busLocation.getGeopoint().getLatitude(),
                            busLocation.getGeopoint().getLongitude()
                    );
                    Toast.makeText(UserMapActivity.this, updatedLatLng.latitude +  "," + updatedLatLng.longitude, Toast.LENGTH_SHORT).show();
                    map.addMarker(new MarkerOptions().position(updatedLatLng).icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_baseline_directions_bus_24)));
//                    setCameraView(updatedLatLng);
                }
            }
        });

//        mDb.collection("Buses Location").addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                Log.d(TAG, "onEvent: called.");
//
//                if (e != null) {
//                    Log.e(TAG, "onEvent: Listen failed.", e);
//                    return;
//                }
//                BusLocation bus;
//                if(queryDocumentSnapshots != null){
//                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                        bus = doc.toObject(BusLocation.class);
//
//                    }
//                }
//
//
//            }
//        });
    }

    private void startBusLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                getLocation(mBusId);
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void setCameraView(LatLng mBusPosition) {

        // Set a boundary to start
        //TODO
        double bottomBoundary = mBusPosition.latitude- .1;
        double leftBoundary = mBusPosition.longitude - .1;
        double topBoundary = mBusPosition.latitude + .1;
        double rightBoundary = mBusPosition.longitude + .1;
        LatLngBounds mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());
        saveUserLocation(geoPoint);
    }

//    private void calculateDirections(Marker marker){
//        Log.d(TAG, "calculateDirections: calculating directions.");
//
//        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
//                marker.getPosition().latitude,
//                marker.getPosition().longitude
//        );
//        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
//
//        directions.alternatives(true);
//        directions.origin(
//                new com.google.maps.model.LatLng(
//                        mUserPosition.getGeo_point().getLatitude(),
//                        mUserPosition.getGeo_point().getLongitude()
//                )
//        );
//        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
//        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
//            @Override
//            public void onResult(DirectionsResult result) {
////                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
////                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
////                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
////                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
//
//                Log.d(TAG, "onResult: successfully retrieved directions.");
//                addPolylinesToMap(result);
//            }
//
//            @Override
//            public void onFailure(Throwable e) {
//                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );
//
//            }
//        });
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

}



























