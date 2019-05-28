package club.whuhu.sctphone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import club.whuhu.jrpc.JRPC;
import club.whuhu.jrpc.Link;
import club.whuhu.jrpc.Server;

public class AgentService extends Service {
    public static final String CHANNEL_ID = "sct_notification_channel";

    private static Server eventServer = new Server(Link.ANDROID_CAR_SERVICE_EVENT, new Link.ILinkStateListener() {
        @Override
        public void connecting() {

        }

        @Override
        public void connected() {

        }

        @Override
        public void disconnected() {

        }
    });

    private static Server iconServer = new Server(Link.ANDROID_CAR_SERVICE_ICON, new Link.ILinkStateListener() {
        @Override
        public void connecting() {

        }

        @Override
        public void connected() {

        }

        @Override
        public void disconnected() {

        }
    });

    public static void send(JRPC.Notification notification) {
         eventServer.getJrpc().send(notification);
    }

   public AgentService() {
       eventServer.getJrpc().register("get_places", new JRPC.Method() {
           @Override
           public void call(JRPC.Response r, Object params) throws JRPC.Error {
               List<Object> places = new ArrayList<>();
               {
                   Map<String, String> place = new HashMap<>();
                   place.put("name", "Ulmer Münster" );
                   place.put("uri", "Münsterplatz 21, 89073 Ulm" );
                   places.add(place);
               }
               {
                   Map<String, String> place = new HashMap<>();
                   place.put("name", "Murphy's Law" );
                   place.put("uri", "Keltergasse 3, 89073 Ulm" );
                   places.add(place);
               }
               {
                   Map<String, String> place = new HashMap<>();
                   place.put("name", "Donauhalle Ulm" );
                   place.put("uri", "Böfinger Str. 50, 89073 Ulm" );
                   places.add(place);
               }
               {
                   Map<String, String> place = new HashMap<>();
                   place.put("name", "Museum der Brotkultur" );
                   place.put("uri", "Salzstadelgasse 10, 89073 Ulm" );
                   places.add(place);
               }
               {
                   Map<String, String> place = new HashMap<>();
                   place.put("name", "Bier-Akademie" );
                   place.put("uri", "Baurengasse 10, 89073 Ulm" );
                   places.add(place);
               }

               r.send(places);
           }
       });


       eventServer.getJrpc().register("navigate", new JRPC.Method() {
           @Override
           public void call(JRPC.Response r, Object params) throws JRPC.Error {
               System.out.println("NAVIGATE TO" + params);

               Map<String, String> data = (Map<String, String>)params;
              Uri gmmIntentUri = Uri.parse("google.navigation:q=" +data.get("uri"));
               Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
               mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

               mapIntent.setPackage("com.google.android.apps.maps");
               startActivity(mapIntent);

               r.send(params);
           }
       });


       iconServer.getJrpc().register("get_icon", new JRPC.Method() {
           @Override
           public void call(JRPC.Response r, Object params) throws JRPC.Error {
               Map<String, Object> data = (Map<String, Object>) params;
               String md5 = (String) data.get("icon_md5");
               String base = NotificationListener.icons.get(md5);
               data.put("icon", base);
               System.out.println("XXXXXXXXXXXXXXXXXXX SEND " + base.length());

               r.send(data);
           }
       });
   }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("lalall")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        eventServer.start();
        iconServer.start();

        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public boolean stopService(Intent name) {

        eventServer.stop();
        iconServer.stop();

        return super.stopService(name);
    }
}
