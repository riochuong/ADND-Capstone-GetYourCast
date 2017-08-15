package getyourcasts.jd.com.getyourcasts.util;

/**
 * Created by chuondao on 8/14/17.
 */

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public final class ButtonStateUtil {
    @Retention(SOURCE)
    @IntDef({PRESS_TO_DOWNLOAD, PRESS_TO_STOP_DOWNLOAD, PRESS_TO_PLAY, PRESS_TO_PAUSE,PRESS_TO_UNPAUSE})
    public @interface ButtonState {}
    // STATE OF FAB
    public static final int PRESS_TO_DOWNLOAD = 0;
    public static final int  PRESS_TO_STOP_DOWNLOAD =1;
    public static final int  PRESS_TO_PLAY = 2;
    public static final int  PRESS_TO_PAUSE = 3;
    public static final int  PRESS_TO_UNPAUSE = 4;

}






