package com.zyproo.devlink.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyproo.devlink.ui.theme.DevLinkBlue
import com.zyproo.devlink.ui.theme.DevLinkBorder
import com.zyproo.devlink.ui.theme.DevLinkTextPrimary
import com.zyproo.devlink.ui.theme.DevLinkTextSecondary
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MeetingSchedulePicker(
    initialIsoString: String = "",
    onIsoStringChange: (String) -> Unit
) {
    val now = remember { LocalDate.now() }
    var selectedYear by remember { mutableIntStateOf(now.year) }
    var selectedMonth by remember { mutableIntStateOf(now.monthValue) }
    var selectedDay by remember { mutableIntStateOf(now.dayOfMonth) }

    var enableTime by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(10) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    // Recompute ISO string whenever values change
    LaunchedEffect(selectedYear, selectedMonth, selectedDay, enableTime, selectedHour, selectedMinute) {
        val yStr = selectedYear.toString().padStart(4, '0')
        val mStr = selectedMonth.toString().padStart(2, '0')
        val dStr = selectedDay.toString().padStart(2, '0')
        val hStr = if (enableTime) selectedHour.toString().padStart(2, '0') else "10"
        val minStr = if (enableTime) selectedMinute.toString().padStart(2, '0') else "00"
        onIsoStringChange("${yStr}-${mStr}-${dStr}T${hStr}:${minStr}:00Z")
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = DevLinkBlue,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Meeting / Discussion Date",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DevLinkTextPrimary
            )
        }

        // Dropdowns for Day, Month, Year
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Day selector
            val daysInMonth = remember(selectedYear, selectedMonth) {
                try {
                    java.time.YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
                } catch (e: Exception) {
                    31
                }
            }
            LaunchedEffect(daysInMonth) {
                if (selectedDay > daysInMonth) selectedDay = daysInMonth
            }

            Box(modifier = Modifier.weight(1f)) {
                CustomPickerDropdown(
                    label = "Day",
                    valueText = selectedDay.toString().padStart(2, '0'),
                    options = (1..daysInMonth).toList(),
                    onSelect = { selectedDay = it },
                    itemLabel = { it.toString().padStart(2, '0') }
                )
            }

            // Month selector
            Box(modifier = Modifier.weight(1.3f)) {
                CustomPickerDropdown(
                    label = "Month",
                    valueText = java.time.Month.of(selectedMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    options = (1..12).toList(),
                    onSelect = { selectedMonth = it },
                    itemLabel = { m -> java.time.Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) }
                )
            }

            // Year selector
            val currentYear = now.year
            Box(modifier = Modifier.weight(1.1f)) {
                CustomPickerDropdown(
                    label = "Year",
                    valueText = selectedYear.toString(),
                    options = (currentYear..currentYear + 5).toList(),
                    onSelect = { selectedYear = it },
                    itemLabel = { it.toString() }
                )
            }
        }

        // Optional Time Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, DevLinkBorder)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (enableTime) DevLinkBlue else DevLinkTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Set Time (Optional)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DevLinkTextPrimary
                        )
                    }
                    Switch(
                        checked = enableTime,
                        onCheckedChange = { enableTime = it },
                        modifier = Modifier.height(24.dp)
                    )
                }

                if (enableTime) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CustomPickerDropdown(
                                label = "Hour",
                                valueText = selectedHour.toString().padStart(2, '0'),
                                options = (0..23).toList(),
                                onSelect = { selectedHour = it },
                                itemLabel = { it.toString().padStart(2, '0') + ":00" }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CustomPickerDropdown(
                                label = "Minute",
                                valueText = selectedMinute.toString().padStart(2, '0'),
                                options = listOf(0, 15, 30, 45),
                                onSelect = { selectedMinute = it },
                                itemLabel = { it.toString().padStart(2, '0') }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T> CustomPickerDropdown(
    label: String,
    valueText: String,
    options: List<T>,
    onSelect: (T) -> Unit,
    itemLabel: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, DevLinkBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = DevLinkTextSecondary
                )
                Text(
                    text = valueText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DevLinkTextPrimary,
                    maxLines = 1
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 240.dp)
                .background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemLabel(option),
                            fontSize = 13.sp,
                            color = DevLinkTextPrimary
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
