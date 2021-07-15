package cl.tofcompany.sift.Controllers.Drivers;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class AcercadelDriver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acercadel_driver);
        MyToolbar.show(this,"",true);
    }
}