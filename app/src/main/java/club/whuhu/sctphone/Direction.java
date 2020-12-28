package club.whuhu.sctphone;

import android.app.Notification;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum Direction {

    Left,
    Right,
    Straight;

    //9a47aad7ccf3cff40df15787e123980a
    private static final String MD5_CONTINUE_BMP = "c9961227e231985146bc38695e79bfbb";
    private static final String MD5_LEFT_BMP = "112c7a8155d627c663fd49f72c84ca71";
    private static final String MD5_RIGHT_BMP = "ae8f35e653bb79c660b6a28fe46c5bfa";


    private static String bitmap_to_md5(Drawable d) {
        if (!(d instanceof BitmapDrawable)) {
            return null;
        }

        BitmapDrawable bd = (BitmapDrawable) d;
        Bitmap b = bd.getBitmap();
        if (b == null) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] bitmapBytes = baos.toByteArray();

        String s = new String(bitmapBytes);

        MessageDigest m = null;

        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        m.update(s.getBytes(), 0, s.length());
        String hash = new BigInteger(1, m.digest()).toString(16);
        return hash;
    }

    public static Direction parseDirection(Drawable d) {
        String md5 = bitmap_to_md5(d);
        System.out.println("XXXXXXXXXXXXXXXX EVENT Direction  " + md5);
        if (MD5_CONTINUE_BMP.equals(md5)) {
            return Straight;
        } else if (MD5_LEFT_BMP.equals(md5)) {
            return Left;
        } else if (MD5_RIGHT_BMP.equals(md5)) {
            return Right;
        } else {
            return null;
        }
    }
}
