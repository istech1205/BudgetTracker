package com.istech.expensestracker.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.istech.expensestracker.ui.theme.ChartColors

@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    maxBarHeight: Float = 200f,
    barWidth: Float = 48f
) {
    if (entries.isEmpty()) {
        EmptyChartState(modifier = modifier)
        return
    }

    val colors = ChartColors
    val maxValue = entries.maxOf { it.value }.coerceAtLeast(1f)
    val total = entries.sumOf { it.value.toDouble() }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Expenses by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Chart Area with horizontal scroll for many items
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxBarHeight.dp + 80.dp)
                    .horizontalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    entries.forEachIndexed { index, entry ->
                        val isSelected = selectedIndex == index
                        BarItem(
                            entry = entry,
                            maxValue = maxValue,
                            total = total,
                            maxBarHeight = maxBarHeight,
                            barWidth = barWidth,
                            color = colors[index % colors.size],
                            isSelected = isSelected,
                            onClick = {
                                selectedIndex = if (selectedIndex == index) -1 else index
                            }
                        )
                    }
                }
            }

            // Summary Legend
            if (entries.size > 1) {
                Spacer(modifier = Modifier.height(16.dp))
                CompactLegend(entries = entries, colors = colors)
            }
        }
    }
}

@Composable
private fun BarItem(
    entry: BarChartEntry,
    maxValue: Float,
    total: Double,
    maxBarHeight: Float,
    barWidth: Float,
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedHeight = remember { Animatable(0f) }
    val targetHeightFraction = entry.value / maxValue
    val targetHeight = maxBarHeight * targetHeightFraction

    LaunchedEffect(targetHeightFraction) {
        animatedHeight.animateTo(
            targetValue = targetHeight,
            animationSpec = tween(durationMillis = 800, delayMillis = 100)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(barWidth.dp + 8.dp)
    ) {
        // Value label with background when selected
        if (isSelected) {
            Box(
                modifier = Modifier
                    .background(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "₹${entry.value.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1
                )
            }
        } else {
            // Show small value hint for larger bars
            if (entry.value > maxValue * 0.25f) {
                Text(
                    text = formatCompactAmount(entry.value),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = color.copy(alpha = 0.8f),
                    maxLines = 1
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Animated Bar with gradient and shadow
        Box(
            modifier = Modifier
                .width(barWidth.dp)
                .height(animatedHeight.value.dp)
                .graphicsLayer {
                    shadowElevation = if (isSelected) 8f else 2f
                }
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .clickable { onClick() }
                .animateContentSize()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category label with selection highlight
        Text(
            text = entry.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(barWidth.dp + 4.dp)
        )

        // Percentage indicator (relative to total, same as legend)
        val percentage = if (total > 0) (entry.value / total * 100).toInt() else 0
        if (isSelected) {
            Text(
                text = "$percentage%",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactLegend(
    entries: List<BarChartEntry>,
    colors: List<androidx.compose.ui.graphics.Color>
) {
    val total = entries.sumOf { it.value.toDouble() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        entries.chunked(3).forEach { rowEntries ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowEntries.forEach { entry ->
                    val colorIndex = entries.indexOf(entry)
                    val percentage = if (total > 0) (entry.value / total * 100).toInt() else 0

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(10.dp)
                                .background(
                                    color = colors[colorIndex % colors.size],
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = entry.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${percentage}% • ₹${entry.value.toInt()}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
                // Fill empty slots in row
                repeat(3 - rowEntries.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EmptyChartState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(250.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No expenses this month",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Add expenses to see your spending breakdown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun formatCompactAmount(value: Float): String {
    return when {
        value >= 100000 -> "₹${(value / 100000).toInt()}L"
        value >= 1000 -> "₹${(value / 1000).toInt()}K"
        else -> "₹${value.toInt()}"
    }
}

data class BarChartEntry(
    val label: String,
    val value: Float
)
