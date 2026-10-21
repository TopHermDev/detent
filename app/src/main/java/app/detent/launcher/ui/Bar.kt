package app.detent.launcher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import app.detent.launcher.DeviceStatus
import app.detent.launcher.G
import app.detent.launcher.LX
import app.detent.launcher.LauncherState
import app.detent.launcher.Palette
import app.detent.launcher.W
import app.detent.launcher.snap
import kotlin.math.ceil
import kotlin.math.max

/** Pitch of the status dots on the grid layouts. Four status dots fill one grid step. */
internal const val BP = G / 4f

/** x of the last status dot on a right-hand dial. The status ends on the lens column. */
internal const val BAR_X1 = LX

/** Columns of the status dots: wifi, signal, battery, and the charge in digits. */
fun barCols(st: DeviceStatus): Int {
    val digits = if (st.battery >= 0) "${st.battery}".length * 4 - 1 else 0
    return 9 + 2 + (if (st.cell >= 0) 7 + 2 else 0) + 11 + (if (digits > 0) 2 + digits else 0)
}

/** The grid row of the status panel. It sits in the middle of the band that the Android status bar used. */
fun gridBarY(top: Float) = snap(max(top, 20f) / 2f)

/** The middle of the status band on the radial layouts. */
private fun lineBarY(top: Float) = max(top, 20f) / 2f

/**
 * Detent's own status bar: wifi, mobile signal, and battery. It replaces the Android status bar when the
 * `detent bar` is on. The radial layouts draw it with hairlines. The grid layouts draw it as a small dot matrix.
 */
@Composable
fun StatusStrip(s: LauncherState, pal: Palette, st: DeviceStatus) {
    val u = LocalUnits.current
    // The listeners run only while Detent is in front.
    val live = s.ownBar && s.resumed
    DisposableEffect(live) {
        if (live) st.start()
        onDispose { st.stop() }
    }
    val alpha by animateFloatAsState(if (s.ownBar) 1f else 0f, tween(300), label = "barAlpha")
    if (alpha < .004f) return
    val measurer = rememberTextMeasurer()
    val style = remember(u) { textStyle(u, 11f, Color.White, spacing = .02f) }
    val grid = s.layout.grid
    val left = s.left
    Canvas(Modifier.fillMaxSize()) {
        val k = u.k
        if (grid) {
            drawBarDots(st, pal, k, left, gridBarY(u.top), alpha)
            return@Canvas
        }
        val y = lineBarY(u.top)
        val text = if (st.battery >= 0) measurer.measure("${st.battery}%", style) else null
        val tw = (text?.size?.width ?: 0) / k
        val items = buildList {
            add(13f)
            if (st.cell >= 0) add(12.5f)
            if (st.battery >= 0) add(21.4f)
        }
        val width = items.sum() + 9f * max(0, items.size - 1) + if (text != null) 5f + tw else 0f
        var x = if (left) 24f else W - 24f - width
        val on = pal.fg.copy(alpha = alpha)
        val off = pal.fg.copy(alpha = .25f * alpha)
        val top = y - 5f
        // Without wifi, the wifi arcs stay dim.
        drawWifi(st.wifi, x, top, on, off, k)
        x += 13f + 9f
        if (st.cell >= 0) {
            drawSignal(st.cell, x, top, on, off, k)
            x += 12.5f + 9f
        }
        if (st.battery >= 0) {
            val fill = if (st.charging || st.battery <= 15) pal.accent.copy(alpha = alpha) else on
            drawBattery(st.battery, x, top, pal.fg.copy(alpha = .55f * alpha), fill, k)
            x += 21.4f + 5f
        }
        if (text != null) {
            val color = if (st.charging) pal.accent else pal.fg
            drawText(text, color, Offset(x * k, y * k - text.size.height / 2f), alpha = alpha)
        }
    }
}

// Hairline glyphs, 10 units high. [x] and [top] give the top left corner in design units.

private fun DrawScope.drawWifi(level: Int, x: Float, top: Float, on: Color, off: Color, k: Float) {
    val cx = (x + 6.5f) * k
    val by = (top + 10f) * k
    val line = Stroke(1.5f * k, cap = StrokeCap.Round)
    drawCircle(if (level >= 0) on else off, 1.25f * k, Offset(cx, by - 1.3f * k))
    for ((i, r) in listOf(5f, 9f).withIndex()) {
        drawArc(
            if (level > i) on else off, 225f, 90f, false,
            Offset(cx - r * k, by - r * k), Size(2 * r * k, 2 * r * k), style = line,
        )
    }
}

private fun DrawScope.drawSignal(level: Int, x: Float, top: Float, on: Color, off: Color, k: Float) {
    val bottom = top + 10f
    for (i in 0 until 4) {
        val bx = x + 1f + i * 3.5f
        val h = 3.5f + 2f * i
        drawLine(
            if (level > i) on else off, Offset(bx * k, (bottom - 1f) * k), Offset(bx * k, (bottom - h + 1f) * k),
            strokeWidth = 2f * k, cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawBattery(level: Int, x: Float, top: Float, rim: Color, fill: Color, k: Float) {
    drawRoundRect(
        rim, Offset(x * k, (top + .75f) * k), Size(19f * k, 8.5f * k), CornerRadius(2.4f * k), style = Stroke(1.1f * k),
    )
    drawRoundRect(rim, Offset((x + 19.8f) * k, (top + 3.25f) * k), Size(1.6f * k, 3.5f * k), CornerRadius(.8f * k))
    if (level > 0) {
        val w = max(1.5f, 15f * level / 100f)
        drawRoundRect(fill, Offset((x + 2f) * k, (top + 2.75f) * k), Size(w * k, 4.5f * k), CornerRadius(1f * k))
    }
}

// The dot matrix of the grid layouts: 5 rows of dots on the grid row of the status.

private val WIFI = listOf("..ooooo..", ".o.....o.", "o..iii..o", "..i...i..", "....d....")
private val DIGITS = mapOf(
    '0' to "### #.# #.# #.# ###", '1' to ".#. ##. .#. .#. ###", '2' to "### ..# ### #.. ###",
    '3' to "### ..# .## ..# ###", '4' to "#.# #.# ### ..# ..#", '5' to "### #.. ### ..# ###",
    '6' to "### #.. ### #.# ###", '7' to "### ..# ..# .#. .#.", '8' to "### #.# ### #.# ###",
    '9' to "### #.# ### ..# ###",
).mapValues { it.value.split(' ') }

private const val ON = 1
private const val RIM = 2
private const val HOT = 3

private fun DrawScope.drawBarDots(st: DeviceStatus, pal: Palette, k: Float, left: Boolean, y: Float, alpha: Float) {
    val cols = barCols(st)
    val cells = IntArray(cols * 5)
    fun set(c: Int, r: Int, v: Int) {
        if (c in 0 until cols) cells[r * cols + c] = v
    }
    // Wifi: a dot and two arcs. Without wifi, the glyph stays dim.
    for (r in 0 until 5) for (i in 0 until 9) {
        val lit = when (WIFI[r][i]) {
            'd' -> st.wifi >= 0
            'i' -> st.wifi >= 1
            'o' -> st.wifi >= 2
            else -> continue
        }
        set(i, r, if (lit) ON else RIM)
    }
    var c = 11
    // Signal: 4 bars that grow to the right.
    if (st.cell >= 0) {
        for (b in 0 until 4) for (r in 3 - b until 5) set(c + 2 * b, r, if (st.cell > b) ON else RIM)
        c += 9
    }
    // Battery: an outline with 8 columns of charge and a tip.
    if (st.battery >= 0) {
        val fill = ceil(st.battery / 12.5f).toInt().coerceIn(0, 8)
        val hot = if (st.charging || st.battery <= 15) HOT else ON
        for (i in 0 until 10) {
            set(c + i, 0, RIM)
            set(c + i, 4, RIM)
        }
        for (r in 1..3) {
            set(c, r, RIM)
            set(c + 9, r, RIM)
            for (i in 1..fill) set(c + i, r, hot)
        }
        set(c + 10, 2, RIM)
        c += 13
        for (ch in "${st.battery}") {
            val g = DIGITS[ch] ?: continue
            for (r in 0 until 5) for (i in 0 until 3) if (g[r][i] == '#') set(c + i, r, if (st.charging) HOT else ON)
            c += 4
        }
    }

    // The status keeps to the dial side. It ends on the lens column.
    val x0 = if (left) W - BAR_X1 else BAR_X1 - (cols - 1) * BP
    for (r in 0 until 5) for (i in 0 until cols) {
        val color: Color
        val rad: Float
        val a: Float
        when (cells[r * cols + i]) {
            ON -> { color = pal.fg; rad = 1.05f; a = .95f }
            HOT -> { color = pal.accent; rad = 1.05f; a = 1f }
            RIM -> { color = pal.fg; rad = .85f; a = .3f }
            else -> continue
        }
        drawCircle(color, rad * k, Offset((x0 + i * BP) * k, (y + (r - 2) * BP) * k), alpha = a * alpha)
    }
}
