package cl.tofcompany.sift.Controllers;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import cl.tofcompany.sift.R;

public class testpago extends AppCompatActivity {
    Button btnMakePayment;
    ProgressBar progressBar;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testpago);
       
    }


}