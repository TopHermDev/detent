package app.detent.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.detent.launcher.LauncherState
import app.detent.launcher.Note
import app.detent.launcher.Palette
import kotlin.math.max

/**
 * The notification shade. It comes down over the dial, which recedes behind it. The panel shows the list
 * only: a tap or a drag up closes it. Detent reads the notifications while the panel is open and keeps none.
 */
@Composable
fun ShadePanel(s: LauncherState, pal: Palette, notes: List<Note>, reveal: Float) {
    if (!s.shade && reveal <= .001f) return
    val u = LocalUnits.current
    Column(
        Modifier.fillMaxSize()
            .graphicsLayer {
                alpha = reveal
                translationY = -(1f - reveal) * 48f * u.k
            }
            .background(pal.sheet)
            // A drag up pushes the shade away, and a tap closes it. The dial behind keeps its own gestures.
            .pointerInput(u.k) {
                detectVerticalDragGestures(
                    onDragEnd = { s.endShadeDrag() },
                    onDragCancel = { s.endShadeDrag() },
                ) { _, dy -> s.shadeDrag(dy / u.k) }
            }
            .tap { s.closeShade() },
    ) {
        Spacer(Modifier.height(u.dp(max(60f, u.top + 20f))))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = u.dp(28f)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Txt("notifications", 26f, pal.fg, Modifier.weight(1f), weight = FontWeight.Light, spacing = -.03f, maxLines = 1)
            Box(
                Modifier.height(u.dp(44f)).tap { s.closeShade() }.padding(horizontal = u.dp(10f)),
                contentAlignment = Alignment.Center,
            ) { Txt("close", 13f, pal.muted2) }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = u.dp(28f), top = u.dp(16f), end = u.dp(28f), bottom = u.dp(8f)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Txt(shadeCount(notes.size), 11f, pal.muted, Modifier.weight(1f), spacing = .06f, maxLines = 1)
        }
        if (notes.isEmpty()) {
            Txt("nothing new", 13f, pal.muted2, Modifier.padding(start = u.dp(28f)), spacing = .04f)
            return@Column
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = u.dp(28f), end = u.dp(28f), bottom = u.dp(48f)),
        ) {
            items(notes, key = { it.key }) { n -> NoteRow(n, pal) }
        }
    }
}

private fun shadeCount(n: Int) = if (n == 1) "1 new" else "$n new"

/** One notification: the app, the title, and the words, over a hairline. */
@Composable
private fun NoteRow(n: Note, pal: Palette) {
    val u = LocalUnits.current
    Column(
        Modifier.fillMaxWidth()
            .padding(top = u.dp(16f), bottom = u.dp(14f))
            .drawBehind {
                val y = size.height - u.k / 2
                drawLine(pal.rim2, Offset(0f, y), Offset(size.width, y), strokeWidth = u.k)
            },
        verticalArrangement = Arrangement.spacedBy(u.dp(6f)),
    ) {
        Txt(n.app.lowercase(), 11f, pal.muted, spacing = .06f, maxLines = 1)
        if (n.title.isNotEmpty()) Txt(n.title, 17f, pal.fg, spacing = -.02f, maxLines = 1)
        if (n.text.isNotEmpty()) Txt(n.text, 13f, pal.muted2, line = 1.5f, maxLines = 3)
    }
}
