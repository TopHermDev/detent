package app.detent.launcher.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the quick icon [name] in a square of [box] px with its top left corner at [at]. The icons use a
 * 24-unit grid and hairlines like the other Detent icons. [w] is the line width in px.
 */
fun DrawScope.drawQuickIcon(name: String, color: Color, at: Offset, box: Float, w: Float) {
    val f = box / 24f
    val line = Stroke(w / f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    withTransform({
        translate(at.x, at.y)
        scale(f, f, Offset.Zero)
    }) {
        fun ln(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(color, Offset(x1, y1), Offset(x2, y2), line.width, StrokeCap.Round)
        fun ring(x: Float, y: Float, r: Float) = drawCircle(color, r, Offset(x, y), style = line)
        fun dot(x: Float, y: Float, r: Float) = drawCircle(color, r, Offset(x, y))
        fun box(l: Float, t: Float, r: Float, b: Float, c: Float) =
            drawRoundRect(color, Offset(l, t), Size(r - l, b - t), CornerRadius(c), style = line)
        fun poly(vararg p: Float, close: Boolean = false) {
            val path = Path()
            path.moveTo(p[0], p[1])
            var i = 2
            while (i < p.size) {
                path.lineTo(p[i], p[i + 1])
                i += 2
            }
            if (close) path.close()
            drawPath(path, color, style = line)
        }
        fun stroke(path: Path) = drawPath(path, color, style = line)
        fun circle(x: Float, y: Float, r: Float) = Path().apply { addOval(Rect(Offset(x, y), r)) }
        fun union(vararg p: Path) = p.reduce { a, b -> Path.combine(PathOperation.Union, a, b) }

        when (name) {
            "camera" -> {
                box(2.5f, 7f, 21.5f, 19.5f, 3f)
                poly(8f, 7f, 9.6f, 4.5f, 14.4f, 4.5f, 16f, 7f)
                ring(12f, 13.2f, 3.6f)
            }
            "phone" -> {
                // A handset: a thick quarter ring. Both ends turn in toward the face.
                val c = Offset(19f, 5f)
                val band = Path.combine(
                    PathOperation.Intersect,
                    Path.combine(PathOperation.Difference, circle(c.x, c.y, 15f), circle(c.x, c.y, 10f)),
                    Path().apply { addRect(Rect(0f, c.y, c.x, 24f)) },
                )
                val ear = Path().apply { addRoundRect(RoundRect(Rect(4f, 3.2f, 12.4f, 9.6f), CornerRadius(2.6f))) }
                val mouth = Path().apply { addRoundRect(RoundRect(Rect(13.4f, 11.6f, 19.8f, 20f), CornerRadius(2.6f))) }
                stroke(union(band, ear, mouth))
            }
            "chat" -> stroke(
                Path().apply {
                    moveTo(7f, 4f)
                    lineTo(17f, 4f)
                    quadraticTo(21f, 4f, 21f, 8f)
                    lineTo(21f, 12.5f)
                    quadraticTo(21f, 16.5f, 17f, 16.5f)
                    lineTo(12f, 16.5f)
                    lineTo(7.5f, 20.2f)
                    lineTo(7.5f, 16.5f)
                    lineTo(7f, 16.5f)
                    quadraticTo(3f, 16.5f, 3f, 12.5f)
                    lineTo(3f, 8f)
                    quadraticTo(3f, 4f, 7f, 4f)
                    close()
                },
            )
            "mail" -> {
                box(3f, 5.5f, 21f, 18.5f, 2.5f)
                poly(3.8f, 7.2f, 12f, 13f, 20.2f, 7.2f)
            }
            "globe" -> {
                ring(12f, 12f, 9f)
                drawOval(color, Offset(8f, 3f), Size(8f, 18f), style = line)
                ln(3f, 12f, 21f, 12f)
            }
            "pin" -> {
                stroke(
                    Path().apply {
                        moveTo(12f, 21.5f)
                        cubicTo(9f, 18.5f, 5f, 14f, 5f, 9.5f)
                        arcTo(Rect(5f, 2.5f, 19f, 16.5f), 180f, 180f, false)
                        cubicTo(19f, 14f, 15f, 18.5f, 12f, 21.5f)
                        close()
                    },
                )
                ring(12f, 9.5f, 2.6f)
            }
            "music" -> {
                poly(9f, 17.5f, 9f, 5.8f, 20f, 3.6f, 20f, 15f)
                ring(6.5f, 17.5f, 2.5f)
                ring(17.5f, 15f, 2.5f)
            }
            "headphones" -> {
                stroke(
                    Path().apply {
                        moveTo(3.5f, 16f)
                        lineTo(3.5f, 13f)
                        arcTo(Rect(3.5f, 4.5f, 20.5f, 21.5f), 180f, 180f, false)
                        lineTo(20.5f, 16f)
                    },
                )
                box(3.5f, 13f, 8f, 20.5f, 2.2f)
                box(16f, 13f, 20.5f, 20.5f, 2.2f)
            }
            "play" -> {
                box(2.5f, 5f, 21.5f, 19f, 4.5f)
                poly(10f, 9f, 15.5f, 12f, 10f, 15f, close = true)
            }
            "photo" -> {
                box(3f, 4f, 21f, 20f, 2.5f)
                ring(8.5f, 9f, 1.8f)
                poly(3.5f, 17.5f, 9f, 12f, 13f, 16f, 16f, 13f, 20.5f, 17.5f)
            }
            "calendar" -> {
                box(3f, 5f, 21f, 21f, 2.5f)
                ln(3f, 10f, 21f, 10f)
                ln(8f, 3f, 8f, 7f)
                ln(16f, 3f, 16f, 7f)
                dot(8.5f, 15f, 1.4f)
            }
            "clock" -> {
                ring(12f, 12f, 9f)
                poly(12f, 7f, 12f, 12f, 15.5f, 14f)
            }
            "note" -> {
                poly(15.5f, 4.5f, 19.5f, 8.5f, 8.5f, 19.5f, 4f, 20f, 4.5f, 15.5f, close = true)
                ln(13.5f, 6.5f, 17.5f, 10.5f)
            }
            "book" -> {
                stroke(
                    Path().apply {
                        moveTo(12f, 6.5f)
                        cubicTo(9.5f, 4.8f, 6f, 4.5f, 3f, 5f)
                        lineTo(3f, 19f)
                        cubicTo(6f, 18.5f, 9.5f, 18.8f, 12f, 20.5f)
                        cubicTo(14.5f, 18.8f, 18f, 18.5f, 21f, 19f)
                        lineTo(21f, 5f)
                        cubicTo(18f, 4.5f, 14.5f, 4.8f, 12f, 6.5f)
                        close()
                    },
                )
                ln(12f, 6.5f, 12f, 20.5f)
            }
            "terminal" -> {
                poly(5f, 7f, 10f, 12f, 5f, 17f)
                ln(12.5f, 17f, 19f, 17f)
            }
            "code" -> {
                poly(8f, 7f, 3f, 12f, 8f, 17f)
                poly(16f, 7f, 21f, 12f, 16f, 17f)
                ln(13.5f, 5f, 10.5f, 19f)
            }
            "card" -> {
                box(2.5f, 5.5f, 21.5f, 18.5f, 2.5f)
                ln(2.5f, 10f, 21.5f, 10f)
                ln(6f, 14.5f, 10f, 14.5f)
            }
            "bag" -> {
                poly(5f, 8f, 19f, 8f, 18f, 21f, 6f, 21f, close = true)
                stroke(Path().apply { arcTo(Rect(8.5f, 3f, 15.5f, 13f), 180f, 180f, true) })
            }
            "gear" -> {
                // Eight teeth around a ring.
                val path = Path()
                for (i in 0 until 8) {
                    val a = i * 45f
                    fun at(deg: Float, r: Float) = Offset(
                        12f + r * cos((deg - 90f) * PI.toFloat() / 180f),
                        12f + r * sin((deg - 90f) * PI.toFloat() / 180f),
                    )
                    val p = listOf(at(a - 9f, 9.5f), at(a + 9f, 9.5f), at(a + 15f, 7f), at(a + 30f, 7f))
                    p.forEachIndexed { j, o -> if (i == 0 && j == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y) }
                }
                path.close()
                stroke(path)
                ring(12f, 12f, 3f)
            }
            "sun" -> {
                ring(12f, 12f, 4f)
                for (i in 0 until 8) {
                    val a = i * PI.toFloat() / 4f
                    ln(12f + 6.8f * cos(a), 12f + 6.8f * sin(a), 12f + 9.3f * cos(a), 12f + 9.3f * sin(a))
                }
            }
            "heart" -> stroke(
                Path().apply {
                    moveTo(12f, 20f)
                    cubicTo(12f, 20f, 3f, 14.5f, 3f, 8.8f)
                    cubicTo(3f, 6f, 5.2f, 4f, 7.6f, 4f)
                    cubicTo(9.4f, 4f, 11f, 5f, 12f, 6.6f)
                    cubicTo(13f, 5f, 14.6f, 4f, 16.4f, 4f)
                    cubicTo(18.8f, 4f, 21f, 6f, 21f, 8.8f)
                    cubicTo(21f, 14.5f, 12f, 20f, 12f, 20f)
                    close()
                },
            )
            "star" -> {
                val pts = FloatArray(20)
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) 9.5f else 4.2f
                    val a = (-90f + i * 36f) * PI.toFloat() / 180f
                    pts[2 * i] = 12f + r * cos(a)
                    pts[2 * i + 1] = 12.8f + r * sin(a)
                }
                poly(*pts, close = true)
            }
            "bolt" -> poly(13f, 2.5f, 4.5f, 13.5f, 11.5f, 13.5f, 10.5f, 21.5f, 19.5f, 10.5f, 12.5f, 10.5f, close = true)
            "home" -> {
                poly(3.5f, 11f, 12f, 3.5f, 20.5f, 11f)
                poly(5.5f, 9.5f, 5.5f, 20.5f, 18.5f, 20.5f, 18.5f, 9.5f)
                poly(10f, 20.5f, 10f, 14.5f, 14f, 14.5f, 14f, 20.5f)
            }
            "bell" -> {
                stroke(
                    Path().apply {
                        moveTo(6f, 17f)
                        lineTo(6f, 11f)
                        cubicTo(6f, 7.4f, 8.7f, 4.8f, 12f, 4.8f)
                        cubicTo(15.3f, 4.8f, 18f, 7.4f, 18f, 11f)
                        lineTo(18f, 17f)
                        lineTo(19.5f, 18.5f)
                        lineTo(4.5f, 18.5f)
                        close()
                    },
                )
                ln(12f, 2.8f, 12f, 4.8f)
                stroke(Path().apply { moveTo(10f, 21f); quadraticTo(12f, 22.6f, 14f, 21f) })
            }
            "user" -> {
                ring(12f, 8f, 4f)
                stroke(
                    Path().apply {
                        moveTo(4.5f, 20.5f)
                        cubicTo(4.5f, 16.2f, 8f, 13.8f, 12f, 13.8f)
                        cubicTo(16f, 13.8f, 19.5f, 16.2f, 19.5f, 20.5f)
                    },
                )
            }
            "game" -> {
                box(2.5f, 7f, 21.5f, 17.5f, 5.2f)
                ln(6.5f, 12.2f, 10.5f, 12.2f)
                ln(8.5f, 10.2f, 8.5f, 14.2f)
                dot(15.5f, 11f, 1.1f)
                dot(17.8f, 13.4f, 1.1f)
            }
            "lock" -> {
                box(5f, 10.5f, 19f, 21f, 2.5f)
                stroke(
                    Path().apply {
                        moveTo(8f, 10.5f)
                        lineTo(8f, 7.5f)
                        cubicTo(8f, 5.3f, 9.8f, 3.5f, 12f, 3.5f)
                        cubicTo(14.2f, 3.5f, 16f, 5.3f, 16f, 7.5f)
                        lineTo(16f, 10.5f)
                    },
                )
                dot(12f, 15.7f, 1.4f)
            }
            "cloud" -> {
                val puff = union(
                    circle(8f, 14.5f, 4.5f),
                    circle(12.8f, 10.5f, 5.5f),
                    circle(17.3f, 14.8f, 3.7f),
                    Path().apply { addRect(Rect(8f, 14.5f, 17.3f, 19f)) },
                )
                stroke(Path.combine(PathOperation.Intersect, puff, Path().apply { addRect(Rect(0f, 0f, 24f, 19f)) }))
            }
            else -> dot(12f, 12f, 3.5f)
        }
    }
}
