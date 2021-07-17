package cl.tofcompany.sift.Controllers.Feedback;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import cl.tofcompany.sift.R;


public class FeedbackActivity extends AppCompatActivity {

    private TextView estado;
    private EditText txtdudas, editsubject, mail;
    private Button btnsend;
    private RatingBar ratingBar;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.feedback);
        // Enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));

        estado = findViewById(R.id.estado);
        txtdudas = findViewById(R.id.txtdudas);
        btnsend = findViewById(R.id.btnsend);
        ratingBar = findViewById(R.id.ratingBar);
        editsubject = findViewById(R.id.editsubject);
        mail = findViewById(R.id.mail);

        ratingBar.setOnRatingBarChangeListener((ratingBar , rating , fromUser) -> {
            if ( rating == 0.5) {
                estado.setText(R.string.very_disatisfied);
            } else if (rating == 1 || rating == 1.5) {
                estado.setText(R.string.disatisfied);
            } else if (rating == 2 || rating == 2.5) {
                estado.setText(R.string.Satisfied);
            } else if (rating == 3 || rating == 3.5) {
                estado.setText(R.string.Very_Satisfied);
            } else if (rating == 4 || rating == 4.5) {
                estado.setText(R.string.The_Best_App);
            } else {
                estado.setText(R.string.I_love_it);
            }
        });


        btnsend.setOnClickListener(v -> {

            String email = mail.getText().toString();
            String subject = editsubject.getText().toString().trim();
            String message = txtdudas.getText().toString().trim();


            if (TextUtils.isEmpty(subject)) {
                editsubject.setError("subject requiered");
                return;
            }
            if (TextUtils.isEmpty(message)) {
                txtdudas.setError("Message Feedback requiered.");
                return;
            }

            String[] email_divide = email.split(",");
            Intent send = new Intent(Intent.ACTION_SEND);
            send.putExtra(Intent.EXTRA_EMAIL , email_divide);
            send.putExtra(Intent.EXTRA_SUBJECT , subject);
            send.putExtra(Intent.EXTRA_TEXT , message + " " + ratingBar.getRating());
            send.setType("message/rfc822");
            send.setPackage("com.google.android.gm");
            startActivity(send);
        });

    }
}