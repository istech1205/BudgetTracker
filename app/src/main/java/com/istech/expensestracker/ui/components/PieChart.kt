package com.istech.expensestracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.istech.expensestracker.ui.theme.ChartColors
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

data class PieChartEntry(
    val label: String,
    val value: Float
)

@Composable
fun PieChart(
    entries: List<PieChartEntry>,
    modifier: Modifier = Modifier,
    centerText: String = ""
) {
    if (entries.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data to display",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }
    val total = entries.sumOf { it.value.toDouble() }.toFloat()
    val colors = ChartColors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(entries) {
                        detectTapGestures { offset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val dx = offset.x - centerX
                            val dy = offset.y - centerY
                            val distance = hypot(dx, dy)
                            val radius = min(centerX, centerY)

                            if (distance > radius * 0.4f && distance <= radius) {
                                var angle = atan2(dy, dx) * 180f / PI.toFloat()
                                if (angle < 0) angle += 360f
                                angle = (angle + 90f) % 360f

                                var currentAngle = 0f
                                for (i in entries.indices) {
                                    val sweep = (entries[i].value / total) * 360f
                                    if (angle >= currentAngle && angle < currentAngle + sweep) {
                                        selectedIndex = if (selectedIndex == i) -1 else i
                                        break
                                    }
                                    currentAngle += sweep
                                }
                            }
                        }
                    }
            ) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val radius = min(centerX, centerY) * 0.9f
                val strokeWidth = radius * 0.4f

                var startAngle = -90f

                entries.forEachIndexed { index, entry ->
                    val sweepAngle = (entry.value / total) * 360f
                    val color = colors[index % colors.size]

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(centerX - radius, centerY - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth)
                    )

                    startAngle += sweepAngle
                }
            }

            if (centerText.isNotEmpty() && selectedIndex == -1) {
                Text(
                    text = centerText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else if (selectedIndex != -1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = entries[selectedIndex].label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "₹${entries[selectedIndex].value.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors[selectedIndex % colors.size]
                    )
                }
            }
        }

        // Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            entries.chunked(2).forEach { rowEntries ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowEntries.forEachIndexed { index, entry ->
                        val colorIndex = entries.indexOf(entry)
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = colors[colorIndex % colors.size],
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${entry.label}: ₹${entry.value.toInt()}",
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                    if (rowEntries.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
