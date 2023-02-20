package com.kazumaproject.markdownnote.other

import android.content.Context
import java.util.*
import kotlin.math.max

object DateAgoCalculator {
    enum class TimeUnit {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        MONTH,
        YEAR
    }

    data class Time(
        val value: Int,
        val unit: TimeUnit
    )

    /** 過去時刻を現在時刻と比較して何分前、何時間前、何か月前になるか計算する */
    fun calc(now: Date, past: Date): Time {
        val diffSec = (now.time - past.time) / 1000L

        // 1分未満であれば秒で返す（マイナスの場合は0秒に）
        if (diffSec < 60)
            return Time(max(0, diffSec.toInt()), TimeUnit.SECOND)

        // 1時間未満であれば分で返す
        if (diffSec < 60 * 60)
            return Time((diffSec / 60).toInt(), TimeUnit.MINUTE)

        // 1日未満であれば時間で返す
        if (diffSec < 60 * 60 * 24)
            return Time((diffSec / (60 * 60)).toInt(), TimeUnit.HOUR)

        // 以降、月によって1ヵ月の日数が違うため、カレンダーで計算する
        val nowCalendar = Calendar.getInstance(TimeZone.getDefault())
        val pastCalendar = Calendar.getInstance(TimeZone.getDefault())
        nowCalendar.time = now
        pastCalendar.time = past
        // 1ヵ月未満であれば日で返す
        if (nowCalendar < getModifiedCalender(pastCalendar, Calendar.MONTH, 1)) {
            // 同じ月の場合
            if (nowCalendar.get(Calendar.MONTH) == pastCalendar.get(Calendar.MONTH))
                return Time(nowCalendar.get(Calendar.DAY_OF_MONTH) - pastCalendar.get(Calendar.DAY_OF_MONTH) + compareTime(nowCalendar, pastCalendar), TimeUnit.DAY)

            // 違う月の場合
            return Time(nowCalendar.get(Calendar.DAY_OF_MONTH) - (pastCalendar.get(Calendar.DAY_OF_MONTH) - pastCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) +  + compareTime(nowCalendar, pastCalendar), TimeUnit.DAY)
        }

        // 1年未満であれば月で返す
        if (nowCalendar < getModifiedCalender(pastCalendar, Calendar.YEAR, 1)) {
            for (i in 2..12) {
                if (nowCalendar < getModifiedCalender(pastCalendar, Calendar.MONTH, i))
                    return Time(i - 1, TimeUnit.MONTH)
            }
            return Time(12, TimeUnit.MONTH)
        }

        // 1年以上であれば年で返す
        return Time(nowCalendar.get(Calendar.YEAR) - pastCalendar.get(Calendar.YEAR) + compareDate(nowCalendar, pastCalendar), TimeUnit.YEAR)
    }

    /** 過去時刻を現在時刻と比較して何分前、何時間前、何か月前という文字列を取得する */
    fun getLabel(now: Date, past: Date, context: Context): String {
        val timeAgo = calc(now, past)
        if (context.resources.configuration.locales[0] == Locale.JAPAN || context.resources.configuration.locales[0] == Locale.JAPANESE) {
            return when (timeAgo.unit) {
                TimeUnit.SECOND -> "${timeAgo.value}秒前"    // 1分未満は0分前表示
                TimeUnit.MINUTE -> "${timeAgo.value}分前"
                TimeUnit.HOUR -> "${timeAgo.value}時間前"
                TimeUnit.DAY -> "${timeAgo.value}日前"
                TimeUnit.MONTH -> past.time.convertLongToTime()
                TimeUnit.YEAR -> past.time.convertLongToTime()
            }
        } else {
            return when (timeAgo.unit) {
                TimeUnit.SECOND -> "${timeAgo.value}s"    // 1分未満は0分前表示
                TimeUnit.MINUTE -> "${timeAgo.value}m"
                TimeUnit.HOUR -> "${timeAgo.value}h"
                TimeUnit.DAY -> "${timeAgo.value}d"
                TimeUnit.MONTH -> past.time.convertLongToTime()
                TimeUnit.YEAR -> past.time.convertLongToTime()
            }
        }
    }

    // 日付操作したカレンダーを取得する（元のカレンダーインスタンスは値を変更したくない）
    private fun getModifiedCalender(base: Calendar, unit: Int, add: Int): Calendar {
        val result = base.clone() as Calendar
        return result.apply { this.add(unit, add) }
    }

    private fun compareDate(now: Calendar, past: Calendar): Int {
        val now_ = now.clone() as Calendar
        now_.set(Calendar.YEAR, past.get(Calendar.YEAR))
        return if (past > now_) -1 else 0
    }

    private fun compareTime(now: Calendar, past: Calendar): Int {
        val now_ = now.clone() as Calendar
        now_.set(Calendar.YEAR, past.get(Calendar.YEAR))
        now_.set(Calendar.MONTH, past.get(Calendar.MONTH))
        now_.set(Calendar.DATE, past.get(Calendar.DATE))
        return if (past > now_) -1 else 0
    }
}