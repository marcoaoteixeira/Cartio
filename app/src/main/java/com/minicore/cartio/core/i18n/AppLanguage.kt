package com.minicore.cartio.core.i18n

import androidx.annotation.StringRes
import com.minicore.cartio.R

enum class AppLanguage(val tag: String?, @StringRes val labelRes: Int) {
    SYSTEM_DEFAULT(null, R.string.language_system_default),
    ENGLISH("en", R.string.language_english),
    PORTUGUESE("pt", R.string.language_portuguese),
    SPANISH("es", R.string.language_spanish),
    ITALIAN("it", R.string.language_italian),
    FRENCH("fr", R.string.language_french);

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.tag == tag } ?: SYSTEM_DEFAULT
    }
}
