package me.zachcheatham.audibly;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Test notification

        /*NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.design_snackbar_background);
        notificationBuilder.setContentTitle("This is a test.");
        notificationBuilder.setContentText("Of a notification.");
        notificationBuilder.setAutoCancel(true);

        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notificationBuilder.build());

        Log.d("AUDIBLY", "Notification notified.");*/
    }
}
