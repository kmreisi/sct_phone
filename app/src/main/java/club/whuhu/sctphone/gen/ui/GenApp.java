package club.whuhu.sctphone.gen.ui;

import android.app.Service;
import android.graphics.drawable.Icon;

import java.util.Map;

import club.whuhu.jrpc.JRPC;
import club.whuhu.sctphone.AgentService;

public abstract class GenApp implements MenuItem {

    public abstract String getTitle();

    public abstract String getText();

    public abstract Icon getIcon();

    public String getCommand() {
        return null;
    }

    @Override
    public String getSecondCommand() {
        return null;
    }

    public abstract String getAppId();

    public abstract void handle(JRPC.Response r, String cmd, Object data);

    protected final Service ctx;

    public GenApp(AgentService ctx, JRPC jrpc) {
        this.ctx = ctx;
        jrpc.register("ui:" + getAppId(), (r, params) -> {

            try {
                Map<String, Object> data = (Map<String, Object>) params;
                handle(r, (String) data.get("cmd"), data.get("data"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
