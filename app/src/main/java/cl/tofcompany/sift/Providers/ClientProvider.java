package cl.tofcompany.sift.Providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import cl.tofcompany.sift.Model.Client;

public class ClientProvider {
    DatabaseReference database;

    public ClientProvider() {
        database = FirebaseDatabase.getInstance().getReference().child("Users").child("Clients");
    }

    public Task<Void> create(Client client){
        Map<String,Object> map = new HashMap<>();
        map.put("name",client.getName());
        map.put("email",client.getEmail());
        map.put("password",client.getPassword());
        map.put("password2",client.getPassword2());
        return database.child(client.getId()).setValue(map);
    }
    public DatabaseReference getClient(String idClient){
        return database.child(idClient);
    }
    public Task<Void> update(Client client){
        Map<String,Object> map = new HashMap<>();
        map.put("name",client.getName());
        map.put("image",client.getImage());
        map.put("password",client.getPassword());
        map.put("password2",client.getPassword2());
        return database.child(client.getId()).updateChildren(map);
    }
}



