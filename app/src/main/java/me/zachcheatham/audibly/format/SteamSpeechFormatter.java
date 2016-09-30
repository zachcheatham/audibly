package me.zachcheatham.audibly.format;

// com.valvesoftware.android.steam.community
public class SteamSpeechFormatter extends SpeechFormatter
{
    @Override
    protected String formatText(String title, String text)
    {
        return String.format("Steam message from %s", title);
    }
}
