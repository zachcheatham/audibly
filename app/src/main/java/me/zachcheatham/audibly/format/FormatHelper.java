package me.zachcheatham.audibly.format;

public class FormatHelper
{
    public static String removeURLs(String text)
    {
        return text.replaceAll("(.*://[^<>[:space:]]+[[:alnum:]/])", "");
    }
}