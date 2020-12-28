package club.whuhu.sctphone.gen.ui;

import android.content.ContextWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import club.whuhu.Config;
import club.whuhu.jrpc.JRPC;
import club.whuhu.sctphone.AgentService;

public class GenUi {
    private final List<MenuItem> apps = new ArrayList<>();

    public static final Map<String, Object> RETURN_TO_DASH = Collections.singletonMap("next", "dash");

    public GenUi(AgentService ctx, JRPC jrpc) {
        apps.add(new NavigationApp(ctx, jrpc));

        if (Config.SPOTIFY_CLIENT_ID != null) {
            apps.add(new SpotifyApp(ctx, jrpc));
        }

        jrpc.register("ui:get_apps", new JRPC.Method() {
            @Override
            public void call(JRPC.Response r, Object params) throws JRPC.Error {
                r.send(MenuItem.serialize(apps));
            }
        });
    }

}
