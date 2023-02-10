package com.example.findmybus;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.checkerframework.checker.nullness.qual.NonNull;

public class BusMapActivity extends AppCompatActivity implements OnMapReadyCallback , LocationListener {

    private GoogleMap map;

    private FirebaseFirestore mDb;
    private static FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_map);

        mDb = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

    }

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());
        saveUserLocation(geoPoint);
    }

    private void saveUserLocation(GeoPoint geoPoint){
        if(geoPoint != null){
            DocumentReference locationRef = mDb.collection("Buses Locations")
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
    }
}