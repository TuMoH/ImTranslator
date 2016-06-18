package com.timursoft.subtitleparser;

public class Subtitle {

    public String content;
    public int startTime;
    public int endTime;
    public String style;

    public Subtitle() {
    }

    public Subtitle(String content, int startTime, int endTime, String style) {
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.style = style;
    }

}
