package getyourcasts.jd.com.getyourcasts.util

/**
 * TimeUtil helps to create timestamp for
 * updated time of the episode.
 */
import java.util.Calendar

class TimeUtil {
    companion object {
        /**
         * get currenttime in Millisec
         */
        fun getCurrentTimeInMs(): String {
            return Calendar.getInstance().get(Calendar.MILLISECOND).toString()
        }
    }

}