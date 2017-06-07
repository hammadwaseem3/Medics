package com.example.dell_pc.se_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dell-Pc on 4/13/2017.
 */

public class Driver {
    private FirebaseDatabase database;
    private DatabaseReference myRef ;
    private DatabaseReference refForPeople;
    private ValueEventListener listener;
    private String name;
    private String id;
    private String Lat;
    private String Long;
    private String num;
    private String tempName;
    private String tempId;
    private String tempLat;
    private String tempLong;
    private String tempNum;
    private Marker marker;
    private boolean isAssign;

    public Driver(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Ambulance");
        marker=null;
        isAssign=false;

    }

    public boolean getIsAssign() {
        return isAssign;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getLat() {
        return Lat;
    }

    public String getLong() {
        return Long;
    }

    public String getNum() {
        return num;
    }

    public String getIdAfterCall(){
        id=tempId;
        name=tempName;
        Lat=tempLat;
        Long=tempLong;
        num=tempNum;
        return id;
    }

    public String selectBestDriver(final double latUser,final double longUser){




        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d("Driver Class: ", "yhn par agya :P");
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //List<HashMap<String,String>> value = (ArrayList<HashMap<String,String>>)dataSnapshot.getValue();

                double distance = 0;

                    int count=0;
                    double min=Double.MAX_VALUE;
                    Log.d("Driver Class: ",""+ min);



                for(DataSnapshot currentDriver: dataSnapshot.getChildren()){

                    HashMap<String,String> v = (HashMap<String,String>) currentDriver.getValue();
                        if(v.get("availability").equals("true")) {


                            isAssign=true;
                            tempId = v.get("id");
                            tempName = v.get("name");
                            tempLat = v.get("Lat");
                            tempLong = v.get("Long");
                            tempNum = v.get("num");

                            distance = Math.sqrt(
                                    Math.pow(latUser - Double.parseDouble(v.get("Lat")), 2)
                                            +
                                            Math.pow(longUser - Double.parseDouble(v.get("Long")), 2)


                            );
                            Log.d("Driver Class: ", "Distance: " + distance);
                            if (distance < min) {
                                min = distance;
                                tempId = v.get("id");
                                tempName = v.get("name");
                                tempLat = v.get("Lat");
                                tempLong = v.get("Long");
                                tempNum = v.get("num");
                            }


                            count++;
                        }
                    }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Driver Class: ", "Failed to read value.", error.toException());

            }
        });
        id=tempId;
        name=tempName;
        Lat=tempLat;
        Long=tempLong;
        num=tempNum;
        return id;

    }

    /*private Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }*/

    public Marker attachListner(final GoogleMap mMap){
        Log.d("Driver Class: ", "Id: " +id);
        listener= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                HashMap<String,String> value = (HashMap<String,String>)dataSnapshot.getValue();
                Double runtimeLat= Double.parseDouble(value.get("Lat"));
                Double runtimeLong= Double.parseDouble(value.get("Long"));
                LatLng driverLocation = new LatLng(runtimeLat, runtimeLong);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ambulanceicon);
                if (marker == null){

                    marker = mMap.addMarker(new MarkerOptions().position(driverLocation).icon(icon).title("Ambulance"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLocation, 15));
                }
                else{
                    marker.remove();
                    marker = mMap.addMarker(new MarkerOptions().position(driverLocation).icon(icon).title("Ambulance"));

                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Driver Class: ", "Failed to read value.", error.toException());

            }
        };

        refForPeople = database.getReference("Ambulance/"+id);
        refForPeople.addValueEventListener(listener);
        return marker;
    }

    public void assignRequestID(FirebaseUser currentUser) {
        String uid = currentUser.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Ambulance/"+id+"/req_user_id");

        myRef.setValue(uid);
    }

    public void deAttachEverything(){
        if(refForPeople != null && listener != null){
            refForPeople.removeEventListener(listener);
        }

        if(marker != null) {
            marker.remove();
            marker = null;
        }

    }
}
