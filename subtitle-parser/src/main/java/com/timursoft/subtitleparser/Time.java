package com.timursoft.subtitleparser;

public class Time {

    public static final String SRT_FORMAT = "hh:mm:ss,ms";
    public static final String ASS_FORMAT = "h:mm:ss.cs";

    // in an integer we can store 24 days worth of milliseconds, no need for a long
    public int mseconds;

    /**
     * @param format supported formats: "hh:mm:ss,ms", "h:mm:ss.cs"
     * @param value  string in the correct format
     */
    public Time(String format, String value) {
        if (format.equalsIgnoreCase(Time.SRT_FORMAT)) {
            // this type of format:  01:02:22,501 (used in .SRT)
            int h, m, s, ms;
            h = Integer.parseInt(value.substring(0, 2));
            m = Integer.parseInt(value.substring(3, 5));
            s = Integer.parseInt(value.substring(6, 8));
            ms = Integer.parseInt(value.substring(9, 12));

            mseconds = ms + s * 1000 + m * 60000 + h * 3600000;

        } else if (format.equalsIgnoreCase(Time.ASS_FORMAT)) {
            // this type of format:  1:02:22.51 (used in .ASS/.SSA)
            int h, m, s, cs;
            String[] hms = value.split(":");
            h = Integer.parseInt(hms[0]);
            m = Integer.parseInt(hms[1]);
            s = Integer.parseInt(hms[2].substring(0, 2));
            cs = Integer.parseInt(hms[2].substring(3, 5));

            mseconds = cs * 10 + s * 1000 + m * 60000 + h * 3600000;
        }
    }

    /**
     * Method to return a formatted value of the time stored
     *
     * @param format supported formats: "hh:mm:ss,ms", "h:mm:ss.cs"
     * @return formatted time in a string
     */
    public String getTime(String format) {
        //we use string builder for efficiency
        StringBuilder time = new StringBuilder();
        String aux;
        if (format.equalsIgnoreCase(Time.SRT_FORMAT)) {
            // this type of format:  01:02:22,501 (used in .SRT)
            int h, m, s, ms;
            h = mseconds / 3600000;
            aux = String.valueOf(h);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append(':');
            m = (mseconds / 60000) % 60;
            aux = String.valueOf(m);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append(':');
            s = (mseconds / 1000) % 60;
            aux = String.valueOf(s);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append(',');
            ms = mseconds % 1000;
            aux = String.valueOf(ms);
            if (aux.length() == 1) time.append("00");
            else if (aux.length() == 2) time.append('0');
            time.append(aux);

        } else if (format.equalsIgnoreCase(Time.ASS_FORMAT)) {
            // this type of format:  1:02:22.51 (used in .ASS/.SSA)
            int h, m, s, cs;
            h = mseconds / 3600000;
            aux = String.valueOf(h);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append(':');
            m = (mseconds / 60000) % 60;
            aux = String.valueOf(m);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append(':');
            s = (mseconds / 1000) % 60;
            aux = String.valueOf(s);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append('.');
            cs = (mseconds / 10) % 100;
            aux = String.valueOf(cs);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
        }

        return time.toString();
    }

    /**
     * ASS/SSA time format.
     *
     * @return ASS/SSA time format.
     */
    @Override
    public String toString() {
        return getTime(Time.ASS_FORMAT);
    }

}
