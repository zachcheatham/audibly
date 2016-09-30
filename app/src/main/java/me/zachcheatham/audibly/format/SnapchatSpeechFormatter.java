package me.zachcheatham.audibly.format;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnapchatSpeechFormatter extends SpeechFormatter
{
    private Pattern multiPattern = Pattern.compile("^(.*)\\((\\d+)\\)$");

    @Override
    protected String formatText(String title, String text)
    {
        Matcher matcher = multiPattern.matcher(text);

        if (matcher.find())
            return String.format("You have %s snap chats from %s", matcher.group(2), matcher.group(1));
        else
            return String.format("Snap chat from %s", text);
    }
}
