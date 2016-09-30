package me.zachcheatham.audibly.format;

public class EmailSpeechFormatter extends SpeechFormatter
{
    @Override
    protected String formatText(String title, String text)
    {
        return String.format("Email from %s: Subject: %s", removeInboxLabel(title), replaceRE(text));
    }

    private static String removeInboxLabel(String title)
    {
        return title.replaceAll("^(.*):\\s", "");
    }

    private static String replaceRE(String subject)
    {
        return subject.replaceAll("[Rr][Ee]:", "Response to");
    }
}
