package com.timursoft.subtitleparser;

import java.util.Map;
import java.util.TreeMap;

/**
 * These objects can (should) only be created through the implementations of parse() in the {@link Format} interface
 * They are an object representation of a subtitle file and contain all the subtitles and associated styles.
 */
public class SubtitleObject {

    public String scriptInfoText;
    public String stylesText;
    public String fontsText;
    public String graphicsText;

    //list of subtitles (begin time, reference)
    //represented by a tree map to maintain order
    public Map<Integer, Subtitle> subtitles = new TreeMap<>();

    public void addSubtitle(Subtitle subtitle) {
        int key = subtitle.startTime;
        //in case the key is already there, we increase it by a millisecond, since no duplicates are allowed
        while (subtitles.containsKey(key)) key++;
        subtitles.put(key, subtitle);
    }

}
