package com.rayluc.ffxivnodetimer.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chris on 7/10/16.
 */
public final class Util {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private final static double TIME_MULITPLIER = 20.571428571428573;

    private Util() {

    }

    public static long getEorzeanTime() {
        Calendar calendar = Calendar.getInstance();
        long localTime = calendar.getTimeInMillis();
        return (long) (localTime * TIME_MULITPLIER);
    }

    /**
     * Returns the next UTC occurrence of the input time
     *
     * @param time Eorzean time in HH:mm format
     * @return UTC time in millis of next occurrence
     */
    public static long getNextRealTimeInMillis(String time) {
        //Since this can be time sensitive, instead of relying on the Calendar object and SDF
        //to parse items and do time manipulation, I decided to make assumptions on the time
        //since I will be the one inputting it into the database
        if (time.length() > 5) {
            return -1;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getEorzeanTime());
        Calendar calendarCopy = Calendar.getInstance();
        calendarCopy.setTimeInMillis(getEorzeanTime());

        calendarCopy.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.substring(0, 2)));
        calendarCopy.set(Calendar.MINUTE, Integer.valueOf(time.substring(3)));
        calendarCopy.set(Calendar.SECOND, 0);
        calendarCopy.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() >= calendarCopy.getTimeInMillis()) {
            calendarCopy.add(Calendar.DATE, 1);
        }

        return (long) (calendarCopy.getTimeInMillis() / TIME_MULITPLIER);
    }

    public static String convert24HourToAmPm(String input) {
        Date date = null;
        try {
            date = simpleDateFormat.parse(input);
        } catch (ParseException e) {
            //Failed parsing so just return input
            return input;
        }

        return new SimpleDateFormat("hh:mm aa").format(date);
    }
}
