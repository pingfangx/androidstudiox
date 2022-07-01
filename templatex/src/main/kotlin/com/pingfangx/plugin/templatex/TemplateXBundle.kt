package com.pingfangx.plugin.templatex

import com.intellij.DynamicBundle
import com.pingfangx.plugin.templatex.extension.toSentenceCase
import org.jetbrains.annotations.PropertyKey

/**
 * messages
 *
 * @author pingfangx
 * @date 2022/7/4
 */
private const val BUNDLE: String = "messages.TemplateXBundle"

object TemplateXBundle : DynamicBundle(BUNDLE) {
    @JvmStatic
    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any
    ): String = getMessage(key, *params)

    @JvmStatic
    fun messagePointer(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any
    ): java.util.function.Supplier<String> = getLazyMessage(key, *params)

    fun messageWithSentenceCase(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        message(key, params).toSentenceCase()
}