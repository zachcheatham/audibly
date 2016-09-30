package me.zachcheatham.audibly.format;

import android.app.Notification;

public abstract class SpeechFormatter
{
    protected boolean removeURLs = true;

    public String getText(Notification notification)
    {
        CharSequence charSeqTitle = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence charSeqText = notification.extras.getCharSequence(Notification.EXTRA_TEXT);

        String title;
        String text;

        if (charSeqTitle != null)
            title = charSeqTitle.toString();
        else
            title = null;

        if (charSeqText != null)
            text = charSeqText.toString();
        else
            text = null;

        return FormatHelper.removeURLs(formatText(title, text));
    }

    protected abstract String formatText(String title, String text);

}
