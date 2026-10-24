package com.minicore.cartio.features.settings.data

import com.minicore.cartio.core.i18n.AppLanguage
import kotlinx.coroutines.flow.StateFlow

interface AppLanguagePreferences {
    val currentLanguage: StateFlow<AppLanguage>
    fun setLanguage(language: AppLanguage)
}
