package com.timursoft.subtitleparser;

/**
 * This interface for any format supported by the converter
 */
public interface Format {

    String LINE_SEPARATOR = "\r\n";

    SubtitleObject parse(String text);

    String serialize(SubtitleObject tto);

}
