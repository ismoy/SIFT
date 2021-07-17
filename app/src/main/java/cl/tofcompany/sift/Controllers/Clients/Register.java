package cl.tofcompany.sift.Controllers.Clients;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.regex.Pattern;

import cl.tofcompany.sift.Controllers.Logins.Login;
import cl.tofcompany.sift.Model.Client;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.ClientProvider;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class Register extends AppCompatActivity {

    //definamos cada campos del activity con sus nombres
    EditText musername, mEmail, mPassword, mrepetpassword;
    Button mRegisterBtn;
    private ProgressDialog mDialog;
    /*sharedpreferences es para saber cual usuario que estan registrando para guardarlo en BD*/
    SharedPreferences mPreferences;
    //authprovider viene de la clase AuthProvider
    AuthProvider mAuthProvider;
    //Clientprovider viene de la clase ClientProvider
     ClientProvider mClientProvider;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mPreferences = getApplication().getSharedPreferences("typeUser",MODE_PRIVATE);
        //asignamos cada campos con sus respectivos id
        musername = findViewById(R.id.username);
        mEmail = findViewById(R.id.correo);
        mPassword = findViewById(R.id.password);
        mrepetpassword = findViewById(R.id.repeatpassword);
        mRegisterBtn = findViewById(R.id.btn_registrar);
        mDialog = new ProgressDialog(this);
        mAuthProvider =  new AuthProvider();
        mClientProvider = new ClientProvider();
        fAuth = FirebaseAuth.getInstance();
        MyToolbar.show(this,"Register",true);
        mRegisterBtn.setOnClickListener(v -> clickRegister());


    }

    private void clickRegister() {
        //definemos los variables de cada campo para poder validarlos
        final String name = musername.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();
        final String password2 = mrepetpassword.getText().toString().trim();
        //Validamos el campo nombre para que no esta vacio
        if (TextUtils.isEmpty(name)) {
            musername.setError(getString(R.string.Requiredfield));
            return;
        }
        //Validamos que solo pueden igresar letra
        if (!validarletras(name)) {
            musername.setError(getString(R.string.Onlyallowletters));
            return;
        }
        //Validamos que el campo nombre debe tener minimum 6 caracteres
        if (name.length() <= 6) {
            musername.setError(getString(R.string.Pleaseenteryourfullname));
            return;
        }
        //Validamos el campo email para que no esta vacio
        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.Email_is_required_));
            return;
        }
        if (!validaremail(email)) {
            mEmail.setError(getString(R.string.invalid_email));
            return;
        }
        //Validamos el campo password para que no esta vacio
        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.Password_is_required_));
            return;
        }
        //Validamos el campo confirmar password para que no esta vacio
        if (TextUtils.isEmpty(password2)) {
            mrepetpassword.setError(getString(R.string.Password_is_required_));
            return;
        }
        //Validamos el campo password para que la contraseña sea minimum 6 caracteres
        if (password.length() < 6) {
            mPassword.setError(getString(R.string.The_password_must_be_at_least_6_characters_long));
            return;
        }
        //Validamos que el campo confirmar contraseña sea igual al contraseña ingresado anteriormente
        if (password.equals(password2)) {
            mDialog.setMessage(getString(R.string.Loading___));
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
            //despues de validar los campos enviamos los datos en la realtime database de una vez
            register(name,email,password,password2);
        } else {
            mrepetpassword.setError(getString(R.string.Passwordsdonotmatch));
        }
       /* Intent i = new Intent(this , MainActivity.class);
        startActivity(i);*/
    }

    void register(final String name,final String email,String password,String password2) {
        mAuthProvider.register(email , password).addOnCompleteListener(task -> {
            mDialog.dismiss();
            //aseguramos que el processo sea exitoso y enviamos mensaje al usuario al contrario enviamos error
            if (task.isSuccessful()) {
                String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                Client client = new Client(id,name,email,password,password2);
                create(client);
                    // si la tarrea fue exitosa enviamos un correo de confirmation al usuario para verificar su cuenta
                    FirebaseUser user = fAuth.getCurrentUser();
                    fAuth.setLanguageCode("es");
                    assert user != null;
                    user.sendEmailVerification().addOnCompleteListener(task1 -> Toast.makeText(Register.this , R.string.Youraccounthasbeencreatedsuccessfully_Checkyouremailtoactivatetheaccount , Toast.LENGTH_LONG).show()).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String TAG = "";
                            Log.d(TAG , R.string.onFailureWecouldnotverifyyouremail + e.getMessage());
                        }

                    });

            } else {
                Toast.makeText(Register.this , R.string.Error + Objects.requireNonNull(task.getException()).getMessage() , Toast.LENGTH_LONG).show();
            }
        });
    }
void create(Client client){
        mClientProvider.create(client).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String user = mPreferences.getString("user","");
                if (user.equals("client")){
                    Intent intent = new Intent(Register.this, Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(Register.this, Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }else {
                Toast.makeText(Register.this, R.string.Error + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
}
    //metodo para guardar automaticamente en realtime database
    /*public void saveUser(String id, String name, String email,String password,String password2) {
        String selectedUser = mPreferences.getString("user","");
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(password);
        user.setPassword2(password2);
        if (selectedUser.equals("driver")) {
            database.child("Users").child("Drivers").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(Register.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else if (selectedUser.equals("client")){
            database.child("Users").child("Clients").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(Register.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }
*/



    //Validacion regex para los campos
    public static boolean validarletras(String datos) {
        return datos.matches("[a-zA-Z ]*");
    }

    //Validacion Patterns para el campo email
    private boolean validaremail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
}







