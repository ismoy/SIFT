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
        //llamar la clase de MyToolbar y lo damos la accion show y de cimos en esta actividad y ponemos un titulo
        //y activamos la opcion para volver atras
        MyToolbar.show(this,"",true);
    }
}