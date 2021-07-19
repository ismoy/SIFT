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
       init();
    }

    //aqui iniciamos los variable y las clases
    private void init(){
        //iniciamos el image con su id
        miImageViewprofile = findViewById(R.id.imageviewprofileclient);
        //iniciamos el btn update con su id
        mButtonupdate = findViewById(R.id.btnupdateprofile);
        //iniciamos el name con su id
        mTextViewname = findViewById(R.id.actualizarname);
        //iniciamos el password con su id
        mTextViewpassword = findViewById(R.id.actualizarpassword);
        //llamamos nuestra toolbar
        MyToolbar.show(this,"",true);
        //iniciamos clase ClientProvider
        mClientProvider = new ClientProvider();
        //iniciamos clase AuthProvider
        mAuthProvider = new AuthProvider();
        //iniciamos clase ImageProvider y lo pasamos el nodo client_image
        mImageProvider = new ImagesProvider("client_images");
        //llamado progresDialog
        mProgressDialog = new ProgressDialog(this);
        //metodo para tomar los datos de los clientes
        getClientInfo();
        //agregamos un evento onclick en el buton update
        mButtonupdate.setOnClickListener(v ->
                //metodo para actualizar
                updateProfile());
        //aplicamos un evento onclick sobre el imagen de perfil
        miImageViewprofile.setOnClickListener(v ->
                //para abrir la galeria y seleccionar otra foto
                openGallery());
    }
    //metodo para abrir la galeria y cambiar la foto
    private void openGallery() {
        //creamos un intent de una action_get_content
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        //decimos que el tipo va hacer de todos tipos de imagenes
        galleryIntent.setType("image/*");
        //iniciamos la actividad con el resultado lo pasamos el intent y tb el gallery_request
        startActivityForResult(galleryIntent, GALLERY_REQUEST );
    }

    @Override
    //sobre escribir el metodo de onActivityResult
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //asegurar que el requesCode es igual a mi Gallery_request y si el resultado es igual a ok
        if (requestCode== GALLERY_REQUEST && resultCode == RESULT_OK) {
            try {
                //a mi variable mimagefile lo pasamos el metodo de FileUtil y ejecutamos el from
                //recibe un contexto que es this y luego sacamos la data que llego
                mimagefile = FileUtil.from(this, data.getData());
                //eso me va a decodificar el imager y me lo va a coprimir para no ocupa muchos espacios
                miImageViewprofile.setImageBitmap(BitmapFactory.decodeFile(mimagefile.getAbsolutePath()));
            } catch(Exception e) {
                Log.d("ERROR", "Mensaje: " +e.getMessage());
            }
        }
    }
    //metodo para tomar los infos del cliente
    private void getClientInfo() {
        //llamamos la clase ClientProvider y ejecutamos el metodo getCLient eso recibe un id
        //lo tenemos en mAuthProvider y luego lo pasamos un evento de firebase addListenerForSingleValueEvent
        mClientProvider.getClient(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguremos que el datos exist
                if (dataSnapshot.exists()) {
                    //creamos un variable name y lo pasamos el valor proviene de firebase mediante
                    //dataSnapshot.child("name")
                    String name = dataSnapshot.child("name").getValue().toString();
                    //creamos un variable password y lo pasamos el valor proviene de firebase mediante
                    //dataSnapshot.child("password")
                    String password = dataSnapshot.child("password").getValue().toString();
                    //creamos un variable password2 y lo pasamos el valor proviene de firebase mediante
                    //dataSnapshot.child("password2")
                    String password2 = dataSnapshot.child("password2").getValue().toString();
                    //creamos un variable image lo iniciamos en vacio
                    String image = "";
                    //aseguramos que el nodo image en firebas tiene imagen
                    if (dataSnapshot.hasChild("image")) {
                        //mi image lo pasamos el valor que proviene de firebase mediante dataSnapshot.child("image")
                        image = dataSnapshot.child("image").getValue().toString();
                        //al final lo procesamos el imagen con picasso y luego enviarlo en la vista
                        Picasso.get().load(image).into(miImageViewprofile);
                    }
                    //enviar el name en la vista
                    mTextViewname.setText(name);
                    //enviar el password en la vista
                    mTextViewpassword.setText(password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //metodo para actualizar los datos
    private void updateProfile() {
        //tomamos el valor que esta dentro en editText
        mname = mTextViewname.getText().toString();
        //aseguremos que no esta vacio tb en imagefile
        if (!mname.equals("") && mimagefile != null) {
            //mensaje del dialog
            mProgressDialog.setMessage("Espere un momento...");
            //desactivamos la opcion del Touch para que el usuario no pueda cancelar esta operacion
            mProgressDialog.setCanceledOnTouchOutside(false);
            //mestramos el dialog
            mProgressDialog.show();
            //guardamos los datos si todos estan ok
            saveImage();
        }
        else {
            //mensaje de error
            Toast.makeText(this, "Ingresa la imagen y el nombre", Toast.LENGTH_SHORT).show();
        }
    }
     //metodo para guardar image y nombre despues de modificarlo
    private void saveImage() {
        //entramos en la clase de ImageProvider ejecutamos el metodo de saveImage eso recibe un contexto
        //en mi caso sera this, tb recibe um image lo tenemos en mimagefile, tb recibe un id
        //lo tenemos en mAuthProvider
        mImageProvider.saveImage(UpdateProfile.this, mimagefile, mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                //pregunta si la tarea fue exitosa
                if (task.isSuccessful()) {
                    //llamamos la clase ImageProvider ejecutamos el metodo getStorage y el getDownloadUrl()
                    //eso ricibe el uri como parametro
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        //guardamos en image el uri
                        String image = uri.toString();
                        //llamamos la clase del modelo cliente
                        Client client = new Client();
                        //enviamos el imagen en el image
                        client.setImage(image);
                        //enviamos el nombre en el name
                        client.setName(mname);
                        //enviamos el id en el mAuthProvider
                        client.setId(mAuthProvider.getId());
                        //entramos en la clase de ClientProvider y ejecutamos el metodo update eso recibe client por parametro
                        mClientProvider.update(client).addOnSuccessListener(aVoid -> {
                            //ocultar dialog
                            mProgressDialog.dismiss();
                            //mensaje exito
                            Toast.makeText(UpdateProfile.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                        });
                    });
                }
                else {
                    //,ensaje error
                    Toast.makeText(UpdateProfile.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}