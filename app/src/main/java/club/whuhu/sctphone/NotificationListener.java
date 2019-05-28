package club.whuhu.sctphone;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Phaser;

import club.whuhu.jrpc.JRPC;

public class NotificationListener extends NotificationListenerService {

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static String toMd5Hash(byte[] data) {
        MessageDigest m = null;

        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        m.update(data,0,data.length);
        String hash = new BigInteger(1, m.digest()).toString(16);
        return hash;
    }

    byte[] toPng(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    String toBase(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public String getTitle(Bundle extras, String fallback) {
        CharSequence chars =
                extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
        if(chars != null && chars.length() > 0) {
            return chars.toString();
        }
        chars =
                extras.getCharSequence(Notification.EXTRA_TITLE);
        if(chars != null && chars.length() > 0) {
            return chars.toString();
        }

        return fallback;
    }

    public String getText(Bundle extras, String fallback) {
        CharSequence[] lines =
                extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        if(lines != null && lines.length > 0) {
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
        if(chars != null && chars.length() > 0) {
            return chars.toString();
        }

        return fallback;
    }

    public static Map<String, String> icons = new HashMap<>();

    public String updateIcon(Icon icon) {
        Drawable drawable = icon.loadDrawable(this);
        if (drawable == null) {
            return null;
        }

        byte[] png = toPng(drawableToBitmap(drawable));
        String hash = toMd5Hash(png);
        if(!icons.containsKey(hash)) {
            icons.put(hash, toBase(png));
        }

        return hash;
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
            params.put("icon_md5", updateIcon(icon));

            AgentService.send(new JRPC.Notification("notification", params));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        System.out.println("XXXXXXXXXXXXXXXX EVENT removed: " + sbn.getId());

    }
}
