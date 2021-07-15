package cl.tofcompany.sift.Controllers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.regex.Pattern;

import cl.tofcompany.sift.Controllers.Clients.MapsClientActivity;
import cl.tofcompany.sift.Controllers.Drivers.MapsDriverActivity;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class Login extends AppCompatActivity {
    //variable button login
    Button button;
    //variable toolbar
    Toolbar toolbar;
    //variable signIn on firebase
    FirebaseAuth fAuth;
    FirebaseUser mUser;
    //variable signIn on google
    SignInButton btngoogle;
    GoogleSignInClient googleSignInClient;
    public static final int RC_SIGN_IN = 0;
    SharedPreferences mPreferences;

    //definamos los campos de textos
    EditText mEmail, mPassword;
    TextView mtxtforgot, name1, email;
    NavigationView nav_view;
    private ProgressDialog mDialog;
    DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mEmail = findViewById(R.id.correo);
        mPassword = findViewById(R.id.password);
        mDialog = new ProgressDialog(this);
        mtxtforgot = findViewById(R.id.txtforgot);
        button = findViewById(R.id.btn_login);
        mPreferences = getApplication().getSharedPreferences("typeUser",MODE_PRIVATE);
        database = FirebaseDatabase.getInstance().getReference();
        //Initialise Firebase
        fAuth = FirebaseAuth.getInstance();
        fAuth.getCurrentUser();
        fAuth = FirebaseAuth.getInstance();
        MyToolbar.show(this,"SIFT",true);

        button.setOnClickListener(v -> {
            String email = mEmail.getText().toString();
            final String password = mPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                mEmail.setError(Login.this.getString(R.string.Email_is_required_));
                return;
            }
            if (!Login.this.validaremail(email)) {
                mEmail.setError(Login.this.getString(R.string.invalid_email));
                return;
            }

            if (TextUtils.isEmpty(password)) {
                mPassword.setError(Login.this.getString(R.string.Password_is_required_));
                return;
            }
            //Validamos el campo password para que el contraseña sea minimum 6 caracteres
            if (password.length() < 6) {
                mPassword.setError(Login.this.getString(R.string.The_password_must_be_at_least_6_characters_long));
                return;
            }
            mDialog.setMessage(Login.this.getString(R.string.Loading___));
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
            //Iniciar session con FireBase
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                //aseguramos que el processo sea exitoso y enviamos mensaje al usuario al contrario enviamos error
                if (task.isSuccessful()) {
                    //obligamos que el sistema requiere verificacion email para poder entrar
                    if (Objects.requireNonNull(fAuth.getCurrentUser()).isEmailVerified()) {
                        String user = mPreferences.getString("user", "");
                        //capturamos el usuario que shrapreferences nos dijo que esta conectando y luego comparamos en la BD para poder validar si es un client o driver
                        if (user.equals("client")) {
                            database.child("Users").child("Clients").child(fAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Intent intent = new Intent(Login.this, MapsClientActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(Login.this, "No es un usuario permitido", Toast.LENGTH_SHORT).show();
                                        fAuth.signOut();
                                        mDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            database.child("Users").child("Drivers").child(fAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Intent intent = new Intent(Login.this, MapsDriverActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(Login.this, "No es un usuario permitido", Toast.LENGTH_SHORT).show();
                                        fAuth.signOut();
                                        mDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                    } else {
                        Toast.makeText(Login.this, R.string.Account_not_verified_Please_check_youre_mail, Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }
                } else {
                    Toast.makeText(Login.this, R.string.Problemwhenloggingincheckyouremailandpassword, Toast.LENGTH_LONG).show();
                    //en caso que hay problema para registrar desabilitamos el progresbar para no sigue cargando
                    mDialog.dismiss();
                }
            });
        });

        //Google SignIn
      /*  btngoogle.setOnClickListener(v -> signInWithGoogle());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this , gso);
*/
        //metodo para ir en la pagina de olvidar contraseña
        mtxtforgot.setOnClickListener(v -> startActivity(new Intent(getApplicationContext() , ForgotPassword.class)));

       // mnuevacuenta.setOnClickListener(v -> startActivity(new Intent(getApplicationContext() , Register.class)));

        // Initialize Facebook Login button
       /* mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email" , "public_profile");
        loginButton.registerCallback(mCallbackManager , new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });*/
    }

   /* @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }*/


    //metodo para el acceso al token de facebook
   /* private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this , task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = fAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(Login.this , R.string.Authenticationfailed_ ,
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }*/

    /*private void updateUI(FirebaseUser user1) {
        if (user1 != null) {
            String user = mPreferences.getString("user","");
            if (user.equals("client")){
                Toast.makeText(Login.this , R.string.User_was_successfully_logged_in , Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Login.this, MapsClientActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else{
                Toast.makeText(Login.this , R.string.User_was_successfully_logged_in , Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Login.this, MapsDriverActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        } else {

            Toast.makeText(this , R.string.Singintocontinue , Toast.LENGTH_SHORT).show();
        }
    }
    */


    //Metodo para hacer accion del boton de google
    /*private void signInWithGoogle() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent , RC_SIGN_IN);
    }*/

    @Override
    protected void onActivityResult(int requestCode , int resultCode , @Nullable Intent data) {
        //solo mcallback eso es para facebook
       // mCallbackManager.onActivityResult(requestCode , resultCode , data);
        super.onActivityResult(requestCode , resultCode , data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(Login.this , R.string.Failedtoconnectwithgoogle , Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken , null);
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this , task -> {
                    if (task.isSuccessful()) {
                        String user = mPreferences.getString("user","");
                        if (user.equals("client")){
                            Toast.makeText(Login.this , R.string.User_was_successfully_logged_in , Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, MapsClientActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else{
                            Toast.makeText(Login.this , R.string.User_was_successfully_logged_in , Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, MapsDriverActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(Login.this , R.string.Loginfailed , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Validacion Patterns para el campo email
    private boolean validaremail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }


}