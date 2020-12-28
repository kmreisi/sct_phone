package club.whuhu.sctphone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.util.Base64;

import com.spotify.protocol.client.Result;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ImageUri;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import club.whuhu.Config;
import club.whuhu.jrpc.JRPC;
import club.whuhu.sctphone.gen.ui.SpotifyApp;

public class IconLoader {
    private static IconLoader instance;

    private final Map<String, String> icons = new HashMap<>();

    private Context context = null;
    private JRPC jrpc = null;

    public synchronized static IconLoader getInstance() {
        if (instance == null) {
            instance = new IconLoader();
        }

        return instance;
    }


    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static String toMd5Hash(byte[] data) {
        MessageDigest m = null;

        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        m.update(data, 0, data.length);
        String hash = new BigInteger(1, m.digest()).toString(16);
        return hash;
    }

    private static byte[] toPng(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private static String toBase(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public void init(Context context, JRPC jrpc) {
        this.context = context;
        this.jrpc = jrpc;

        jrpc.register("get_icon", new JRPC.Method() {
            @Override
            public void call(JRPC.Response r, Object params) throws JRPC.Error {
                Map<String, Object> data = (Map<String, Object>) params;
                String md5 = (String) data.get("icon_md5");
                String base = icons.get(md5);

                // XXX: we should have a proper singleton for this
                // Short hack to load images on demand from spotify
                if (Config.SPOTIFY_CLIENT_ID != null && base == null) {
                    // file does not exist, if a URI it might be required to fetch it
                    try {
                        ImageUri uri = new ImageUri(md5);
                        Result<Bitmap> b = SpotifyApp.spotify.getImagesApi().getImage(uri, Image.Dimension.THUMBNAIL).await(1000, TimeUnit.MILLISECONDS);
                        if (b.isSuccessful()) {
                            // register
                            byte[] png = toPng(b.getData());
                            base = toBase(png);
                            icons.put(md5, base);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (base != null) {
                    Map<String, String> response = new HashMap<>();
                    data.put("icon", base);

                    System.out.println("ICON LOADER: Send " + base.length());
                    r.send(data);
                    return;
                }

                r.send(null);
            }
        });
    }

    public String getIconMd5(Bitmap icon) {
        if (icon == null) {
            return null;
        }

        byte[] png = toPng(icon);
        String hash = toMd5Hash(png);
        if (!icons.containsKey(hash)) {
            icons.put(hash, toBase(png));
        }

        return hash;
    }

    public String getIconMd5(Icon icon) {
        if (icon == null) {
            return null;
        }
        try {

            Drawable drawable = icon.loadDrawable(context);
            if (drawable == null) {
                return null;
            }
            return getIconMd5(drawableToBitmap(drawable));
        } catch (Throwable e) {
        }
        return null;
    }

    public String getIconMd5(Object icon) {
        if (icon instanceof Icon) {
            return getIconMd5((Icon) icon);
        }

        if (icon instanceof Bitmap) {
            return getIconMd5((Bitmap) icon);
        }

        if (icon instanceof ImageUri) {
            // the icon will be fetched from spotify on client request
            return ((ImageUri) icon).raw;
        }

        return null;
    }
}
