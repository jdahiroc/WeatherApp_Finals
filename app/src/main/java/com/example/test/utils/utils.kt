package com.example.test.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat

class utils {
    companion object {
        fun timestampToHumanDate(timestamp: Long,format: String): String{
            val sdf = SimpleDateFormat(format)
            return sdf.format(timestamp * 1000)
        }

        fun buildIcon(icon: String, isBigSize: Boolean = true) : String {
            return if (isBigSize) {
                "https://openweathermap.org/img/wn/$icon@4x.png"
            } else {
                "https://openweathermap.org/img/wn/$icon.png"
            }
        }
    }
}