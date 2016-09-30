package me.zachcheatham.audibly;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import me.zachcheatham.audibly.format.*;

public class NotificationService extends NotificationListenerService
{
    private static final String LOG_TAG = "AUDIBLY_NLS";
    private static final String NOTIFICATION_ACTION_STOP = "me.zachcheatham.audibly.STOP";
    private static final int NOTIFICATION_ID_CANCEL = 1;

    private final static String[] disabledApplications = {
            "com.spotify.music",
            "com.android.vending",
            "me.zachcheatham.audibly"
    };

    private BluetoothStateReceiver bluetoothStateReceiver = null;
    private HeadsetPlugReciever headsetPlugReciever = null;
    private AudiblyNotificationActionReceiver audiblyNotificationActionReceiver = null;

    private AnnouncerService announcerService = null;
    private boolean announcerConnected = false;
    private String connectedMessage = null;

    @Override
    public void onCreate()
    {
        Log.d(LOG_TAG, "Created.");

        IntentFilter intentFilter = new IntentFilter("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        bluetoothStateReceiver = new BluetoothStateReceiver();
        registerReceiver(bluetoothStateReceiver, intentFilter);

        intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        headsetPlugReciever = new HeadsetPlugReciever();
        registerReceiver(headsetPlugReciever, intentFilter);

        intentFilter = new IntentFilter(NOTIFICATION_ACTION_STOP);
        audiblyNotificationActionReceiver = new AudiblyNotificationActionReceiver();
        registerReceiver(audiblyNotificationActionReceiver, intentFilter);


        if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn())
        {
            Log.d(LOG_TAG, "Bluetooth already running.");
            connectedMessage = String.format("%s will now speak notifications.", BluetoothAdapter.getDefaultAdapter().getName());
            startAnnouncerService();
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification)
    {
        if (isActive())
        {
            Log.d(LOG_TAG, "Notification Start");
            Log.d(LOG_TAG, notification.getPackageName());
            Log.d(LOG_TAG, "ID: " + notification.getId());
            Log.d(LOG_TAG, "OnGoing: " + notification.isOngoing());
            Log.d(LOG_TAG, "getNotification().toString(): " + notification.getNotification().toString());

            if (!notification.isOngoing() && !isAppDisabled(notification.getPackageName()))
            {
                String text = getSpeechFormatter(notification.getPackageName()).getText(notification.getNotification());
                if (text != null && text.length() > 0)
                    announcerService.announce(text);
            }

            Log.d(LOG_TAG, "------------------------");
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(LOG_TAG, "Destroyed.");

        if (announcerConnected)
        {
            unbindService(announcerConnection);
            announcerConnected = false;
            announcerService = null;
        }

        if (headsetPlugReciever != null)
        {
            unregisterReceiver(headsetPlugReciever);
            headsetPlugReciever = null;
        }

        if (bluetoothStateReceiver != null)
        {
            unregisterReceiver(bluetoothStateReceiver);
            bluetoothStateReceiver = null;
        }

        if (audiblyNotificationActionReceiver != null)
        {
            unregisterReceiver(audiblyNotificationActionReceiver);
            audiblyNotificationActionReceiver = null;
        }
    }

    private boolean isActive()
    {
        return announcerConnected;// && audioManager.isBluetoothA2dpOn();
    }

    private void startAnnouncerService()
    {
        if (!announcerConnected)
        {
            Intent intent = new Intent(this, AnnouncerService.class);
            bindService(intent, announcerConnection, Context.BIND_AUTO_CREATE);

            showStopNotification();
        }
    }

    private void stopAnnouncerService()
    {
        if (announcerConnected)
        {
            unbindService(announcerConnection);
            announcerService.stopSelf();
            announcerService = null;
            announcerConnected = false;

            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID_CANCEL);
        }
    }

    private void showStopNotification()
    {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.ic_volume_up_black_24dp);
        notificationBuilder.setPriority(Notification.PRIORITY_LOW);
        notificationBuilder.setContentTitle("Audibly running");
        notificationBuilder.setContentText("Touch to stop.");
        notificationBuilder.setOngoing(true);

        Intent stopIntent = new Intent(NOTIFICATION_ACTION_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingStopIntent);

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID_CANCEL, notificationBuilder.build());
    }

    private static SpeechFormatter getSpeechFormatter(String packageName)
    {
        switch (packageName)
        {
            case "com.textra":
                return new SMSSpeechFormatter();
            case "com.google.android.apps.inbox":
                return new EmailSpeechFormatter();
            case "com.snapchat.android":
                return new SnapchatSpeechFormatter();
            case "com.valvesoftware.android.steam.community":
                return new SteamSpeechFormatter();
            default:
                return new GenericSpeechFormatter();
        }
    }

    private static boolean isAppDisabled(String packageID)
    {
        for (String s : disabledApplications)
            if (s.equals(packageID))
                return true;

        return false;
    }

    @SuppressWarnings("deprecation")
    private class BluetoothStateReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            if (state == BluetoothA2dp.STATE_CONNECTED)
            {
                Log.d(LOG_TAG, "Bluetooth Connected.");

                if (!am.isWiredHeadsetOn())
                {
                    connectedMessage = String.format("%s connected.", BluetoothAdapter.getDefaultAdapter().getName());
                    startAnnouncerService();
                }
                else
                    announcerService.announce(String.format("%s connected.", BluetoothAdapter.getDefaultAdapter().getName()));
            }
            else if (state == BluetoothA2dp.STATE_DISCONNECTED || state == -1)
            {
                Log.d(LOG_TAG, "Bluetooth Disconnected.");

                if (!am.isWiredHeadsetOn())
                    stopAnnouncerService();
            }
        }
    }

    private class HeadsetPlugReciever extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int state = intent.getIntExtra("state", 0);
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            if (state == 1)
            {
                Log.d(LOG_TAG, "Headphone jack plugged.");

                if (!am.isBluetoothA2dpOn())
                    startAnnouncerService();
            }
            else
            {
                Log.d(LOG_TAG, "Headphone jack unplugged.");

                if (!am.isBluetoothA2dpOn())
                    stopAnnouncerService();
            }
        }
    }

    private class AudiblyNotificationActionReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(NOTIFICATION_ACTION_STOP))
            {
                Log.d(LOG_TAG, "User requested notifications to be stopped.");
                stopAnnouncerService();
            }
        }
    }

    private ServiceConnection announcerConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            AnnouncerService.AnnouncerBinder binder = (AnnouncerService.AnnouncerBinder) service;
            announcerService = binder.getService();
            announcerConnected = true;

            if (connectedMessage != null)
            {
                final String msg = connectedMessage;
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        announcerService.announce(msg);
                    }
                }, 500);
                connectedMessage = null;
            }

            Log.d(LOG_TAG, "Connected to announcer");
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            announcerConnected = false;
            Log.d(LOG_TAG, "Announcer Disconnected.");
        }
    };
}
