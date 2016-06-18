package com.timursoft.subtitleparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that represents the .ASS and .SSA subtitle file format
 */
public class FormatASS implements Format {

    private final static Pattern BLOCK_PATTERN = Pattern.compile("\\[(.*)\\](\r\n|\n)([^\\[]*)");

    public static final String SCRIPT_INFO = "Script Info";
    public static final String V4_STYLES = "V4+ Styles";
    public static final String EVENTS = "Events";
    public static final String FONTS = "Fonts";
    public static final String GRAPHICS = "Graphics";

    public SubtitleObject parse(String text) {
        SubtitleObject subtitleObject = new SubtitleObject();

        Matcher matcher = BLOCK_PATTERN.matcher(text);
        while (matcher.find()) {
            String head = matcher.group(1);
            String body = matcher.group(3).trim();

            if (SCRIPT_INFO.equalsIgnoreCase(head)) {
                subtitleObject.scriptInfoText = body;
            } else if (V4_STYLES.equalsIgnoreCase(head)) {
                subtitleObject.stylesText = body;
            } else if (EVENTS.equalsIgnoreCase(head)) {
                String[] lines = body.split("\r\n|\n");
                String[] dialogueFormat = lines[0].split(":")[1].trim().split(",");
                for (String line : lines) {
                    if (line.startsWith("Dialogue:")) {
                        //we parse the dialogue
                        Subtitle subtitle = parseDialogue(line.split(":", 2)[1].trim()
                                .split(",", dialogueFormat.length), dialogueFormat);
                        subtitleObject.addSubtitle(subtitle);
                    }
                }
            } else if (FONTS.equalsIgnoreCase(head)) {
                subtitleObject.fontsText = body;
            } else if (GRAPHICS.equalsIgnoreCase(head)) {
                subtitleObject.graphicsText = body;
            }
        }
        return subtitleObject;
    }

    public String serialize(SubtitleObject tto) {
        StringBuilder result = new StringBuilder();

        result.append("[").append(SCRIPT_INFO).append("]").append(LINE_SEPARATOR);
        result.append(tto.scriptInfoText).append(LINE_SEPARATOR);
        result.append(LINE_SEPARATOR);

        if (tto.stylesText != null) {
            result.append("[").append(V4_STYLES).append("]").append(LINE_SEPARATOR);
            result.append(tto.stylesText).append(LINE_SEPARATOR);
            result.append(LINE_SEPARATOR);
        }

        result.append("[").append(EVENTS).append("]").append(LINE_SEPARATOR);
        //define the format
        result.append("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text")
                .append(LINE_SEPARATOR);
        //Next we iterate over the subtitles
        for (Subtitle subtitle : tto.subtitles.values()) {
            //for each caption
            result.append("Dialogue: 0,");
            //start time
            result.append(serializeTime(subtitle.startTime)).append(",");
            //end time
            result.append(serializeTime(subtitle.endTime)).append(",");
            //style
            if (subtitle.style != null) {
                result.append(subtitle.style);
            } else {
                result.append("Default");
            }
            //default margins are used, no name or effect is recognized
            result.append(",,0000,0000,0000,,");

            //we add the caption text with \N as line breaks  and clean of XML
            result.append(subtitle.content);
            //and we add the caption line
            result.append(LINE_SEPARATOR);
        }

        if (tto.fontsText != null) {
            result.append("[").append(FONTS).append("]").append(LINE_SEPARATOR);
            result.append(tto.fontsText).append(LINE_SEPARATOR);
            result.append(LINE_SEPARATOR);
        }
        if (tto.graphicsText != null) {
            result.append("[").append(GRAPHICS).append("]").append(LINE_SEPARATOR);
            result.append(tto.graphicsText).append(LINE_SEPARATOR);
            result.append(LINE_SEPARATOR);
        }

        return result.toString();
    }

    /**
     * This methods transforms a dialogue line from ASS according to a format definition into an Subtitle object.
     *
     * @param line           the dialogue line without its declaration
     * @param dialogueFormat the list of attributes in this dialogue line
     * @return a new Subtitle object
     */
    private Subtitle parseDialogue(String[] line, String[] dialogueFormat) {
        Subtitle subtitle = new Subtitle();

        for (int i = 0; i < dialogueFormat.length; i++) {
            String trimmedDialogueFormat = dialogueFormat[i].trim();
            //we go through every format parameter and save the interesting values
            if (trimmedDialogueFormat.equalsIgnoreCase("Style")) {
                //we save the style
                subtitle.style = line[i].trim();
            } else if (trimmedDialogueFormat.equalsIgnoreCase("Start")) {
                //we save the starting time
                subtitle.startTime = parseTime(line[i].trim());
            } else if (trimmedDialogueFormat.equalsIgnoreCase("End")) {
                //we save the starting time
                subtitle.endTime = parseTime(line[i].trim());
            } else if (trimmedDialogueFormat.equalsIgnoreCase("Text")) {
                //we save the text
                //text is cleaned before being inserted into the caption
                subtitle.content = line[i]/*.replaceAll("\\{.*?\\}", "")*/;
            }
        }
        return subtitle;
    }

    private int parseTime(String text) {
        // this type of format:  1:02:22.51
        int h, m, s, cs;
        String[] hms = text.split(":");
        h = Integer.parseInt(hms[0]);
        m = Integer.parseInt(hms[1]);
        s = Integer.parseInt(hms[2].substring(0, 2));
        cs = Integer.parseInt(hms[2].substring(3, 5));

        return (cs * 10 + s * 1000 + m * 60000 + h * 3600000);
    }

    public static String serializeTime(int milliseconds) {
        // this type of format:  1:02:22.51
        StringBuilder time = new StringBuilder();
        String aux;
        int h, m, s, cs;

        h = milliseconds / 3600000;
        time.append(h);
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
        time.append('.');
        cs = (milliseconds / 10) % 100;
        aux = String.valueOf(cs);
        if (aux.length() == 1) time.append('0');
        time.append(aux);

        return time.toString();
    }

}
