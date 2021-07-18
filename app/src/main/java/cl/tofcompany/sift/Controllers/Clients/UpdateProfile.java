package cl.tofcompany.sift.Controllers.Clients;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

import cl.tofcompany.sift.Model.Client;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.ClientProvider;
import cl.tofcompany.sift.Providers.ImagesProvider;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.Utils.FileUtil;
import cl.tofcompany.sift.includes.MyToolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfile extends AppCompatActivity {
    //variable CircleImageView
    private CircleImageView miImageViewprofile;
    //variable btn
    private Button mButtonupdate;
    //variable Textview name
    private TextView mTextViewname;
    //variable Textview password
    private TextView mTextViewpassword;
    //variable clase ClientProvider
    private ClientProvider mClientProvider;
    //variable clase AuthProvider
    private AuthProvider mAuthProvider;
    //variable clase File
    private File mimagefile;
    //variable imagen
    private String mimage;
    //variable cuando para acceder al galeria lo iniciamos en 1
    private final int GALLERY_REQUEST = 1;
    //progressDialog
    private ProgressDialog mProgressDialog;
    //variable de name
    private String mname;
    //variable clase ImagesProvider
    private ImagesProvider mImageProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        miImageViewprofile = findViewById(R.id.imageviewprofileclient);
        mButtonupdate = findViewById(R.id.btnupdateprofile);
        mTextViewname = findViewById(R.id.actualizarname);
        mTextViewpassword = findViewById(R.id.actualizarpassword);
        MyToolbar.show(this,"",true);
        mClientProvider = new ClientProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImagesProvider("client_images");
        mProgressDialog = new ProgressDialog(this);
        getClientInfo();
        mButtonupdate.setOnClickListener(v ->
                updateProfile());
        miImageViewprofile.setOnClickListener(v ->
                openGallery());
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== GALLERY_REQUEST && resultCode == RESULT_OK) {
            try {
                mimagefile = FileUtil.from(this, data.getData());
                miImageViewprofile.setImageBitmap(BitmapFactory.decodeFile(mimagefile.getAbsolutePath()));
            } catch(Exception e) {
                Log.d("ERROR", "Mensaje: " +e.getMessage());
            }
        }
    }

    private void getClientInfo() {
        mClientProvider.getClient(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String password = dataSnapshot.child("password").getValue().toString();
                    String password2 = dataSnapshot.child("password2").getValue().toString();
                    String image = "";
                    if (dataSnapshot.hasChild("image")) {
                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(miImageViewprofile);
                    }
                    mTextViewname.setText(name);
                    mTextViewpassword.setText(password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateProfile() {
        mname = mTextViewname.getText().toString();
        if (!mname.equals("") && mimagefile != null) {
            mProgressDialog.setMessage("Espere un momento...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            saveImage();
        }
        else {
            Toast.makeText(this, "Ingresa la imagen y el nombre", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        mImageProvider.saveImage(UpdateProfile.this, mimagefile, mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        String image = uri.toString();
                        Client client = new Client();
                        client.setImage(image);
                        client.setName(mname);
                        client.setId(mAuthProvider.getId());
                        mClientProvider.update(client).addOnSuccessListener(aVoid -> {
                            mProgressDialog.dismiss();
                            Toast.makeText(UpdateProfile.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                        });
                    });
                }
                else {
                    Toast.makeText(UpdateProfile.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}