package me.zachcheatham.audibly.format;

public class SMSSpeechFormatter extends SpeechFormatter
{
    @Override
    protected String formatText(String title, String text)
    {
        return String.format("Text from %s: %s", title, text);
    }
}
