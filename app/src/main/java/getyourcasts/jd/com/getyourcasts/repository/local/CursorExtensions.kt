package getyourcasts.jd.com.getyourcasts.repository.local

import android.database.Cursor

/**
 * Created by chuondao on 7/27/17.
 */

fun Cursor.getStringValue(colName: String): String? {
        val colIdx = this.getColumnIndex(colName)
        if (colIdx >= 0){
            return this.getString(colIdx)
        }
        return null
}

fun Cursor.getIntValue(colName: String): Int? {
    val colIdx = this.getColumnIndex(colName)
    if (colIdx >= 0){
        return this.getInt(colIdx)
    }
    return null
}