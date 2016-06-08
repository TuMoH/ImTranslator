package com.timursoft.subtitleparser;

public class Subtitle {

    public Style style;
    public Region region;

    public Time start;
    public Time end;

    /**
     * Raw content, before cleaning up templates and markup.
     */
    public String rawContent = "";
    /**
     * Cleaned-up subtitle content.
     */
    public String content = "";

    public String translatedContent = null;

    @Override
    public String toString() {
        return "Subtitle{" +
                start + ".." + end +
                ", " + (style != null ? style.iD : null) + ", " + region + ": " + content +
                '}';
    }
}
