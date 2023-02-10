package com.example.findmybus;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;


import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BusRegistrationActivity extends AppCompatActivity{
    private static final String TAG = "BusRegistrationActivity";

    private TextInputLayout regBusNo;
    private TextInputLayout regDestinationOne;
    private TextInputLayout regDestinationSecond;
    private TextInputLayout regEmail;
    private TextInputLayout regPassword;
    private TextView regAddStops;

    private FirebaseFirestore mDb;
    private String mBusId;

    private GeoApiContext mGeoApiContext = null;
//    private Pair<LatLng,LatLng> locationPair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_registration);

        mDb = FirebaseFirestore.getInstance();

        if(mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.Google_map_API_key))
                    .build();
        }

        regBusNo = findViewById(R.id.regBusNo);
        regDestinationOne = findViewById(R.id.regDestinationOne);
        regDestinationSecond = findViewById(R.id.regDestinationSecond);
        regEmail = findViewById(R.id.regEmail);
        regAddStops = findViewById(R.id.regAddStops);
        regPassword = findViewById(R.id.regPassword);
        regAddStops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
        Button regCreateBtn = findViewById(R.id.regCreateBtn);
        regCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String busNo = Objects.requireNonNull(regBusNo.getEditText()).getText().toString();
                String destinationOne = Objects.requireNonNull(regDestinationOne.getEditText()).getText().toString();
                String destinationSecond = Objects.requireNonNull(regDestinationSecond.getEditText()).getText().toString();
                String email = Objects.requireNonNull(regEmail.getEditText()).getText().toString();
                String password = Objects.requireNonNull(regPassword.getEditText()).getText().toString();

                if(!TextUtils.isEmpty(destinationOne) || !TextUtils.isEmpty(destinationSecond)){
//                    alertBox(busNo + "\n" + destinationOne+ "\n" + destinationSecond+ "\n" + email+ "\n" + password,"Details");
//                    Toast.makeText(BusRegistrationActivity.this, busNo + "\n" + destinationOne+ "\n" + destinationSecond+ "\n" + email+ "\n" + password, Toast.LENGTH_SHORT).show();
                    registerUser(busNo, destinationOne, destinationSecond, email, password);
                }else{
                    Toast.makeText(BusRegistrationActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUser(String busNo, String destinationOne, String destinationSecond, String email, String password) {

        Pair<GeoPoint,GeoPoint> locationPair = geoLocate(destinationOne,destinationSecond);
        if(locationPair != null){
//            calculateDirections(locationPair);
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task) {
                    mBusId = FirebaseAuth.getInstance().getUid();
                    Bus bus = new Bus(mBusId,busNo,"null",email,destinationOne,destinationSecond,locationPair.first,locationPair.second );
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                    mDb.setFirestoreSettings(settings);

                    DocumentReference newBus = mDb.collection("Buses").document(mBusId);
                    newBus.set(bus).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if(task.isSuccessful()){
                                DocumentReference newUserLocationRef = mDb.collection("Buses Locations").document(mBusId);
                                newUserLocationRef.set(new BusLocation(null,null)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            redirectLoginScreen();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });

        }else{
            Toast.makeText(this, "Result is False", Toast.LENGTH_SHORT).show();
        }

//        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    mUserId = FirebaseAuth.getInstance().getUid();
//                    User user = new User(name,email,mUserId);
//                    UserLocation userLocation = new UserLocation(null,null);
//
//                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
//                    mDb.setFirestoreSettings(settings);
//
//                    DocumentReference newUserRef = mDb.collection("Users").document(mUserId);
//
//                    newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful()){
//                                DocumentReference newUserLocationRef = mDb.collection("Users Locations").document(mUserId);
//                                newUserLocationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if(task.isSuccessful()){
//                                            redirectLoginScreen();
//                                        }
//                                    }
//                                });
//                            }else{
//                                View parentLayout = findViewById(android.R.id.content);
//                                Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                }else {
//                    View parentLayout = findViewById(android.R.id.content);
//                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    private Pair<GeoPoint,GeoPoint> geoLocate(String destinationOne, String destinationSecond){
        Geocoder geocoder = new Geocoder(BusRegistrationActivity.this);
        List<Address> list1 = new ArrayList<>();
        List<Address> list2 = new ArrayList<>();
        try{
            list1 = geocoder.getFromLocationName(destinationOne, 1);
        }catch (IOException e){
            Log.e(TAG, "GeoLocate: IOException: " + e.getMessage() );
        }
        try{
            list2 = geocoder.getFromLocationName(destinationSecond, 1);
        }catch (IOException e){
            Log.e(TAG, "GeoLocate: IOException: " + e.getMessage() );
        }
        Address address1 = null;
        Address address2 = null;

        if(list1.size() > 0){
            address1 = list1.get(0);
        }else{
            alertBox(destinationOne + " Not Found","Sorry!!");
        }

        if(list2.size() > 0){
            address2 = list2.get(0);
        }else{
            alertBox(destinationSecond + " Not Found","Sorry!!");
        }

        if(list1.size()>0 && list2.size()>0){
            assert address1 != null;
            GeoPoint origin = new GeoPoint(address1.getLatitude(), address1.getLongitude());
            assert address2 != null;
            GeoPoint destination = new GeoPoint(address2.getLatitude(), address2.getLongitude());
            //            alertBox(locationPair.first.toString() + " " + locationPair.second.toString(),"Message");
//            Toast.makeText(BusRegistrationActivity.this, locationPair.first.toString() + " " + locationPair.second.toString(), Toast.LENGTH_LONG).show();
            return new Pair<>(origin,destination);
        }
        return null;
    }

//    private void calculateDirections(Pair<LatLng,LatLng> locationPair){
//        Log.d(TAG, "CalculateDirections: Calculating Directions.");
//
//        if(mGeoApiContext == null) {
//            mGeoApiContext = new GeoApiContext.Builder()
//                    .apiKey(getString(R.string.Google_map_API_key))
//                    .build();
//        }
//
//        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
//        directions.alternatives(true);
//        directions.origin(locationPair.first);
//        directions.destination(locationPair.second);
//
//        try{
//            DirectionsResult res = directions.await();
//            if (res.routes != null && res.routes.length > 0) {
//                DirectionsRoute route = res.routes[0];
//                Toast.makeText(BusRegistrationActivity.this,route.toString(), Toast.LENGTH_LONG).show();
//            }
//        } catch(Exception ex) {
//            Log.e(TAG, ex.getLocalizedMessage());
//        }
////                .setCallback(new PendingResult.Callback<DirectionsResult>() {
////            @Override
////            public void onResult(DirectionsResult result) {
////                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
////                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
////                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
////                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
////
////                alertBox(result.routes[0].toString(),"Direction");
////                Toast.makeText(BusRegistrationActivity.this, locationPair.first.toString() + " " + locationPair.second.toString(), Toast.LENGTH_LONG).show();
////
////                Toast.makeText(BusRegistrationActivity.this, result.routes[0].toString()
////                        + " " + result.routes[0].legs[0].duration
////                        + " " + result.routes[0].legs[0].distance
////                        + " " + result.geocodedWaypoints[0].toString(), Toast.LENGTH_LONG).show();
////
////                Log.d(TAG, "OnResult:Successfully retrieved directions.");
//////                addPolylinesToMap(result);
////            }
////            @Override
////            public void onFailure(Throwable e) {
////                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );
////            }
////        });
//    }

    public void alertBox(String message, String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(BusRegistrationActivity.this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) -> {dialog.cancel(); });
        builder.create().show();
    }

    private void redirectLoginScreen() {
        Intent intent = new Intent(BusRegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}