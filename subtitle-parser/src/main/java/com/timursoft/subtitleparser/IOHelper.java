package com.timursoft.subtitleparser;

import java.io.InputStream;
import java.util.Scanner;

public final class IOHelper {

    private IOHelper() {
    }

    public static String streamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
