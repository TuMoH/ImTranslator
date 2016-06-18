package com.timursoft.subtitleparser;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Created by TuMoH on 18.06.2016.
 */
public class FormatASSTest {

    private FormatASS formatASS;
    private String text;

    @Before
    public void init() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("ass.ass");
        text = IOHelper.streamToString(is).replace("\uFEFF", "");
        formatASS = new FormatASS();
    }

    @Test
    public void parse() throws Exception {
        SubtitleObject subtitleObject = formatASS.parse(text);
        Iterator<Subtitle> iterator = subtitleObject.subtitles.values().iterator();
        Subtitle subtitle = iterator.next();

        assertEquals("info", subtitleObject.scriptInfoText);
        assertEquals("styles", subtitleObject.stylesText);
        assertEquals(2, subtitleObject.subtitles.size());

        assertEquals(0, subtitle.startTime);
        assertEquals(5100, subtitle.endTime);
        assertEquals("{\\pos(320,13)}Test-group {\\b1\\c&H0BA1C6&}ImTr{\\c&H645937&\\b0} present", subtitle.content);
        assertEquals("credits (top)", subtitle.style);

        subtitle = iterator.next();

        assertEquals(53560, subtitle.startTime);
        assertEquals(57970, subtitle.endTime);
        assertEquals("- Your Majesty! \\N- eunuch Pang nothing to blame", subtitle.content);
        assertEquals("Default", subtitle.style);
    }

    @Test
    public void serialize() throws Exception {
        SubtitleObject subtitleObject = new SubtitleObject();
        subtitleObject.scriptInfoText = "info";
        subtitleObject.stylesText = "styles";

        Subtitle subtitle = new Subtitle();
        subtitle.startTime = 0;
        subtitle.endTime = 5100;
        subtitle.content = "{\\pos(320,13)}Test-group {\\b1\\c&H0BA1C6&}ImTr{\\c&H645937&\\b0} present";
        subtitle.style = "credits (top)";
        subtitleObject.subtitles.put(0, subtitle);

        Subtitle subtitle2 = new Subtitle();
        subtitle2.startTime = 53560;
        subtitle2.endTime = 57970;
        subtitle2.content = "- Your Majesty! \\N- eunuch Pang nothing to blame";
        subtitleObject.subtitles.put(53560, subtitle2);

        String text = formatASS.serialize(subtitleObject);
        assertEquals(text, text);
    }

    @Test
    public void twoWay() throws Exception {
        SubtitleObject subtitleObject = formatASS.parse(text);
        String serialized = formatASS.serialize(subtitleObject);

        assertEquals(text, serialized);
    }

}