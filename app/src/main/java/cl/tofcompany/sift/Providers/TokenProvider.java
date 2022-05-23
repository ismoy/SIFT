package cl.tofcompany.sift.Providers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import cl.tofcompany.sift.Model.Token;

public class TokenProvider {
    DatabaseReference mDatabaseReference;

    public TokenProvider() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }
    public void create(final String idUser) {
        if (idUser == null) return;
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
               Token token = new Token(task.getResult());
                mDatabaseReference.child(idUser).setValue(token);

            }
        });
    }
    public DatabaseReference getToken(String idUser){
        return mDatabaseReference.child(idUser);
    }


}
