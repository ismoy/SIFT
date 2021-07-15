package cl.tofcompany.sift.Controllers.Clients;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class ConnectWithSIFT extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_with_sift);
        MyToolbar.show(this,"",true);
    }
}