package com.example.dell_pc.driver_se;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private LocationManager locationMangaer = null;
    private LocationListener locationListener = null;
    private double longitudeAmbulance;
    private double latitudeAmbulance;
    private FirebaseDatabase database;
    private FirebaseDatabase m_database;
    private DatabaseReference myRef ;
    private DatabaseReference m_Ref;
    ProgressDialog mapProgressDialog;
    private Marker markerForAmbulance;
    private Marker markerForPeople;
    private String user_lat;
    private String user_long;
    private String idForPeople;
    private DatabaseReference refForPeople;
    private ValueEventListener listenerForPeople;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        final TextView nameOfUser = (TextView) findViewById(R.id.nameofUser);
        final TextView numOfUser = (TextView) findViewById(R.id.numofUser);
        final Button end_ride = (Button) findViewById(R.id.end_ride);


        nameOfUser.setVisibility(View.GONE);
        numOfUser.setVisibility(View.GONE);
        end_ride.setVisibility(View.GONE);

        mapProgressDialog = new ProgressDialog(MapsActivity.this,0);
        mapProgressDialog.setMessage("Loading");
        mapProgressDialog.setCanceledOnTouchOutside(false);
        mapProgressDialog.show();

        mapFragment.getMapAsync(this);
        locationMangaer = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        ImageButton navBarButton = (ImageButton) findViewById(R.id.navBarButton);
        navBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, NavigationMenu.class));
                //Toast.makeText(MapsActivity.this, "Nav menu",
                  //      Toast.LENGTH_SHORT).show();
            }
        });

        database = FirebaseDatabase.getInstance();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        m_Ref = database.getReference("Ambulance/"+uid+"/req_user_id") ;
        m_Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String req_user_id = dataSnapshot.getValue().toString();
                Log.e("Maps","req_user_id: "+req_user_id);

                if ( !req_user_id.equals("null") )
                {
                    Log.v("Maps","In the accept area(IF)");
                    database.getReference("Ambulance/"+uid+"/availability").setValue("false");
                    database.getReference("Users/"+req_user_id+"/driver").setValue(uid);
                    idForPeople=req_user_id;
                    myRef = database.getReference("Users/"+req_user_id);
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {

                            String user_name = dataSnapshot1.child("name").getValue().toString() ;
                            String user_no = dataSnapshot1.child("num").getValue().toString() ;
                            user_lat = dataSnapshot1.child("Lat").getValue().toString() ;
                            user_long = dataSnapshot1.child("Long").getValue().toString() ;

                            LatLng userLocation = new LatLng(Double.parseDouble(user_lat), Double.parseDouble(user_long));
                            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.peoplemarker);

                            markerForPeople = mMap.addMarker(new MarkerOptions().position(userLocation).icon(icon).title("Passenger"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                            updateMarkerForPeople(req_user_id);


                            nameOfUser.setText(user_name);
                            numOfUser.setText(user_no);


                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                            final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            try
                            {
                                r.play();
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }


                            new android.support.v7.app.AlertDialog.Builder(MapsActivity.this)
                            .setTitle("Request Message")
                            .setMessage("!!... Passenger is waiting for You ...!!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    r.stop();
                                    r.stop();
                                    r.stop();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    end_ride.setVisibility(View.VISIBLE);
                    nameOfUser.setVisibility(View.VISIBLE);
                    numOfUser.setVisibility(View.VISIBLE);

                }
                else
                {
                    if(markerForPeople != null)
                        markerForPeople.remove();
                    Log.v("Maps","In the accept area(ELSE)");
                    database.getReference("Ambulance/"+uid+"/availability").setValue("true");
                    end_ride.setVisibility(View.GONE);
                    nameOfUser.setVisibility(View.GONE);
                    numOfUser.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Button end_rid = (Button) findViewById(R.id.end_ride);
        end_rid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

                builder.setTitle("Confirm");
                builder.setMessage("Are you sure ?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        end_ride.setVisibility(View.GONE);
                        nameOfUser.setVisibility(View.GONE);
                        numOfUser.setVisibility(View.GONE);

                        String uid = user.getUid();
                        Log.v("Maps","In the cancel area.. ID= "+uid);

                        m_Ref = database.getReference("Ambulance/"+uid+"/availability");
                        m_Ref.setValue("true");

                        if(idForPeople != null)
                            database.getReference("Users/"+idForPeople+"/driver").setValue("null");

                        database.getReference("Ambulance/"+uid+"/req_user_id").setValue("null");
                        if(markerForPeople != null)
                            markerForPeople.remove();

                        refForPeople.removeEventListener(listenerForPeople);
                        idForPeople=null;
                        markerForPeople=null;
                        user_long=null;
                        user_lat=null;
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }

    private void updateMarkerForPeople(String req_user_id){

        listenerForPeople = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String Lat= dataSnapshot.child("Lat").getValue().toString();
                String Long= dataSnapshot.child("Long").getValue().toString();

                if((user_lat != null && user_long != null) && (!Lat.equals(user_lat) || !Long.equals(user_long))){
                    user_lat = Lat;
                    user_long = Long;
                    LatLng userLocation = new LatLng(Double.parseDouble(user_lat), Double.parseDouble(user_long));
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.peoplemarker);

                    if(markerForPeople == null) {
                        markerForPeople = mMap.addMarker(new MarkerOptions().position(userLocation).icon(icon).title("Passenger"));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    }
                    else {
                        markerForPeople.remove();
                        markerForPeople = mMap.addMarker(new MarkerOptions().position(userLocation).icon(icon).title("Passenger"));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        refForPeople =database.getReference("Users/"+req_user_id);
        refForPeople.addValueEventListener(listenerForPeople);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
        moveTaskToBack(true);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        if (displayGpsStatus()) {

            Log.v(TAG, "onClick");


            locationListener = new MyLocationListener();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                locationMangaer.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            }
            else{
                Toast.makeText(this,"Kindly Provide Location Acces",Toast.LENGTH_SHORT).show();
            }

            Log.v(TAG,latitudeAmbulance +" , "+longitudeAmbulance);

        } else {
            Toast.makeText(this,"GPS not available!!",Toast.LENGTH_SHORT).show();
        }

    }

    /*----Method to Check GPS is enable or disable ----- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {

            String longitude = "" +loc.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "" +loc.getLatitude();
            Log.v(TAG, latitude);

    /*----------to get City-Name from coordinates ------------- */
            String cityName=null;
            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc
                        .getLongitude(), 1);
                if (addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());
                cityName=addresses.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String s = longitude+"\n"+latitude +
                    "\n\nMy Currrent City is: "+cityName;
            longitudeAmbulance=loc.getLongitude();
            latitudeAmbulance=loc.getLatitude();

            LatLng userLocation = new LatLng(latitudeAmbulance, longitudeAmbulance);
            if(markerForAmbulance == null) {
                markerForAmbulance = mMap.addMarker(new MarkerOptions().position(userLocation).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("unnamed", 200, 200))).title("Your Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
            else {
                markerForAmbulance.remove();
                markerForAmbulance = mMap.addMarker(new MarkerOptions().position(userLocation).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("unnamed", 200, 200))).title("Your Current Location"));
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Name, email address, and profile photo Url
                String uid = user.getUid();
                myRef = database.getReference("Ambulance/"+uid+"/Lat");
                myRef.setValue(""+latitude+"");
                myRef = database.getReference("Ambulance/"+uid+"/Long");
                myRef.setValue(""+longitude+"");
            }
            mapProgressDialog.cancel();
        }

        public Bitmap resizeMapIcons(String iconName,int width, int height){
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
            return resizedBitmap;
        }


        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }
}
