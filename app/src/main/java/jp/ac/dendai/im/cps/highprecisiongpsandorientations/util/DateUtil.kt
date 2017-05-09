package jp.ac.dendai.im.cps.highprecisiongpsandorientations.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    fun parseDate(timeMillis: Long): String {
        val df = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.JAPAN)
        val date = Date(timeMillis)
        return df.format(date)
    }
}