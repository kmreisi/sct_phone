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

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import club.whuhu.jrpc.JRPC;
import club.whuhu.jrpc.Link;
import club.whuhu.jrpc.Server;
import club.whuhu.sctphone.gen.ui.GenUi;

public class AgentService extends Service {
    public static final String CHANNEL_ID = "sct_notification_channel";

    private static Server eventServer = new Server(Link.ANDROID_CAR_SERVICE_EVENT, new Link.ILinkStateListener() {
        @Override
        public void connecting() {

        }

        @Override
        public void connected() {
            NotificationListener.sendActiveNotifications();
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
        eventServer.getJrpc().register("notification_action", new JRPC.Method() {
            @Override
            public void call(JRPC.Response r, Object params) throws JRPC.Error {
                PendingIntent intent = NotificationListener.getNotificationAction(params);
                if (intent == null) {
                    return;
                }

                try {
                    intent.send();
                } catch (Exception e) {
                }

                r.send(params);
            }
        });

        eventServer.getJrpc().register("notification_cancel", new JRPC.Method() {
            @Override
            public void call(JRPC.Response r, Object params) throws JRPC.Error {
                NotificationListener.hideNotification(params);
            }
        });

        IconLoader.getInstance().init(this, iconServer.getJrpc());
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

        new GenUi(this, eventServer.getJrpc());

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
