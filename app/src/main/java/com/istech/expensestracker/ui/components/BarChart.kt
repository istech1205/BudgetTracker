package com.istech.expensestracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.istech.expensestracker.ui.theme.ChartColors

@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    maxBarHeight: Float = 200f,
    barWidth: Float = 40f
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

    val colors = ChartColors
    val maxValue = entries.maxOf { it.value }.coerceAtLeast(1f)
    var selectedIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Chart Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxBarHeight.dp + 60.dp)
                .padding(16.dp)
        ) {
            val barSpacing = 16.dp
            
            entries.forEachIndexed { index, entry ->
                val barHeightFraction = entry.value / maxValue
                val barHeight = (maxBarHeight * barHeightFraction).dp
                val isSelected = selectedIndex == index

                Column(
                    modifier = Modifier
                        .padding(horizontal = barSpacing / 2)
                        .align(Alignment.BottomStart)
                        .then(
                            if (index == 0) Modifier else Modifier.padding(start = (index * (barWidth + 16)).dp)
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Value label above bar
                    if (isSelected || entry.value > maxValue * 0.3f) {
                        Text(
                            text = "₹${entry.value.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = colors[index % colors.size],
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bar
                    Box(
                        modifier = Modifier
                            .width(barWidth.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                color = if (isSelected) 
                                    colors[index % colors.size].copy(alpha = 0.8f) 
                                else 
                                    colors[index % colors.size],
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                            .pointerInput(entry) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val position = event.changes.first().position
                                        if (position.x in 0f..barWidth && 
                                            position.y >= 0f && position.y <= barHeight.toPx()) {
                                            selectedIndex = if (selectedIndex == index) -1 else index
                                        }
                                    }
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category label
                    Text(
                        text = entry.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(barWidth.dp)
                    )
                }
            }
        }

        // Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                        shape = CircleShape
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

data class BarChartEntry(
    val label: String,
    val value: Float
)
