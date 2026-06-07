package com.example.accountingapp.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * iOS-style smooth continuous corner shape (squircle).
 * Uses cubic bezier curves with a lower control-point factor than
 * standard circular corners, producing a gradual, elegant curve.
 */
class SquircleShape(private val radius: Dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { radius.toPx() }
        val w = size.width
        val h = size.height
        // k ≈ 0.552 = perfect circle; k ≈ 0.45 = subtle squircle (iOS feel)
        val k = 0.45f

        val path = Path().apply {
            moveTo(r, 0f)

            // Top edge → top-right corner
            lineTo(w - r, 0f)
            cubicTo(w - r * k, 0f, w, r * k, w, r)

            // Right edge → bottom-right corner
            lineTo(w, h - r)
            cubicTo(w, h - r * k, w - r * k, h, w - r, h)

            // Bottom edge → bottom-left corner
            lineTo(r, h)
            cubicTo(r * k, h, 0f, h - r * k, 0f, h - r)

            // Left edge → top-left corner
            lineTo(0f, r)
            cubicTo(0f, r * k, r * k, 0f, r, 0f)

            close()
        }
        return Outline.Generic(path)
    }
}
