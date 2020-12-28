package club.whuhu.sctphone.gen.ui;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.DhcpInfo;
import android.net.Uri;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Result;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.ListItem;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import club.whuhu.Config;
import club.whuhu.jrpc.JRPC;
import club.whuhu.sctphone.AgentService;
import club.whuhu.sctphone.gen.ui.GenApp;
import club.whuhu.sctphone.gen.ui.MenuItem;

public class SpotifyApp extends GenApp {


    private static Bitmap getBitmap(ListItem item) {
        if (item.imageUri == null) {
            return null;
        }

        if (item.imageUri.toString().contains("android.resource://")) {
            return null;
        }

        try {
            Result<Bitmap> b = spotify.getImagesApi().getImage(item.imageUri, Image.Dimension.THUMBNAIL).await(1000, TimeUnit.MILLISECONDS);
            if (b.isSuccessful()) {
                return b.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getTitle(ListItem item) {
        return item.title;
    }

    private static boolean hasChildren(ListItem item) {
        if (item.hasChildren) {
            return true;
        }

        if (item.id.contains("spotify:track") || item.id.contains("spotify:episode")) {
            return false;
        }

        return true;
    }

    private static String getText(ListItem item) {
        StringBuilder s = new StringBuilder();
        if (hasChildren(item)) {
            s.append("[List] ");
        }

        if (item.playable) {
            s.append("[Play] ");
        }

        s.append(item.subtitle);
        return s.toString();
    }

    private static String getCmd1(ListItem item) {
        if (hasChildren(item)) {

            return "list";
        }

        if (item.playable) {
            return "play";
        }

        return null;
    }

    private static String getCmd2(ListItem item) {
        if (item.playable) {
            return "play";
        }

        return null;
    }

    public static class ListEntry extends MenuItem.Generic {
        private static Object toData(String id) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            return data;
        }

        public ListEntry(ListItem item) {
            super(item.title, SpotifyApp.getText(item), getBitmap(item), ID, getCmd1(item), getCmd2(item), toData(item.id));
        }
    }


    public static final String ID = "spotify";
    private static SpotifyAppRemote spotify;


    public SpotifyApp(final AgentService ctx, JRPC jrpc) {
        super(ctx, jrpc);

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(Config.SPOTIFY_CLIENT_ID)
                        .showAuthView(true)
                        .setRedirectUri("https://github.com/kmreisi/sct_phone")
                        .build();

        SpotifyAppRemote.connect(ctx, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        spotify = spotifyAppRemote;
                    }

                    public void onFailure(Throwable throwable) {
                    }
                });
    }

    @Override
    public String getTitle() {
        return "Spotify";
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public String getAppId() {
        return ID;
    }

    private Map<String, ListItem> lookup = new HashMap<>();


    @Override
    public void handle(JRPC.Response r, String cmd, Object params) {
        if (spotify == null || !spotify.isConnected()) {
            r.send(GenUi.RETURN_TO_DASH);
            return;
        }

        if ("list".equals(cmd)) {
            String id = (String) ((Map<String, Object>) params).get("id");
            ListItem parent = lookup.get((id));
            if (parent == null) {
                parent = new ListItem(id, null, null, null, null, true, true);
            }

            spotify.getContentApi().getChildrenOfItem(parent, 20, 0)
                    .setResultCallback(listItems -> {

                        new Thread(() -> {
                            List<MenuItem> items = new ArrayList<>();
                            for (ListItem item : listItems.items) {
                                ListEntry e = new ListEntry(item);
                                if (e.getCommand() != null) {
                                    items.add(e);
                                    lookup.put(item.id, item);
                                }
                            }
                            r.send(MenuItem.serialize(items));
                        }).start();
                    })
                    .setErrorCallback(throwable -> {
                        r.send(null);
                    });

            return;
        }

        if ("play".equals(cmd)) {
            String id = (String) ((Map<String, Object>) params).get("id");
            spotify.getPlayerApi().play(id);
            r.send(GenUi.RETURN_TO_DASH);
            return;
        }

        // since the API does not allow us to send the following artists send the recommended play lists
        spotify.getContentApi().getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
                .setResultCallback(listItems -> {
                    List<MenuItem> items = new ArrayList<>();
                    for (ListItem item : listItems.items) {
                        ListEntry e = new ListEntry(item);
                        if (e.getCommand() != null) {
                            items.add(e);
                            lookup.put(item.id, item);
                        }
                    }
                    r.send(MenuItem.serialize(items));
                })
                .setErrorCallback(throwable -> {
                    r.send(null);
                });
    }
}
