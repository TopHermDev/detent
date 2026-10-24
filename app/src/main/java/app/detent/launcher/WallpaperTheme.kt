package app.detent.launcher

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi

/**
 * The colours that the phone draws from the wallpaper. Android 12 and higher keep five tonal palettes for
 * the wallpaper, so Detent can follow them without reading the wallpaper and without a permission. Android
 * 11 and lower keep no such palette, so [available] is false there and the option stays away.
 */
object WallpaperTheme {
    /** True when the system keeps a tonal palette for the wallpaper. */
    val available get() = Build.VERSION.SDK_INT >= 31

    private var cache: Cache? = null

    private class Cache(val dark: Boolean, val theme: ThemeDef, val pal: Palette)

    /** The wallpaper theme and its palette for the current day and night setting, or null below Android 12. */
    fun get(ctx: Context): Pair<ThemeDef, Palette>? {
        if (Build.VERSION.SDK_INT < 31) return null
        val dark = (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        cache?.let { if (it.dark == dark) return it.theme to it.pal }
        val theme = build(ctx, dark)
        val pal = Palette(theme)
        cache = Cache(dark, theme, pal)
        return theme to pal
    }

    @RequiresApi(31)
    private fun build(ctx: Context, dark: Boolean): ThemeDef {
        // The neutral scale runs from one end to the other. The background takes an end and the foreground the other.
        val n100 = tone(ctx, android.R.color.system_neutral1_100)
        val n900 = tone(ctx, android.R.color.system_neutral1_900)
        val bg = endTone(n100, n900, dark)
        return ThemeDef(
            name = "wallpaper",
            group = "system",
            light = !dark,
            bg = bg,
            fg = endTone(n100, n900, !dark),
            accent = standTone(
                tone(ctx, android.R.color.system_accent1_200),
                tone(ctx, android.R.color.system_accent1_800),
                bg,
            ),
            // No asset: the dial draws its own background and the phone keeps the wallpaper that it has.
            wall = null,
            hand = tone(ctx, android.R.color.system_neutral2_500),
        )
    }

    @RequiresApi(31)
    private fun tone(ctx: Context, @ColorRes res: Int) = ctx.getColor(res).toLong() and 0xFFFFFFFFL
}
