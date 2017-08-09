package getyourcasts.jd.com.getyourcasts.util;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chuondao on 8/9/17.
 */


public final class TimeUtil {

    /**
     * get currenttime in Millisec
     */
    public static String getCurrentTimeInMs() {
        return Calendar.getInstance().get(Calendar.MILLISECOND) + "";
    }

    private static final String datePatternStr = "^(.*),\\s*([0-9][0-9])\\s*([A-Z]*[a-z]*)\\s*([0-9]*)";

    private static final Pattern datePattern = Pattern.compile(datePatternStr);

    public static DatePub parseDatePub(String raw) {
        Matcher matcher = datePattern.matcher(raw);
        boolean found = matcher.find();
        if (found) {
            return new DatePub(matcher.group(2), matcher.group(3), matcher.group(4));
        }

        return null;
    }


    static class DatePub {
        private String dayOfMonth;
        private String month;
        private String year;

        public DatePub(String dayOfMonth, String month, String year) {
            this.dayOfMonth = dayOfMonth;
            this.month = month;
            this.year = year;
        }

        public String getDayOfMonth() {
            return dayOfMonth;
        }

        public String getMonth() {
            return month;
        }

        public String getYear() {
            return year;
        }

        public void setDayOfMonth(String dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public void setYear(String year) {
            this.year = year;
        }
    }
}

