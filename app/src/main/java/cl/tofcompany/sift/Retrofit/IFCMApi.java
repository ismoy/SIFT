package cl.tofcompany.sift.Retrofit;

import cl.tofcompany.sift.Model.FCMBody;
import cl.tofcompany.sift.Model.FCMResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA973gs3A:APA91bEoib9L7-rOq965zZbYtNYjHMPKa-GFe28b1QqY8EB6fZgRji-8f8bNfICj9MiFlfAGjZtk_zU09stVgyXrsKdZ5l5PfkIdKuivXR8I0i7XKuE9S59fw8YUutr7eYFxDyKvQXun"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}
