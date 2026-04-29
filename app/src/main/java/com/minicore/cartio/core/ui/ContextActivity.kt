package com.minicore.cartio.core.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Walks the [ContextWrapper] chain until it finds an [Activity], or null.
 * Safer than `LocalContext.current as Activity` — that cast crashes inside
 * `@Preview` scopes and any future composition that delivers a non-Activity
 * Context.
 */
fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
