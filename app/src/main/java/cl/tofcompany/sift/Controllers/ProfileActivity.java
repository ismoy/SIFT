package cl.tofcompany.sift.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import cl.tofcompany.sift.R;


public class ProfileActivity extends AppCompatActivity {

    private ImageView imgprofile;
    private TextView txtusername, txtemail;
    private Button btnlogout;
    FirebaseUser mUser;
    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.profile);
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        imgprofile = findViewById(R.id.imgprofile);
        txtusername = findViewById(R.id.txtusername);
        txtemail = findViewById(R.id.txtemail);
        btnlogout = findViewById(R.id.btnlogout);
        updateUi();

        btnlogout.setOnClickListener(v -> Alertsignout());

    }

    public void updateUi() {
        if (mUser != null) {
            txtusername.setText(mUser.getDisplayName());
            txtemail.setText(mUser.getEmail());
            if (mUser.getPhotoUrl() != null) {
                String mypicture = mUser.getPhotoUrl().toString();
                mypicture = mypicture + "?type=large";
                Picasso.get().load(mypicture).into(imgprofile);
            }

        } else {

            txtusername.setText("");
            txtemail.setText("");
            imgprofile.setImageResource(R.drawable.logodefault);

        }
    }

    //metodo para cerrar session con alert
    public void Alertsignout() {
        AlertDialog.Builder alertDialog2 = new
                AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog2.setTitle(R.string.Singintocontinue);

        // Setting Dialog Message
        alertDialog2.setMessage(R.string.confirm_signout);

        // Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton(R.string.yes ,
                (dialog , which) -> {
                    // Write your code here to execute after dialog
                    mAuth.getInstance().signOut();
                   // LoginManager.getInstance().logOut();
                    finish();
                    Intent i = new Intent(getApplicationContext() ,
                            MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                });

        // Setting Negative "NO" Btn
        alertDialog2.setNegativeButton(R.string.no ,
                (dialog , which) -> {
                    // Write your code here to execute after dialog
                    Toast.makeText(getApplicationContext() ,
                            R.string.noaction , Toast.LENGTH_SHORT)
                            .show();
                    dialog.cancel();
                });

        // Showing Alert Dialog
        alertDialog2.show();
    }
}
