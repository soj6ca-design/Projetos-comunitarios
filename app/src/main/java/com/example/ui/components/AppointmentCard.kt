package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.ui.theme.StatusCanceled
import com.example.ui.theme.StatusCanceledContainer
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusCompletedContainer
import com.example.ui.theme.StatusConfirmed
import com.example.ui.theme.StatusConfirmedContainer
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingContainer
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onStatusChange: (Long, String) -> Unit,
    onWhatsAppClick: (String, String) -> Unit,
    onAddToCalendar: (Appointment) -> Unit,
    salonName: String = "Vanira e Vanessa Salão Especializado",
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    var menuExpanded by remember { mutableStateOf(false) }

    val (statusColor, statusBg, statusLabel, statusEmoji) = when (appointment.status) {
        "CONFIRMADO" -> Quad(StatusConfirmed, StatusConfirmedContainer, "Confirmado", "🟢")
        "AGUARDANDO" -> Quad(StatusPending, StatusPendingContainer, "Aguardando confirmação", "🟠")
        "CONCLUIDO" -> Quad(StatusCompleted, StatusCompletedContainer, "Concluído", "🟣")
        "CANCELADO" -> Quad(StatusCanceled, StatusCanceledContainer, "Cancelado", "🔴")
        else -> Quad(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant, appointment.status, "⚪")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("appointment_card_${appointment.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Time badge, Status & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = appointment.timeStr,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${appointment.durationMinutes} min",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                // Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$statusEmoji $statusLabel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opções do agendamento"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🟢 Marcar como Confirmado") },
                                onClick = {
                                    onStatusChange(appointment.id, "CONFIRMADO")
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🟣 Marcar como Concluído") },
                                onClick = {
                                    onStatusChange(appointment.id, "CONCLUIDO")
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🟠 Aguardando confirmação") },
                                onClick = {
                                    onStatusChange(appointment.id, "AGUARDANDO")
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🔴 Cancelar agendamento") },
                                onClick = {
                                    onStatusChange(appointment.id, "CANCELADO")
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Client & Service details
            Text(
                text = appointment.clientName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.serviceName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = currencyFormat.format(appointment.price),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Professional assignment
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Profissional: ", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                Text(
                    text = appointment.professionalName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            if (appointment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Obs: ${appointment.notes}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: WhatsApp & Google Agenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WhatsApp Button
                FilledTonalButton(
                    onClick = {
                        val msg = "Olá, ${appointment.clientName}! 💇‍♀️ Passando para confirmar seu agendamento no $salonName para ${appointment.serviceName} com ${appointment.professionalName} no dia ${appointment.dateStr} às ${appointment.timeStr}. Te esperamos com carinho!"
                        onWhatsAppClick(appointment.clientPhone, msg)
                    },
                    modifier = Modifier.weight(1f).testTag("whatsapp_btn_${appointment.id}"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF1B5E20)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "💬 WhatsApp", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                // Google Agenda Button
                OutlinedButton(
                    onClick = { onAddToCalendar(appointment) },
                    modifier = Modifier.weight(1f).testTag("calendar_btn_${appointment.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Google Agenda", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
