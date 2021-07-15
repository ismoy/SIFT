package cl.tofcompany.sift.Providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import cl.tofcompany.sift.Model.HistoryBooking;

public class HistoryBookingProvider {
    private DatabaseReference mDatabase;

    public HistoryBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("HistoryBooking");
    }

    public Task<Void> create(HistoryBooking historyBooking) {
        return mDatabase.child(historyBooking.getIdHistoryBooking()).setValue(historyBooking);
    }

    public  Task<Void> updatecalificactionClient(String idHistoryBooking, float calificacionClient) {
        Map<String, Object> map = new HashMap<>();
        map.put("calificationClient", calificacionClient);
        return mDatabase.child(idHistoryBooking).updateChildren(map);
    }

    public  Task<Void> updatecalificactionDriver(String idHistoryBooking, float calificacionDriver) {
        Map<String, Object> map = new HashMap<>();
        map.put("calificationDriver", calificacionDriver);
        return mDatabase.child(idHistoryBooking).updateChildren(map);
    }

    public DatabaseReference getHistoryBooking(String idHistoryBooking) {
        return mDatabase.child(idHistoryBooking);
    }

}
