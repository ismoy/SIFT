package cl.tofcompany.sift.Controllers.Drivers;

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

import cl.tofcompany.sift.Model.Driver;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.DriverProvider;
import cl.tofcompany.sift.Providers.ImagesProvider;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.Utils.FileUtil;
import cl.tofcompany.sift.includes.MyToolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileDriver extends AppCompatActivity {
    //variable CircleImageView
    private CircleImageView mImageViewProfile;
    //variable btn
    private Button mButtonUpdate;
    //variable Textview name
    private TextView mTextViewName;
    //variable Textview marca
    private TextView mTextViewBrandVehicle;
    //variable Textview placa
    private TextView mTextViewPlateVehicle;
    //variable de la clase DriverProvider
    private DriverProvider mDriverProvider;
    //variable de la clase AuthProvider
    private AuthProvider mAuthProvider;
    //variable de la clase ImagesProvider
    private ImagesProvider mImageProvider;
    //variable objeto File para los archivos
    private File mImageFile;
    //variable para guardar el imagen
    private String mImage;
    //variable cuando para acceder al galeria lo iniciamos en 1
    private final int GALLERY_REQUEST = 1;
    //progressDialog
    private ProgressDialog mProgressDialog;
    //variable de name
    private String mName;
    //variable  mVehicleBrand
    private String mVehicleBrand;
    //variable  mVehiclePlate
    private String mVehiclePlate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_driver);
        //llamado del toolbar
        MyToolbar.show(this, "", true);
        //iniciamos el image con su id
        mImageViewProfile = findViewById(R.id.imageViewProfiledriver);
        //iniciamos el btn con su id
        mButtonUpdate = findViewById(R.id.btnUpdateProfile);
        //iniciamos el name con su id
        mTextViewName = findViewById(R.id.textInputName);
        //iniciamos el marca vehiculo con su id
        mTextViewBrandVehicle = findViewById(R.id.textInputVehicleBrand);
        //iniciamos el placa vehiculo con su id
        mTextViewPlateVehicle = findViewById(R.id.textInputVehiclePlate);
        //variable de la clase DriverProvider
        mDriverProvider = new DriverProvider();
        //variable de la clase AuthProvider
        mAuthProvider = new AuthProvider();
        //variable de la clase ImagesProvider apuntamos al nodo driver_images
        mImageProvider = new ImagesProvider("driver_images");
        //variable progressDialog
        mProgressDialog = new ProgressDialog(this);
        //metodo para tomar el info del driver
        getDriverInfo();
        //agregamos un evento onclick en el buton update
        mImageViewProfile.setOnClickListener(view ->
                //metodo para actualizar
                openGallery());
        //aplicamos un evento onclick sobre el imagen de perfil
        mButtonUpdate.setOnClickListener(view ->
                //para abrir la galeria y seleccionar otra foto
                updateProfile());
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
        //asegurar que el requesCode es igual a mi Gallery_request y si el resultado es igual a ok
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== GALLERY_REQUEST && resultCode == RESULT_OK) {
            try {
                //a mi variable mimagefile lo pasamos el metodo de FileUtil y ejecutamos el from
                //recibe un contexto que es this y luego sacamos la data que llego
                mImageFile = FileUtil.from(this, data.getData());
                //eso me va a decodificar el imager y me lo va a coprimir para no ocupa muchos espacios
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch(Exception e) {
                Log.d("ERROR", "Mensaje: " +e.getMessage());
            }
        }
    }
    //metodo para tomar los infos del cliente
    private void getDriverInfo() {
        //llamamos la clase DriverProvider y ejecutamos el metodo getDriver eso recibe un id
        //lo tenemos en mAuthProvider y luego lo pasamos un evento de firebase addListenerForSingleValueEvent
        mDriverProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguremos que el datos exist
                if (dataSnapshot.exists()) {
                    //creamos un variable name y lo pasamos el valor proviene de firebase mediante
                    //dataSnapshot.child("name")
                    String name = dataSnapshot.child("name").getValue().toString();
                    //creamos un variable vehiculebrand y lo pasamos el valor proviene de firebase mediante
                    //dataSnapshot.child("name")
                    String vehicleBrand = dataSnapshot.child("vehicleBrand").getValue().toString();
                    //creamos un variable vehiculeplate y lo pasamos el valor proviene de firebase mediante
                    //dataSnapshot.child("name")
                    String vehiclePlate = dataSnapshot.child("vehiclePlate").getValue().toString();
                    //creamos un variable image lo iniciamos en vacio
                    String image = "";
                    //aseguramos que el nodo image en firebas tiene imagen
                    if (dataSnapshot.hasChild("image")) {
                        //mi image lo pasamos el valor que proviene de firebase mediante dataSnapshot.child("image")
                        image = dataSnapshot.child("image").getValue().toString();
                        //al final lo procesamos el imagen con picasso y luego enviarlo en la vista
                        Picasso.get().load(image).into(mImageViewProfile);
                    }
                    //enviar el name en la vista
                    mTextViewName.setText(name);
                    //enviar la marcaauto en la vista
                    mTextViewBrandVehicle.setText(vehicleBrand);
                    //enviar la placa en la vista
                    mTextViewPlateVehicle.setText(vehiclePlate);

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
        mName = mTextViewName.getText().toString();
        //tomamos el valor que esta dentro en editText
        mVehicleBrand = mTextViewBrandVehicle.getText().toString();
        //tomamos el valor que esta dentro en editText
        mVehiclePlate = mTextViewPlateVehicle.getText().toString();
        //aseguremos que no esta vacio tb en imagefile
        if (!mName.equals("") && mImageFile != null) {
            //mensaje del dialog
            mProgressDialog.setMessage("Espere un momento...");
            //desactivamos la opcion del Touch para que el usuario no pueda cancelar esta operacion
            mProgressDialog.setCanceledOnTouchOutside(false);
            //muestramos el dialog
            mProgressDialog.show();
            //guardamos los datos si todos estan ok
            saveImage();
        }
        else {
            Toast.makeText(this, "Ingresa la imagen y el nombre", Toast.LENGTH_SHORT).show();
        }
    }
    //metodo para guardar image y nombre despues de modificarlo
    private void saveImage() {
        //entramos en la clase de ImageProvider ejecutamos el metodo de saveImage eso recibe un contexto
        //en mi caso sera this, tb recibe um image lo tenemos en mimagefile, tb recibe un id
        //lo tenemos en mAuthProvider
        mImageProvider.saveImage(UpdateProfileDriver.this, mImageFile, mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                //pregunta si la tarea fue exitosa
                if (task.isSuccessful()) {
                    //llamamos la clase ImageProvider ejecutamos el metodo getStorage y el getDownloadUrl()
                    //eso recibe el uri como parametro
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        //guardamos en image el uri
                        String image = uri.toString();
                        //llamamos la clase del modelo driver
                        Driver driver = new Driver();
                        //enviamos el imagen en el image
                        driver.setImage(image);
                        //enviamos el nombre en el name
                        driver.setName(mName);
                        //enviamos el nombre en el id eso lo tenemos en mAuthProvider
                        driver.setId(mAuthProvider.getId());
                        //enviamos el nombre en el marca lo tenemos mVehicleBrand
                        driver.setVehicleBrand(mVehicleBrand);
                        //enviamos el nombre en el placa lo tenemos mVehicleBrand
                        driver.setVehiclePlate(mVehiclePlate);
                        //entramos en la clase de DriverProvider y ejecutamos el metodo update eso recibe client por parametro
                        mDriverProvider.update(driver).addOnSuccessListener(aVoid -> {
                            //ocultar dialog
                            mProgressDialog.dismiss();
                            //mensaje exito
                            Toast.makeText(UpdateProfileDriver.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                        });
                    });
                }
                else {
                    //mensaje error
                    Toast.makeText(UpdateProfileDriver.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}