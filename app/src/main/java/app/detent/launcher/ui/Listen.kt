package app.detent.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import app.detent.launcher.Ease
import app.detent.launcher.Hear
import app.detent.launcher.LauncherState
import app.detent.launcher.Palette
import app.detent.launcher.Pose
import app.detent.launcher.W
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/** The voice sheet. A field of dots ripples with the voice, and the app name shows as soon as the words name one app. */
@Composable
fun VoiceSheet(s: LauncherState, pal: Palette) {
    val u = LocalUnits.current
    val move = (40 * u.k).toInt()
    // The sheet keeps its last phase while it fades out.
    var phase by remember { mutableStateOf(Hear.Wait) }
    s.hear?.let { phase = it }
    AnimatedVisibility(
        s.hear != null,
        enter = fadeIn(tween(300)) + slideInVertically(tween(400, easing = Ease)) { move },
        exit = fadeOut(tween(300)) + slideOutVertically(tween(400, easing = Ease)) { move },
    ) {
        Box(Modifier.fillMaxSize().background(pal.sheet).tap { s.voiceSheetTap() }) {
            Field(s, pal, phase, Modifier)
            Column(Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(u.dp(max(60f, u.top + 20f))))
                Row(
                    Modifier.fillMaxWidth().height(u.dp(52f)).padding(start = u.dp(28f), end = u.dp(18f)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val status = when (phase) {
                        Hear.Wait -> "starting"
                        Hear.Talk -> "listening"
                        Hear.Think -> "thinking"
                        Hear.Miss -> "no app heard"
                    }
                    Txt(status, 11f, pal.muted, Modifier.weight(1f), spacing = .06f, maxLines = 1)
                    Box(Modifier.height(u.dp(44f)).tap { s.closeVoice() }.padding(horizontal = u.dp(10f)), contentAlignment = Alignment.Center) {
                        Txt("cancel", 13f, pal.muted2)
                    }
                }
            }
            Heard(s, pal, phase, Modifier.place(u, 0f, u.hd * .3f))
        }
    }
}

/** The words so far, and below them the app that they name. */
@Composable
private fun Heard(s: LauncherState, pal: Palette, phase: Hear, modifier: Modifier) {
    val u = LocalUnits.current
    val words = s.heardText
    val app = s.heardApp
    Column(modifier.fillMaxWidth().padding(horizontal = u.dp(28f))) {
        val text = when {
            phase == Hear.Miss -> "tap to try again"
            words.isNotEmpty() -> words
            phase == Hear.Think -> "…"
            else -> "say an app name"
        }
        val quiet = phase == Hear.Miss || words.isEmpty()
        Txt(text, 34f, if (quiet) pal.muted else pal.fg, weight = FontWeight.Light, spacing = -.03f, maxLines = 3)
        Spacer(Modifier.height(u.dp(18f)))
        // The name keeps its place when no app matches, so the open animation always has a start point.
        var name by remember { mutableStateOf("") }
        if (app != null) name = app.display
        val alpha by animateFloatAsState(if (app != null && phase != Hear.Miss) 1f else 0f, tween(200), label = "heardApp")
        Row(
            Modifier.alpha(alpha),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(u.dp(10f)),
        ) {
            Box(Modifier.size(u.dp(6f)).clip(CircleShape).background(pal.accent))
            Txt(
                name, 16f, pal.accent,
                Modifier.onGloballyPositioned {
                    val p = it.positionInRoot()
                    s.heardPose = Pose(p.x / u.k, (p.y + it.size.height / 2f) / u.k, 0f, 16f / 44f, 0f)
                },
                maxLines = 1,
            )
        }
    }
}

/**
 * A field of dots in the lower half of the sheet. Rings run out from the voice button and swell with the voice level.
 * While the service thinks, one slow ring runs out.
 */
@Composable
private fun Field(s: LauncherState, pal: Palette, phase: Hear, modifier: Modifier) {
    val u = LocalUnits.current
    var t by remember { mutableFloatStateOf(0f) }
    var level by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 16f else (now - last) / 1e6f
                last = now
                t += dt / 1000f
                level += (s.voiceLevel - level) * (1f - .8f.pow(dt / 16f))
            }
        }
    }
    val top = u.hd * .5f
    val (sx, sy) = s.voiceOrigin
    Canvas(modifier.fillMaxSize()) {
        val k = u.k
        val gap = 18f
        val cols = (W / gap).toInt()
        val x0 = (W - (cols - 1) * gap) / 2f
        var y = top
        while (y < u.hd - u.bottom - 12f) {
            // The field fades in from its top edge.
            val fade = ((y - top) / 120f).coerceIn(0f, 1f)
            for (i in 0 until cols) {
                val x = x0 + i * gap
                val d = hypot(x - sx, y - sy)
                val amp = when (phase) {
                    Hear.Wait -> .25f * exp(-d / 60f) * (.5f + .5f * sin(t * 4f))
                    Hear.Talk -> {
                        val ring = (.5f + .5f * sin(d * .07f - t * 6f)).pow(3)
                        exp(-d / 260f) * (.08f + level * ring)
                    }
                    Hear.Think -> .7f * exp(-((d - (t * 260f) % 640f) / 34f).let { it * it })
                    Hear.Miss -> 0f
                }
                val c = lerp(pal.dotMin, pal.accent, (amp * 1.6f).coerceIn(0f, 1f))
                drawCircle(c.copy(alpha = c.alpha * fade), (1.2f + amp * 4.2f) * k, Offset(x * k, y * k))
            }
            y += gap
        }
    }
}
