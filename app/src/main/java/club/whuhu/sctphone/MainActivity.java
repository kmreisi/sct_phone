package club.whuhu.sctphone;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;

public class MainActivity extends AppCompatActivity {

    public Boolean verifyNotificationPermission() {
        String theList = android.provider.Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        String[] theListList = theList.split(":");
        String me = (new ComponentName(this, NotificationListener.class)).flattenToString();
        for (String next : theListList) {
            if (me.equals(next)) return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(getApplicationContext())) {
            requestPermission();
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE},
                666);

        Button buttonStart = findViewById(R.id.buttonStart);
        Button buttonStop = findViewById(R.id.buttonStop);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AgentService.class);
                ContextCompat.startForegroundService(getApplicationContext(), intent);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(getApplicationContext(), AgentService.class);
                stopService(serviceIntent);
            }
        });
    }

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2323;

    private void requestPermission() {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
