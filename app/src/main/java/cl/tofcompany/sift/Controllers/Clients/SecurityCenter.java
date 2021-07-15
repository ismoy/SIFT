package cl.tofcompany.sift.Controllers.Clients;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class SecurityCenter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_center);
        MyToolbar.show(this,"",true);
    }
}