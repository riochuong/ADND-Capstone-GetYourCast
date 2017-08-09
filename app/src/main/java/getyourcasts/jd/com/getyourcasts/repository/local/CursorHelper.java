package getyourcasts.jd.com.getyourcasts.repository.local;

import android.database.Cursor;

/**
 * Created by chuondao on 8/9/17.
 */

public class CursorHelper {
    public static String getStringValue(Cursor c, String colName) {
        int colIdx = c.getColumnIndex(colName);
        if (colIdx >= 0){
            return c.getString(colIdx);
        }
        return null;
}

public static int getIntValue(Cursor c, String colName) {
    int colIdx = c.getColumnIndex(colName);
    if (colIdx >= 0){
        return c.getInt(colIdx);
    }
    return -1;
}

}
