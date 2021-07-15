package cl.tofcompany.sift.Providers;

import cl.tofcompany.sift.Model.FCMBody;
import cl.tofcompany.sift.Model.FCMResponse;
import cl.tofcompany.sift.Retrofit.IFCMApi;
import cl.tofcompany.sift.Retrofit.RetrofitClient;
import retrofit2.Call;

public class NotificationProvider {


    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body) {
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}
