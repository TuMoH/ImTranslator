package com.timursoft.subtitleparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the .SRT subtitle format
 */
public class FormatSRT implements Format {

    private final static Pattern PARSE_PATTERN = Pattern.compile("(\\d+)(\\r\\n|\\n)" +
            "(\\d+:\\d+:\\d+,\\d+) --> (\\d+:\\d+:\\d+,\\d+)" +
            "(\\r\\n|\\n)(.*)(\\r\\n|\\n){1,2}");

    @Override
    public SubtitleObject parse(String text) {
        SubtitleObject subtitleObject = new SubtitleObject();

        Matcher matcher = PARSE_PATTERN.matcher(text);
        while (matcher.find()) {
            String startTime = matcher.group(3);
            String endTime = matcher.group(4);
            String content = matcher.group(6);

            Subtitle subtitle = new Subtitle(content, parseTime(startTime), parseTime(endTime), null);
            subtitleObject.addSubtitle(subtitle);
        }
        return subtitleObject;
    }

    public String serialize(SubtitleObject tto) {
        StringBuilder result = new StringBuilder();
        int captionNumber = 1;

        for (Subtitle subtitle : tto.subtitles.values()) {
            //number is written
            result.append(captionNumber++).append(LINE_SEPARATOR);
            //time is written
            result.append(serializeTime(subtitle.startTime))
                    .append(" --> ")
                    .append(serializeTime(subtitle.endTime))
                    .append(LINE_SEPARATOR);
            //text is added
            result.append(subtitle.content).append(LINE_SEPARATOR);

            //we add the next blank line
            result.append(LINE_SEPARATOR);
        }
        return result.toString();
    }

    private int parseTime(String text) {
        // this type of format:  01:02:22,501
        int h, m, s, ms;
        h = Integer.parseInt(text.substring(0, 2));
        m = Integer.parseInt(text.substring(3, 5));
        s = Integer.parseInt(text.substring(6, 8));
        ms = Integer.parseInt(text.substring(9, 12));

        return (ms + s * 1000 + m * 60000 + h * 3600000);
    }

    private String serializeTime(int milliseconds) {
        // this type of format:  01:02:22,501
        StringBuilder time = new StringBuilder();
        String aux;
        int h, m, s, ms;

        h = milliseconds / 3600000;
        aux = String.valueOf(h);
        if (aux.length() == 1) time.append('0');
        time.append(aux);
        time.append(':');
        m = (milliseconds / 60000) % 60;
        aux = String.valueOf(m);
        if (aux.length() == 1) time.append('0');
        time.append(aux);
        time.append(':');
        s = (milliseconds / 1000) % 60;
        aux = String.valueOf(s);
        if (aux.length() == 1) time.append('0');
        time.append(aux);
        time.append(',');
        ms = milliseconds % 1000;
        aux = String.valueOf(ms);
        if (aux.length() == 1) time.append("00");
        else if (aux.length() == 2) time.append('0');
        time.append(aux);

        return time.toString();
    }

}
