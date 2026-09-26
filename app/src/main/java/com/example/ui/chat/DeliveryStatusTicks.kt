package com.example.ui.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WhatsAppBlueTick
import com.example.ui.theme.WhatsAppGreyTick

/**
 * WhatsApp-style Delivery Status Ticks:
 * - "sent": 1 tick (Grey)
 * - "delivered": 2 ticks (Grey)
 * - "read": 2 ticks (Blue #53BDEB)
 */
@Composable
fun DeliveryStatusTicks(
    status: String,
    modifier: Modifier = Modifier
) {
    val tickColor = when (status) {
        "read" -> WhatsAppBlueTick
        else -> WhatsAppGreyTick
    }

    val isDoubleTick = status == "delivered" || status == "read"

    Box(
        modifier = modifier.size(width = if (isDoubleTick) 16.dp else 12.dp, height = 12.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 1.8.dp.toPx()

            // First tick (left tick in double tick, or single tick)
            val leftOffset = if (isDoubleTick) 0f else 2.dp.toPx()
            
            // Draw checkmark 1
            // Start: (leftOffset + 1dp, 6dp), Mid: (leftOffset + 4dp, 10dp), End: (leftOffset + 10dp, 2dp)
            val p1 = Offset(leftOffset + 1.dp.toPx(), 6.5.dp.toPx())
            val p2 = Offset(leftOffset + 4.dp.toPx(), 9.5.dp.toPx())
            val p3 = Offset(leftOffset + 9.dp.toPx(), 2.5.dp.toPx())

            drawLine(
                color = tickColor,
                start = p1,
                end = p2,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = tickColor,
                start = p2,
                end = p3,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Second tick (if delivered or read)
            if (isDoubleTick) {
                val shift = 4.5.dp.toPx()
                val q1 = Offset(shift + 1.dp.toPx(), 6.5.dp.toPx())
                val q2 = Offset(shift + 4.dp.toPx(), 9.5.dp.toPx())
                val q3 = Offset(shift + 9.dp.toPx(), 2.5.dp.toPx())

                drawLine(
                    color = tickColor,
                    start = q1,
                    end = q2,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tickColor,
                    start = q2,
                    end = q3,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
