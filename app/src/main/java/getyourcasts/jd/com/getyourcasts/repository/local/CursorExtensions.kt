package getyourcasts.jd.com.getyourcasts.repository.local

import android.database.Cursor

/**
 * Created by chuondao on 7/27/17.
 */

fun Cursor.getString(colName: String): String? {
        val colIdx = this.getColumnIndex(colName)
        if (colIdx >= 0){
            return this.getString(colName)
        }
        return null
}

fun Cursor.getInt(colName: String): Int? {
    val colIdx = this.getColumnIndex(colName)
    if (colIdx >= 0){
        return this.getInt(colName)
    }
    return null
}