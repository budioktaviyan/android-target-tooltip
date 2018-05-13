@file:JvmName("Typefaces")
package id.kotlin.tooltip

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import java.util.Hashtable

private const val TAG = "Typefaces"
private val FONT_CACHE = Hashtable<String, Typeface>()

internal fun get(ctx: Context, assetPath: String): Typeface? {
    return synchronized(FONT_CACHE) {
        if (!FONT_CACHE.containsKey(assetPath)) {
            try {
                val typeface = Typeface.createFromAsset(ctx.assets, assetPath)
                FONT_CACHE[assetPath] = typeface
            } catch (e: Exception) {
                Log.e(TAG, "Could not get typeface $assetPath because ${e.message}")
                return null
            }
        }

        FONT_CACHE[assetPath]
    }
}