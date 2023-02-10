package com.example.findmybus;

import static android.text.TextUtils.isEmpty;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";
    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    private ProgressBar mProgressBar;
    private FirebaseFirestore mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressBar = findViewById(R.id.loginProgressBar);

        mLoginEmail = findViewById(R.id.logEmail);
        mLoginPassword = findViewById(R.id.logPassword);

        mDb = FirebaseFirestore.getInstance();

        setupFirebaseAuth();

        findViewById(R.id.logBtn).setOnClickListener(this);
        findViewById(R.id.link_register).setOnClickListener(this);
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");
        // User is signed out
        // ...
        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(LoginActivity.this, "Authenticated with: " + user.getEmail(),Toast.LENGTH_SHORT).show();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                    db.setFirestoreSettings(settings);

                    DocumentReference userRef = db.collection("Users")
                            .document(user.getUid());
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: successfully set the user client.");
                                User user = task.getResult().toObject(User.class);
                                ((UserClient) (getApplicationContext())).setUser(user);
                            }
                        }
                    });

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void signIn() {
        String email = Objects.requireNonNull(mLoginEmail.getEditText()).toString();
        String password = Objects.requireNonNull(mLoginPassword.getEditText()).toString();

        if(!isEmpty(email)
                && !isEmpty(Objects.requireNonNull(mLoginPassword.getEditText()).toString())){
            Log.d(TAG, "onClick: attempting to authenticate.");

            showDialog();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            hideDialog();
                            redirectMainScreen();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    hideDialog();
                }
            });
        }else{
            Toast.makeText(LoginActivity.this, "You didn't fill in all the fields.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void redirectMainScreen() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case (R.id.link_register):{
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            }
            case (R.id.logBtn):{
                signIn();
                break;
            }
        }
    }
}