package com.minicore.cartio.features.settings.data

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.minicore.cartio.core.i18n.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [AppCompatDelegate.setApplicationLocales] alone silently no-ops in this app: it
 * only reliably drives the platform [LocaleManager] when at least one
 * AppCompatActivity exists, and Cartio's activities are plain ComponentActivity.
 * So on API 33+ the platform LocaleManager is called directly (which also
 * triggers activity recreation); AppCompatDelegate is kept for API 29-32
 * back-compat, where it manages its own resource-override + recreation path.
 */
@Singleton
class AppLanguagePreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppLanguagePreferences {

    private val state = MutableStateFlow(
        AppLanguage.fromTag(currentTag())
    )

    override val currentLanguage: StateFlow<AppLanguage> = state.asStateFlow()

    override fun setLanguage(language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                if (language.tag == null) LocaleList.getEmptyLocaleList()
                else LocaleList.forLanguageTags(language.tag)
        } else {
            AppCompatDelegate.setApplicationLocales(
                if (language.tag == null) LocaleListCompat.getEmptyLocaleList()
                else LocaleListCompat.forLanguageTags(language.tag)
            )
        }
        state.value = language
    }

    private fun currentTag(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales.get(0)?.toLanguageTag()
        } else {
            AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag()
        }
}
