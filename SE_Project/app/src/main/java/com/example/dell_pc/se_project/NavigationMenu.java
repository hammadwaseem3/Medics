package com.example.dell_pc.se_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.dell_pc.se_project.FirstAid.About_Us;
import com.example.dell_pc.se_project.FirstAid.FirstAidPage;
import com.example.dell_pc.se_project.FirstAid.Help;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NavigationMenu extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference myRef ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_menu);

        Button firstAidButton = (Button) findViewById(R.id.firstAidButton);
        firstAidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent FirstAidActivity = new Intent(NavigationMenu.this, FirstAidPage.class);
                startActivity(FirstAidActivity);
            }
        });

        Button aboutUs = (Button) findViewById(R.id.aboutUsbutton);
        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent about_us_activity = new Intent(NavigationMenu.this, About_Us.class);
                startActivity(about_us_activity);
            }
        });

        Button help = (Button) findViewById(R.id.helpButton);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent help_activity = new Intent(NavigationMenu.this, Help.class);
                startActivity(help_activity);
            }
        });



        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        final SharedPreferences.Editor editor = pref.edit();


        Button logout_btn = (Button) findViewById(R.id.logOutButton);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor.putString("UserName", "");
                editor.putString("Password" , "") ;
                editor.commit();


                database = FirebaseDatabase.getInstance();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                String uid = user.getUid();
                myRef = database.getReference("Ambulance/"+uid+"/availability");
                myRef.setValue("false");

                Intent MainActivity = new Intent(NavigationMenu.this,MainActivity.class);
                startActivity(MainActivity);
            }
        });
    }
}
