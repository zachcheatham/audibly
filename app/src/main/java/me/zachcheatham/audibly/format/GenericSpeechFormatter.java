package me.zachcheatham.audibly.format;

public class GenericSpeechFormatter extends SpeechFormatter
{

    @Override
    protected String formatText(String title, String text)
    {
        return title + " " + text;
    }
}
