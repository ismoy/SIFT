package cl.tofcompany.sift.Channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import cl.tofcompany.sift.R;
//extendemos el contextWrapper para crear los metodos necesario para la notificacion
public class NotificationHelper extends ContextWrapper {
    //definemos de forma estatica los canales de comunicanion
    //definemos el canal del id en nuestro caso el el id del proyecto
    private static final String CHANNEL_ID= "cl.tofcompany.sift";
    //definemos el canal del nombre en nuestro caso es el nombre del proyecto
    private static final String CHANNEL_NAME = "SIFT";
    private NotificationManager manager;

    //proviene notificationhelper
    public NotificationHelper(Context base) {
        super(base);
        //preguntamos por la version de sdk es mayor o igual a la versio de android actual antes de crear los canales
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //metodo de los canales
            createChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //metodo de los canales
    private void createChannels() {
        //pasamos al metodo notification chanel los canales que teniamos definidos arriba
        NotificationChannel notificationChannel = new
                NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        //prendemos el celular cuando llego la notificacion
        notificationChannel.enableLights(true);
        //hacemos vibrar el celular cuando llego la notificacion
        notificationChannel.enableVibration(true);
        //la luz que mostraba como mensaje de notificacion sera gris
        notificationChannel.setLightColor(Color.GRAY);
        //si la pantalla esta apagada igual lo vamos a prender para mostrar la notificacion
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        //llamamos el metodo getManager y lo pasamos el metodo crearnotificacionbanner y alli lo pasamos nuestra notificacion chanel
        getManager().createNotificationChannel(notificationChannel);
    }
      //metodo notificacion manager
    public NotificationManager getManager() {
        //validamos el manager
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //metodo para notificacion en version android recientes  necesitamos el titulo de la notificacion mas el cuerpo, mas una accion pendiente en nuestro caso va hacer 2 botones
    //aceptar y rechazar y tambien lo pasamos un sonido para la notificacion
    public Notification.Builder getNotification(String title, String body, PendingIntent intent, Uri soundUri) {
        //retornamos la notificacion builder y lo pasamos el channer id
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                //enviando el titulo
                .setContentTitle(title)
                //enviando el cuerpo
                .setContentText(body)
                //se puede auto cancelado en caso no se acepta la solicitud
                .setAutoCancel(true)
                //enviando la reproduccion del sonido
                .setSound(soundUri)
                //enviando los pendingInten que estan guardados en la accioen intent
                //recuerdan de cada intent es un await esta esperando una accion
                .setContentIntent(intent)
                //enviando el icino de la app
                .setSmallIcon(R.drawable.ic_car)
                //mas que nada es el estilo de la notificacion como debe verse las cosas en la pantalla del receptor
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //metodo notification Accion lo pasamos el titulo,el cuerpo,el sonido,y las 2 acciones
    public Notification.Builder getNotificationActions(String title,
                                                       String body,
                                                       Uri soundUri,
                                                       Notification.Action acceptAction ,
                                                       Notification.Action cancelAction) {
        //aqui retornamos el metodo de notification
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                //enviando el titulo
                .setContentTitle(title)
                //enviando el cuerpo
                .setContentText(body)
                //habilitamos el auto cancelado
                .setAutoCancel(true)
                //enviando el sonido
                .setSound(soundUri)
                //enviando el icono
                .setSmallIcon(R.drawable.ic_car)
                //agregamos la accion de aceptar
                .addAction(acceptAction)
                //agregamos la accion de cancelar
                .addAction(cancelAction)
                //el estilo como debe verse en la pantalla del receptor
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }
     //metodo para enviar la misma notificacion de arriba pero en versiones android  anteriores o antiguas
     //lo pasamos el titulo,el cuerpo,una accion pendiente,y un sonido
    public NotificationCompat.Builder getNotificationOldAPI(String title, String body, PendingIntent intent, Uri soundUri) {
        //retornamos la notificacion builder y lo pasamos el channer id
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                //enviando el titulo
                .setContentTitle(title)
                //enviando el body
                .setContentText(body)
                //habilitamos el autocancelado
                .setAutoCancel(true)
                //enviando el sonido
                .setSound(soundUri)
                //enviando la accion del intent
                .setContentIntent(intent)
                //enviando el icono
                .setSmallIcon(R.drawable.ic_car)
                //es el estilo como debe verse la notificacion en la pantalla del recptor
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));
    }
    //metodo notification Accion lo pasamos el titulo,el cuerpo,el sonido,y las 2 acciones para version android antigua
    public NotificationCompat.Builder getNotificationOldAPIActions(
            String title,
            String body,
            Uri soundUri,
            NotificationCompat.Action acceptAction,
            NotificationCompat.Action cancelAction) {
        //aqui retornamos el metodo de notificationoldaction
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                //enviando el titulo
                .setContentTitle(title)
                //enviando el cuerpo
                .setContentText(body)
                //habilitamos el auto cancelado
                .setAutoCancel(true)
                //enviando el sonido
                .setSound(soundUri)
                //enviando el incono
                .setSmallIcon(R.drawable.ic_car)
                //agregamos la action de aceptar
                .addAction(acceptAction)
                //agregamos la action de rechazar
                .addAction(cancelAction)
                //estilo como debe verse la notificacion en la pantalla del receptor
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));
    }
}
