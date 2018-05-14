@file:JvmName("Utils")
package id.kotlin.tooltip

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.util.Log

internal fun getActivity(ctx: Context?): Activity? = when (ctx) {
    is Activity -> ctx
    is ContextWrapper -> getActivity(ctx.baseContext)
    else -> null
}

internal fun log(tag: String, level: Int, format: String, vararg args: Any) {
    Tooltip.dbg.run {
        when (level) {
            Log.DEBUG -> Log.d(tag, String.format(format, *args))
            Log.ERROR -> Log.e(tag, String.format(format, *args))
            Log.INFO -> Log.i(tag, String.format(format, *args))
            Log.WARN -> Log.w(tag, String.format(format, *args))
            Log.VERBOSE -> Log.v(tag, String.format(format, *args))
            else -> Log.v(tag, String.format(format, *args))
        }
    }
}

internal fun equals(first: Any?, second: Any?): Boolean = if (null == first) second == null else first == second

internal fun rectContainsRectWithTolerance(parentRect: Rect, childRect: Rect, type: Int): Boolean = !parentRect.contains(
        childRect.left + type, childRect.top + type, childRect.right - type, childRect.bottom - type
)