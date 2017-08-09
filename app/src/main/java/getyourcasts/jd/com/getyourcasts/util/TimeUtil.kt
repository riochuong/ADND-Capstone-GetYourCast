//package getyourcasts.jd.com.getyourcasts.util
//
///**
// * TimeUtil helps to create timestamp for
// * updated time of the episode.
// */
//import java.util.Calendar
//import java.util.regex.Pattern
//
//class TimeUtil {
//    companion object {
//        /**
//         * get currenttime in Millisec
//         */
//        fun getCurrentTimeInMs(): String {
//            return Calendar.getInstance().get(Calendar.MILLISECOND).toString()
//        }
//
//        val datePatternStr = "^(.*),\\s*([0-9][0-9])\\s*([A-Z]*[a-z]*)\\s*([0-9]*)"
//
//        val datePattern : Pattern = Pattern.compile(datePatternStr)
//
//        fun parseDatePub (raw : String): DatePub? {
//            val matcher = datePattern.matcher(raw)
//            val found = matcher.find()
//            if (found) {
//                return DatePub(matcher.group(2), matcher.group(3), matcher.group(4))
//            }
//
//            return null
//        }
//
//    }
//
//}
//
//data class DatePub(val dayOfMonth: String,
//           val month: String,
//           val year:  String)