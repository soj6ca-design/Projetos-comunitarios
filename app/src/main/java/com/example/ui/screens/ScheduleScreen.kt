package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.Professional
import com.example.data.model.ScheduleBlock
import com.example.ui.components.AppointmentCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ScheduleScreen(
    appointments: List<Appointment>,
    professionals: List<Professional>,
    selectedDate: String,
    todayDate: String,
    selectedProfessionalId: Long?,
    onSelectDate: (String) -> Unit,
    onFilterProfessional: (Long?) -> Unit,
    onNewAppointmentClick: () -> Unit,
    onStatusChange: (Long, String) -> Unit,
    onWhatsAppClick: (String, String) -> Unit,
    onAddToCalendar: (Appointment) -> Unit,
    salonName: String = "Vanira e Vanessa Salão Especializado",
    scheduleBlocks: List<ScheduleBlock> = emptyList(),
    onAddBlockClick: (() -> Unit)? = null,
    onDeleteBlock: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Dynamic next 7 days starting from todayDate
    val dynamicDates = remember(todayDate) {
        val list = mutableListOf<Pair<String, String>>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        try {
            val parsed = sdf.parse(todayDate)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}

        val dayNames = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
        for (i in 0..6) {
            val dateStr = sdf.format(cal.time)
            val label = when (i) {
                0 -> "Hoje"
                1 -> "Amanhã"
                else -> {
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                    dayNames[dayOfWeek.coerceIn(0, 6)]
                }
            }
            list.add(label to dateStr)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    // Filtered blocks for this date and professional
    val dayBlocks = remember(scheduleBlocks, selectedDate, selectedProfessionalId) {
        scheduleBlocks.filter { block ->
            block.dateStr == selectedDate &&
            (selectedProfessionalId == null || block.professionalId == selectedProfessionalId)
        }.sortedBy { it.startTime }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Date Selector Bar
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agenda",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onAddBlockClick != null) {
                            OutlinedButton(
                                onClick = onAddBlockClick,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("agenda_block_time_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Bloquear", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = onNewAppointmentClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("agenda_new_appointment_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Novo Horário", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dynamicDates.forEach { (label, date) ->
                        DateChip(
                            title = label,
                            date = date,
                            selected = selectedDate == date,
                            onClick = { onSelectDate(date) }
                        )
                    }
                }
            }
        }

        // Filter by Professional
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Filtrar por profissional:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedProfessionalId == null,
                        onClick = { onFilterProfessional(null) },
                        label = { Text("Todas as profissionais") }
                    )
                    professionals.forEach { prof ->
                        FilterChip(
                            selected = selectedProfessionalId == prof.id,
                            onClick = { onFilterProfessional(prof.id) },
                            label = { Text("${prof.avatarEmoji} ${prof.name.split(" ").first()}") }
                        )
                    }
                }
            }
        }

        // Schedule summary for selected date
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Data: $selectedDate",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "${appointments.size} atendimento(s)${if (dayBlocks.isNotEmpty()) " • ${dayBlocks.size} bloqueio(s)" else ""}",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // Schedule Blocks (if any)
        if (dayBlocks.isNotEmpty()) {
            items(dayBlocks, key = { "block_${it.id}" }) { block ->
                val prof = professionals.find { it.id == block.professionalId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "⛔ ${block.startTime} - ${block.endTime} • ${block.reason}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB71C1C)
                                    )
                                )
                                Text(
                                    text = "Profissional: ${prof?.name ?: "Geral"}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFC62828))
                                )
                            }
                        }

                        if (onDeleteBlock != null) {
                            IconButton(onClick = { onDeleteBlock(block.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remover bloqueio",
                                    tint = Color(0xFFB71C1C)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Appointments List
        if (appointments.isEmpty() && dayBlocks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📅", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nenhum agendamento para esta data",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Aproveite os horários livres para fazer encaixes ou novos agendamentos!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(appointments, key = { "app_${it.id}" }) { app ->
                AppointmentCard(
                    appointment = app,
                    onStatusChange = onStatusChange,
                    onWhatsAppClick = onWhatsAppClick,
                    onAddToCalendar = onAddToCalendar,
                    salonName = salonName
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DateChip(
    title: String,
    date: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = date.substringAfter("-"),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}
