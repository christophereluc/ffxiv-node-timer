package com.rayluc.ffxivnodetimer.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by chris on 7/10/16.
 */
public final class Util {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private final static double TIME_MULITPLIER = 20.571428571428573;

    private Util() {

    }

    public static Calendar getEorzeanTime() {
        long localTime = System.currentTimeMillis();
        long eorzeanTime = (long) (localTime * TIME_MULITPLIER);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(eorzeanTime);
        return calendar;
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
        Calendar calendar = getEorzeanTime();
        Calendar calendarCopy = getEorzeanTime();
        calendarCopy.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.substring(0, 2)));
        calendarCopy.set(Calendar.MINUTE, Integer.valueOf(time.substring(3)));
        calendarCopy.set(Calendar.SECOND, 0);
        calendarCopy.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() >= calendarCopy.getTimeInMillis()) {
            calendarCopy.add(Calendar.DATE, 1);
        }

        return (long) (calendarCopy.getTimeInMillis() / TIME_MULITPLIER);
    }

}
