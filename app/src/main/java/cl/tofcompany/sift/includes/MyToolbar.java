package cl.tofcompany.sift.includes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import cl.tofcompany.sift.R;

public class MyToolbar {
    public  static void show(AppCompatActivity activity, String title, boolean upbutton){
       Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upbutton);
    }
}
