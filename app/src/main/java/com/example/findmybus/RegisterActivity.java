package com.example.findmybus;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "RegisterActivity";

    private TextInputLayout mRegDisplayName;
    private TextInputLayout mRegEmail;
    private TextInputLayout mRegPassword;

    private ProgressBar mProgressBar;
    private FirebaseFirestore mDb;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        mProgressBar = (ProgressBar) findViewById(R.id.regProgressBar);

        mRegDisplayName = findViewById(R.id.regName);
        mRegEmail = findViewById(R.id.regEmail);
        mRegPassword = findViewById(R.id.regPassword);
        mDb = FirebaseFirestore.getInstance();

        findViewById(R.id.createBtn).setOnClickListener(this);
        findViewById(R.id.link_registration_for_bus).setOnClickListener(this);


    }

    private void registerUser(String name, String email, String password) {
        showDialog();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    mUserId = FirebaseAuth.getInstance().getUid();
                    User user = new User(name,email,mUserId);
                    UserLocation userLocation = new UserLocation(null,null);

                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                    mDb.setFirestoreSettings(settings);

                    DocumentReference newUserRef = mDb.collection("Users").document(mUserId);
                    newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                DocumentReference newUserLocationRef = mDb.collection("Users Locations").document(mUserId);
                                newUserLocationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            hideDialog();
                                            redirectLoginScreen();
                                        }
                                    }
                                });
                            }else{
                                View parentLayout = findViewById(android.R.id.content);
                                Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                    hideDialog();
                }
            }
        });
    }

    private void redirectLoginScreen() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case (R.id.link_registration_for_bus):{
                Intent intent = new Intent(RegisterActivity.this, BusRegistrationActivity.class);
                startActivity(intent);
                break;
            }
            case (R.id.createBtn):{
                String displayName = Objects.requireNonNull(mRegDisplayName.getEditText()).getText().toString();
                String email = Objects.requireNonNull(mRegEmail.getEditText()).getText().toString();
                String password = Objects.requireNonNull(mRegPassword.getEditText()).getText().toString();

                if(!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password) ){
                    registerUser(displayName, email, password);
                }else{
                    Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}