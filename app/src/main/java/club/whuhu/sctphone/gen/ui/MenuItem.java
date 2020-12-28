package club.whuhu.sctphone.gen.ui;

import android.graphics.drawable.Icon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import club.whuhu.sctphone.IconLoader;

public interface MenuItem {
    String getTitle();

    String getText();

    Object getIcon();

    String getAppId();

    String getCommand();

    String getSecondCommand();

    Object getData();


    static Object serialize(MenuItem item) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", item.getTitle());
        data.put("text", item.getText());
        data.put("icon_md5", IconLoader.getInstance().getIconMd5(item.getIcon()));
        data.put("app_id", item.getAppId());
        data.put("cmd", item.getCommand());
        data.put("cmd2", item.getSecondCommand());
        data.put("data", item.getData());

        return data;
    }

    static Object serialize(List<MenuItem> items) {
        List<Object> values = new ArrayList<>();
        for (MenuItem item : items) {
            values.add(serialize(item));
        }
        return values;
    }

    public static class Generic implements MenuItem {

        private final String title;
        private final String text;
        private final Object icon;
        private final String app_id;
        private final String cmd;
        private final String cmd2;
        private final Object data;

        public Generic(String title, String text, Object icon, String app_id, String cmd, String cmd2, Object data) {
            this.title = title;
            this.text = text;
            this.icon = icon;
            this.app_id = app_id;
            this.cmd = cmd;
            this.cmd2 = cmd2;
            this.data = data;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public Object getIcon() {
            return icon;
        }

        @Override
        public String getAppId() {
            return app_id;
        }

        @Override
        public String getCommand() {
            return cmd;
        }

        @Override
        public String getSecondCommand() {
            return cmd2;
        }

        @Override
        public Object getData() {
            return data;
        }
    }
}
