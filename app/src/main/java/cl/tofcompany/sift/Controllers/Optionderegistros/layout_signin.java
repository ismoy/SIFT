package cl.tofcompany.sift.Controllers.Optionderegistros;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import cl.tofcompany.sift.Controllers.Clients.Register;
import cl.tofcompany.sift.Controllers.Drivers.RegisterDriverActivity;
import cl.tofcompany.sift.Controllers.Logins.Login;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class layout_signin extends AppCompatActivity {
 private Button email,phone,google,registrar;
    SharedPreferences mPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_signin);

        email=findViewById(R.id.btn_iniciar_sign_in);
        phone = findViewById(R.id.btn_phone_sign_in);
        google = findViewById(R.id.btn_google_sign_in);
        registrar = findViewById(R.id.btn_registrar);
        mPreferences = getApplication().getSharedPreferences("typeUser",MODE_PRIVATE);
        MyToolbar.show(this,"",true);
        email.setOnClickListener(v -> gotologin());

        phone.setOnClickListener(v -> gotophoneregister());
        registrar.setOnClickListener(v -> gotoregister());
    }

    private void gotoregister() {
        String typeUser = mPreferences.getString("user","");
        if (typeUser.equals("client")){
            Intent intent = new Intent(layout_signin.this, Register.class);
            startActivity(intent);
        }else {
            Intent intent = new Intent(layout_signin.this, RegisterDriverActivity.class);
            startActivity(intent);
        }

    }

    private void gotophoneregister() {
       /* Intent intent = new Intent(layout_signin.this, Login.class);
        startActivity(intent);*/
    }

    private void gotologin() {
        Intent intent = new Intent(layout_signin.this, Login.class);
        startActivity(intent);
    }


}