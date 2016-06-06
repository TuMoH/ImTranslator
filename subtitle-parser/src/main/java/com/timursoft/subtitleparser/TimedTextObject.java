package com.timursoft.subtitleparser;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

/**
 * These objects can (should) only be created through the implementations of parseFile() in the {@link TimedTextFileFormat} interface
 * They are an object representation of a subtitle file and contain all the subtitles and associated styles.
 */
public class TimedTextObject {

    /*
     * Attributes
     *
     */
    //meta info
    public String title = "";
    public String description = "";
    public String copyrigth = "";
    public String author = "";
    public String fileName = "";
    public String language = "";

    //list of styles (id, reference)
    public Map<String, Style> styling = new HashMap<>();

    //list of layouts (id, reference)
    public Map<String, Region> layout = new HashMap<>();

    //list of subtitles (begin time, reference)
    //represented by a tree map to maintain order
    public Map<Integer, Subtitle> subtitles = new TreeMap<>();

    //to store non fatal errors produced during parsing
    public String warnings = "List of non fatal errors produced during parsing:\n\n";

    //**** OPTIONS *****
    //to know whether file should be saved as .ASS or .SSA
    public boolean useASSInsteadOfSSA = true;
    //to delay or advance the subtitles, parsed into +/- milliseconds
    public int offset = 0;

    //to know if a parsing method has been applied
    public boolean built = false;


    /**
     * Protected constructor so it can't be created from outside
     */
    protected TimedTextObject() {
    }

	/* 
     * PROTECTED METHODS
	 * 
	 */

    /**
     * This method simply checks the style list and eliminate any style not referenced by any caption
     * This might come useful when default styles get created and cover too much.
     * It require a unique iteration through all subtitles.
     */
    protected void cleanUnusedStyles() {
        //here all used styles will be stored
        Hashtable<String, Style> usedStyles = new Hashtable<>();
        //we iterate over the subtitles
        for (Subtitle subtitle : subtitles.values()) {
            //new caption
            //if it has a style
            if (subtitle.style != null) {
                String iD = subtitle.style.iD;
                //if we haven't saved it yet
                if (!usedStyles.containsKey(iD))
                    usedStyles.put(iD, subtitle.style);
            }
        }
        //we saved the used styles
        this.styling = usedStyles;
    }

}
