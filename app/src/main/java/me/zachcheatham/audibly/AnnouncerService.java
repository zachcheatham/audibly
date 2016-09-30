package me.zachcheatham.audibly;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Locale;

public class AnnouncerService extends Service implements TextToSpeech.OnInitListener
{
    private final IBinder binder = new AnnouncerBinder();

    private TextToSpeech tts;
    private boolean ready = false;

    public class AnnouncerBinder extends Binder
    {
        AnnouncerService getService()
        {
            // Return this instance of LocalService so clients can call public methods
            return AnnouncerService.this;
        }
    }

    public void announce(String text)
    {
        if (ready)
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, hashCode() + "");

        Log.d("AnnouncerService", "Speaking: " + text);
    }

    @Override
    public void onCreate()
    {
        Log.d("AnnouncerService", "Creating instance...");

        tts = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS)
        {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
                ready = true;
        }
    }

    @Override
    public void onDestroy()
    {
        if (tts != null)
        {
            tts.stop();
            tts.shutdown();
        }

        Log.d("AnnouncerService", "Destroyed.");

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }
}
