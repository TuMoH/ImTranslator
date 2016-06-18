package com.timursoft.subtitleparser;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Created by TuMoH on 05.06.2016.
 */
public class FormatSRTTest {

    private FormatSRT formatSRT;
    private String text;

    @Before
    public void init() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("srt.srt");
        text = IOHelper.streamToString(is).replace("\uFEFF", "");
        formatSRT = new FormatSRT();
    }

    @Test
    public void parse() throws Exception {
        SubtitleObject subtitleObject = formatSRT.parse(text);
        Iterator<Subtitle> iterator = subtitleObject.subtitles.values().iterator();
        Subtitle subtitle = iterator.next();

        assertEquals(2, subtitleObject.subtitles.size());
        assertEquals(27160, subtitle.startTime);
        assertEquals(30000, subtitle.endTime);
        assertEquals("<i>The Tesseract has awakened.</i>", subtitle.content);

        subtitle = iterator.next();

        assertEquals(31760, subtitle.startTime);
        assertEquals(36515, subtitle.endTime);
        assertEquals("It is on a little world, a human world.", subtitle.content);
    }

    @Test
    public void serialize() throws Exception {
        SubtitleObject subtitleObject = new SubtitleObject();

        Subtitle subtitle = new Subtitle();
        subtitle.startTime = 27160;
        subtitle.endTime = 30000;
        subtitle.content = "<i>The Tesseract has awakened.</i>";
        subtitleObject.subtitles.put(27160, subtitle);

        Subtitle subtitle2 = new Subtitle();
        subtitle2.startTime = 31760;
        subtitle2.endTime = 36515;
        subtitle2.content = "It is on a little world, a human world.";
        subtitleObject.subtitles.put(31760, subtitle2);

        String result = formatSRT.serialize(subtitleObject);
        assertEquals(text, result);
    }

    @Test
    public void twoWay() throws Exception {
        SubtitleObject subtitleObject = formatSRT.parse(text);
        String result = formatSRT.serialize(subtitleObject);

        assertEquals(text, result);
    }

}