package com.timursoft.subtitleparser;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by TuMoH on 05.06.2016.
 */
public class FormatSRTTest {

    private InputStream is;
    private FormatSRT formatSRT;

    @Before
    public void init() throws Exception {
        is = getClass().getClassLoader().getResourceAsStream("srt.srt");
        formatSRT = new FormatSRT();
    }

    @Test
    public void parseFile() throws Exception {
        TimedTextObject timedTextObject = formatSRT.parseFile("name", is);
        Iterator<Subtitle> iterator = timedTextObject.subtitles.values().iterator();
        Subtitle subtitle = iterator.next();

        assertEquals(2, timedTextObject.subtitles.size());
        assertEquals(27160, subtitle.start.mseconds);
        assertEquals(30000, subtitle.end.mseconds);
        assertEquals("<i>The Tesseract has awakened.</i>", subtitle.content);

        subtitle = iterator.next();

        assertEquals(31760, subtitle.start.mseconds);
        assertEquals(36515, subtitle.end.mseconds);
        assertEquals("It is on a little world, a human world.", subtitle.content);
    }

}