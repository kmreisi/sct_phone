package club.whuhu.sctphone;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import club.whuhu.jrpc.JRPC;

public class NotificationListener extends NotificationListenerService {

    public static NotificationListener INSTANCE;

    public NotificationListener() {
        INSTANCE = this;
    }

    public String getTitle(Bundle extras, String fallback) {
        CharSequence chars =
                extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
        if (chars != null && chars.length() > 0) {
            return chars.toString();
        }
        chars =
                extras.getCharSequence(Notification.EXTRA_TITLE);
        if (chars != null && chars.length() > 0) {
            return chars.toString();
        }

        return fallback;
    }

    public String getText(Bundle extras, String fallback) {
        CharSequence[] lines =
                extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        if (lines != null && lines.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (CharSequence msg : lines)
                if (msg != null && msg.length() > 0) {
                    sb.append(msg.toString());
                    sb.append('\n');
                }
            return sb.toString().trim();
        }
        CharSequence chars =
                extras.getCharSequence(Notification.EXTRA_TEXT);
        if (chars != null && chars.length() > 0) {
            return chars.toString();
        }

        return fallback;
    }


    public static StatusBarNotification getNotification(int id) {
        if (INSTANCE != null) {
            for (StatusBarNotification notification : INSTANCE.getActiveNotifications()) {
                if (notification.getId() == id) {
                    return notification;
                }
            }
            for (StatusBarNotification notification : INSTANCE.getSnoozedNotifications()) {
                if (notification.getId() == id) {
                    return notification;
                }
            }
        }
        return null;
    }

    public static void sendActiveNotifications() {
        if (INSTANCE == null) {
            return;
        }

        for (StatusBarNotification notification : INSTANCE.getActiveNotifications()) {
            INSTANCE.onNotificationPosted(notification);
        }
        for (StatusBarNotification notification : INSTANCE.getSnoozedNotifications()) {
            INSTANCE.onNotificationPosted(notification);
        }

    }

    public static PendingIntent getNotificationAction(Object params) {

        Map<String, Object> data = (Map<String, Object>) params;
        long id = (long) data.get("id");
        String title = (String) data.get("title");

        StatusBarNotification notification = getNotification((int) id);
        if (notification == null) {
            return null;
        }

        Notification.Action[] actions = notification.getNotification().actions;
        if (actions == null) {
            return null;
        }

        for (Notification.Action action : actions) {
            if (action.title.equals(title)) {
                return action.actionIntent;
            }
        }

        return null;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {

            Notification notification = sbn.getNotification();

            Bundle extras = notification.extras;

            String title = getTitle(extras, sbn.getPackageName());
            String text = getText(extras, extras.toString());

            Icon icon = null;
            icon = notification.getLargeIcon();
            if (icon == null) {
                icon = notification.getSmallIcon();
            }

            Map<String, Object> params = new HashMap<>();
            params.put("id", sbn.getId());
            params.put("title", title);
            params.put("text", text);
            params.put("icon_md5", IconLoader.getInstance().getIconMd5(icon));

            if (notification.actions != null) {
                List<Object> actions = new ArrayList<>();
                for (Notification.Action action : notification.actions) {
                    Map<String, Object> actionData = new HashMap<>();
                    actionData.put("title", action.title.toString());
                    //   actionData.put("icon_md5", IconLoader.getInstance().getIconMd5(action.getIcon()));
                    actions.add(actionData);
                }
                params.put("actions", actions);
            }

            AgentService.send(new JRPC.Notification("notification", params));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        System.out.println("XXXXXXXXXXXXXXXX EVENT removed: " + sbn.getId());

        Map<String, Object> params = new HashMap<>();
        params.put("id", sbn.getId());

        AgentService.send(new JRPC.Notification("notification_removed", params));

    }
}
