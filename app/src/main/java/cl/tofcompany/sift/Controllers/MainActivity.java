package cl.tofcompany.sift.Controllers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import cl.tofcompany.sift.Controllers.Clients.MapsClientActivity;
import cl.tofcompany.sift.Controllers.Drivers.MapsDriverActivity;
import cl.tofcompany.sift.R;


public class MainActivity extends AppCompatActivity {

    Button mbtncliente, mbtnconductor;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //capturamos cada campos con sus respectivo id
        mbtncliente = findViewById(R.id.btn_soy_cliente);
        mbtnconductor = findViewById(R.id.btn_soy_conductor);
        mPreferences = getApplicationContext().getSharedPreferences("typeUser",MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        mbtncliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user", "client");
                editor.apply();
                gotoselectoroptions();
            }
        });

        mbtnconductor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user", "driver");
                editor.apply();
                gotoselectoroptions();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() !=null){
            String user = mPreferences.getString("user","");
            if (user.equals("client")){
                Intent intent = new Intent(MainActivity.this, MapsClientActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else{
                Intent intent = new Intent(MainActivity.this, MapsDriverActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    private void gotoselectoroptions() {
        Intent intent = new Intent(MainActivity.this, layout_signin.class);
        startActivity(intent);
    }

}