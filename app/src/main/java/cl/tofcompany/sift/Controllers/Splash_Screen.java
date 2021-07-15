package cl.tofcompany.sift.Controllers;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import cl.tofcompany.sift.R;


public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash__screen);


        //duration splash screen
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(Splash_Screen.this);

            @Override
            public void run() {
                if (user != null && account != null) {
                    Intent intent = new Intent(Splash_Screen.this , Login.class);
                    startActivity(intent);
                    finish();
                } else {
                    startActivity(new Intent(Splash_Screen.this , MainActivity.class));
                    finish();
                }

            }
        } , 4000);
    }

}





