package club.whuhu.sctphone.gen.ui;

import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import club.whuhu.jrpc.JRPC;
import club.whuhu.sctphone.AgentService;
import club.whuhu.sctphone.gen.ui.GenApp;
import club.whuhu.sctphone.gen.ui.MenuItem;

public class NavigationApp extends GenApp {

    public static class Place extends MenuItem.Generic {
        public static final String CMD = "navigate";

        private static Object toData(String uri) {
            Map<String, Object> data = new HashMap<>();
            data.put("uri", uri);
            return data;
        }

        public Place(String name, String uri) {
            super(name, uri, null, ID, CMD, null, toData(uri));
        }
    }

    public static final String ID = "navigation";

    private static List<MenuItem> places = new ArrayList<>();

    static {
        places.add(new Place("Ulmer Münster", "Münsterplatz 21, 89073 Ulm"));
        places.add(new Place("Murphy's Law", "Keltergasse 3, 89073 Ulm"));
        places.add(new Place("Donauhalle Ulm", "Böfinger Str. 50, 89073 Ulm"));
        places.add(new Place("Museum der Brotkultur", "Salzstadelgasse 10, 89073 Ulm"));
        places.add(new Place("Bier-Akademie", "Baurengasse 10, 89073 Ulm"));
    }

    public NavigationApp(AgentService ctx, JRPC jrpc) {
        super(ctx, jrpc);
    }

    @Override
    public String getTitle() {
        return "Navigate";
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

    @Override
    public void handle(JRPC.Response r, String cmd, Object params) {
        if (Place.CMD.equals(cmd)) {
            // do navigation, return null
            Map<String, String> data = (Map<String, String>) params;
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + data.get("uri"));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mapIntent.setPackage("com.google.android.apps.maps");
            try {
                ctx.startActivity(mapIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }

            r.send(GenUi.RETURN_TO_DASH);
            return;
        }

        r.send(MenuItem.serialize(places));
    }

}
