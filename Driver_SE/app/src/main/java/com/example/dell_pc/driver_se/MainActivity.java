package com.example.dell_pc.driver_se;


import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dell_pc.driver_se.FirstAid.Help;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {



    private static final String TAG = "MyActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    ProgressDialog myProgressDialog;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef ;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.v(TAG,"Acceeesssss");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {


                    Log.v(TAG,"Deniedddddd");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        final SharedPreferences.Editor editor = pref.edit();


        String email = ((EditText) findViewById(R.id.signin_id)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.signin_password)).getText().toString().trim();


        boolean isFirstRun = pref.getBoolean("FIRSTRUN", true);
        if (isFirstRun)
        {
            // Code to run once
            editor.putString("UserName", "");
            editor.putString("Password" , "") ;
            editor.putString("SaveUserName", "");
            editor.putString("SavePassword" , "") ;
            editor.putBoolean("FIRSTRUN", false);
            editor.commit();
        }

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
        else{

        }


        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }

        };

        Button signUpPage = (Button) findViewById(R.id.goto_signup_page);
        signUpPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signUpActivity = new Intent(MainActivity.this,signUpPageActivity.class);
                startActivity(signUpActivity);
            }
        });

        String session_username = pref.getString("UserName", null);
        String session_password = pref.getString("Password", null);
        if (!session_username.isEmpty() && !session_password.isEmpty())
        {
            myProgressDialog = new ProgressDialog(MainActivity.this,0);
            myProgressDialog.setMessage("Loading");
            myProgressDialog.setCanceledOnTouchOutside(false);
            myProgressDialog.show();

            //Toast.makeText(MainActivity.this,session_username + " " + session_password,Toast.LENGTH_LONG).show();
            signinAccount(session_username, session_password);
            //Intent i = new Intent(MainActivity.this,MainAct.class);
            //startActivity(i);
        }


        String session_saveusername = pref.getString("SaveUserName", null);
        String session_savepassword = pref.getString("SavePassword", null);

        //Toast.makeText(MainActivity.this,session_saveusername+" "+session_savepassword,Toast.LENGTH_SHORT).show();

        if (!session_saveusername.isEmpty() && !session_savepassword.isEmpty())
        {
            EditText t1 = (EditText) findViewById(R.id.signin_id);
            EditText t2 = (EditText) findViewById(R.id.signin_password);
            CheckBox ch = (CheckBox) findViewById(R.id.checkBox2);

            ch.setChecked(true);
            t1.setText(session_saveusername);
            t2.setText(session_savepassword);
        }

        Button signIn = (Button) findViewById(R.id.signin_button);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = ((EditText) findViewById(R.id.signin_id)).getText().toString().trim();
                final String password = ((EditText) findViewById(R.id.signin_password)).getText().toString().trim();
                myProgressDialog = new ProgressDialog(MainActivity.this,0);
                myProgressDialog.setMessage("Loading");
                myProgressDialog.setCanceledOnTouchOutside(false);
                myProgressDialog.show();

                if(!email.isEmpty() && !password.isEmpty()) {

                    CheckBox cb = (CheckBox) findViewById(R.id.checkBox2);
                    if(cb.isChecked())
                    {
                        editor.putString("SaveUserName", email);
                        editor.putString("SavePassword", password);
                        editor.commit();
                        Toast.makeText(MainActivity.this, "Email and Password Saved",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        editor.putString("SaveUserName", "");
                        editor.putString("SavePassword", "");
                        editor.commit();
                    }

                    editor.putString("UserName", email);
                    editor.putString("Password", password);
                    editor.commit();


                    signinAccount(email, password);
                }
                else{
                    Toast.makeText(MainActivity.this, "Email or Password is empty",Toast.LENGTH_SHORT).show();
                }
            }
        });





        Button helpButton = (Button)findViewById(R.id.goto_help_page);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent HelpActivity = new Intent(MainActivity.this, Help.class);
                startActivity(HelpActivity);
            }
        });
    }

    public void signinAccount(String email,String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        myProgressDialog.cancel();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            //Log.w(TAG, "signInWithEmail:failed", task.getException());
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(MainActivity.this, "Invalid email or password",Toast.LENGTH_SHORT).show();

                            } catch(Exception e) {

                                Toast.makeText(MainActivity.this,"Sign in failed (Check Internet Connection)" +
                                        "" ,Toast.LENGTH_SHORT).show();
                                Log.e(TAG, e.getMessage());
                            }


                        }else{

                            database = FirebaseDatabase.getInstance();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = user.getUid();
                            myRef = database.getReference("Ambulance/"+uid+"/availability");
                            myRef.setValue("true");


                            //when successfull login
                            startActivity(new Intent(MainActivity.this,MapsActivity.class));

                            Toast.makeText(MainActivity.this, "Sign in successfull",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    /*

    The email address is badly formatted.
    The password is invalid or the user does not have a password.

    Sign-in Failed: An internal error has occurred. [ WEAK_PASSWORD  ]
     */

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
        moveTaskToBack(true);
    }



}
