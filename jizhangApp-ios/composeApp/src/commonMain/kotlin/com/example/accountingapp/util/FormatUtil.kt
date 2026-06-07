package com.example.accountingapp.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToLong

// ═══════════════════════════════════════════════════════
// 数字格式化（替代 String.format / Locale）
// ═══════════════════════════════════════════════════════

/** String.format("%.0f", x) → 四舍五入到整数 */
fun Double.format0(): String = "${(this + 0.5).roundToLong()}"

/** String.format("%.2f", x) → 保留两位小数 */
fun Double.format2(): String {
    val rounded = (this * 100).roundToLong()
    val intPart = rounded / 100
    val decPart = (rounded % 100).absoluteValue
    return "$intPart.${decPart.toString().padStart(2, '0')}"
}

/** String.format(Locale.US, "%.1f", x) → 保留一位小数 */
fun Double.format1(): String {
    val rounded = (this * 10).roundToLong()
    val intPart = rounded / 10
    val decPart = (rounded % 10).absoluteValue
    return "$intPart.$decPart"
}

fun Float.format1(): String = toDouble().format1()
fun Float.format0(): String = toDouble().format0()

// ═══════════════════════════════════════════════════════
// 日期时间格式化（替代 SimpleDateFormat / Calendar / Date）
// ═══════════════════════════════════════════════════════

/** 毫秒时间戳 → 小时（0-23） */
fun Long.toHour(): Int =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).hour

/** 毫秒时间戳 → 分钟（0-59） */
fun Long.toMinute(): Int =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).minute

/** 格式化为 "MM月dd日" */
fun Long.toMMdd(): String {
    val date = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.monthNumber.toString().padStart(2, '0')}月${date.dayOfMonth.toString().padStart(2, '0')}日"
}

/** 格式化为 "yyyy年MM月dd日" */
fun Long.toYYYYMMdd(): String {
    val date = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.year}年${date.monthNumber.toString().padStart(2, '0')}月${date.dayOfMonth.toString().padStart(2, '0')}日"
}

/** 格式化为 "MM月dd日 HH:mm" */
fun Long.toMMddHHmm(): String {
    val ldt = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${ldt.monthNumber.toString().padStart(2, '0')}月${ldt.dayOfMonth.toString().padStart(2, '0')}日 " +
           "${ldt.hour.toString().padStart(2, '0')}:${ldt.minute.toString().padStart(2, '0')}"
}

/** 格式化为 "HH:mm" */
fun Long.toHHmm(): String =
    "${toHour().toString().padStart(2, '0')}:${toMinute().toString().padStart(2, '0')}"

// ═══════════════════════════════════════════════════════
// 时间操作（替代 Calendar.set / Calendar.add）
// ═══════════════════════════════════════════════════════

/** 将毫秒时间戳的时分秒归零到当天 00:00:00 */
fun Long.startOfDay(): Long {
    val ldt = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    return LocalDateTime(ldt.date, LocalTime(0, 0))
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
}

/** 保持日期不变，替换为指定的时分 */
fun Long.withTime(hour: Int, minute: Int): Long {
    val ldt = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    return LocalDateTime(ldt.date, LocalTime(hour, minute))
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
}

/** 当前的小时数（0-23），替代 Calendar.getInstance().get(Calendar.HOUR_OF_DAY) */
fun currentHour(): Int =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
