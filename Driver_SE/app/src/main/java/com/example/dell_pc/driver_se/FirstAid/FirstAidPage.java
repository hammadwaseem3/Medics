package com.example.dell_pc.driver_se.FirstAid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.dell_pc.driver_se.R;

public class FirstAidPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_aid_page);
    }

    public void Choking_Aid(View view)
    {

        if(view.getId()==R.id.Choking_button)
        {
            Intent i = new Intent(FirstAidPage.this , Choking.class);
            startActivity(i);
        }
    }

    public void Electric_shock_Aid(View view)
    {

        if(view.getId()==R.id.Electric_shock_Button)
        {
            Intent i = new Intent(FirstAidPage.this , ElectricShock.class);
            startActivity(i);
        }
    }

    public void Burns_Aid(View view)
    {

        if(view.getId()==R.id.Burns_Button)
        {
            Intent i = new Intent(FirstAidPage.this , Burns.class);
            startActivity(i);
        }
    }

    public void Drowning_Aid(View view)
    {

        if(view.getId()==R.id.Drowning_Button)
        {
            Intent i = new Intent(FirstAidPage.this , Drowning.class);
            startActivity(i);
        }
    }

    public void Snake_bite_Aid(View view)
    {

        if(view.getId()==R.id.Snake_bite_button)
        {
            Intent i = new Intent(FirstAidPage.this , SnakeBit.class);
            startActivity(i);
        }
    }

    public void Cuts_Aid(View view)
    {

        if(view.getId()==R.id.Cuts_Button)
        {
            Intent i = new Intent(FirstAidPage.this , Cuts.class);
            startActivity(i);
        }
    }
}
