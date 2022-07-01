package com.pingfangx.plugin.templatex.extension

import java.util.*

/**
 * 消息的扩展
 *
 * @author pingfangx
 * @date 2022/7/5
 */
fun String.toSentenceCase(): String =
    lowercase().capitalizeCompat()

fun String.capitalizeCompat(): String =
    replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            it.toString()
        }
    }