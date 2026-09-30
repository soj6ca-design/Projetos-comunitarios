package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Client
import com.example.data.model.Professional
import com.example.data.model.SalonService
import com.example.data.model.executesService
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentDialog(
    clients: List<Client>,
    services: List<SalonService>,
    professionals: List<Professional>,
    defaultDate: String,
    onDismiss: () -> Unit,
    onConfirm: (
        clientName: String,
        clientPhone: String,
        clientId: Long?,
        service: SalonService,
        professional: Professional,
        dateStr: String,
        timeStr: String,
        notes: String
    ) -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var selectedClientId by remember { mutableStateOf<Long?>(null) }

    var selectedService by remember { mutableStateOf(services.firstOrNull()) }
    var serviceExpanded by remember { mutableStateOf(false) }

    var selectedProfessional by remember { mutableStateOf(professionals.firstOrNull()) }
    var profExpanded by remember { mutableStateOf(false) }

    var dateStr by remember { mutableStateOf(defaultDate) }
    var timeStr by remember { mutableStateOf("09:00") }
    var notes by remember { mutableStateOf("") }

    val availableTimes = listOf(
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
        "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
        "17:00", "17:30", "18:00", "18:30", "19:00"
    )
    var timeExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Novo Agendamento",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cliente
                Text(
                    text = "Cliente",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nome da cliente") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_client_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("WhatsApp (com DDD)") },
                    placeholder = { Text("11987654321") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_client_phone"),
                    singleLine = true
                )

                // Quick client suggestions if available
                if (clients.isNotEmpty() && clientName.isBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Clientes frequentes:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        clients.take(3).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        clientName = c.name
                                        clientPhone = c.phone
                                        selectedClientId = c.id
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = c.name.split(" ").first(), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Serviço Dropdown
                Text(
                    text = "Serviço Desejado",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = serviceExpanded,
                    onExpandedChange = { serviceExpanded = !serviceExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedService?.let { "${it.name} - ${currencyFormat.format(it.price)}" } ?: "Selecione o serviço",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = serviceExpanded,
                        onDismissRequest = { serviceExpanded = false }
                    ) {
                        services.forEach { service ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = service.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${service.durationMinutes} min • ${currencyFormat.format(service.price)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                },
                                onClick = {
                                    selectedService = service
                                    serviceExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profissional Dropdown
                Text(
                    text = "Profissional",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = profExpanded,
                    onExpandedChange = { profExpanded = !profExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProfessional?.let { "${it.avatarEmoji} ${it.name} (${it.role})" } ?: "Selecione o profissional",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = profExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = profExpanded,
                        onDismissRequest = { profExpanded = false }
                    ) {
                        professionals.forEach { prof ->
                            val canExecute = selectedService?.let { prof.executesService(it.id) } ?: true
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${prof.avatarEmoji} ${prof.name} - ${prof.role}")
                                        if (canExecute) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "✓ Habilitada",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedProfessional = prof
                                    profExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Data e Horário
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Data (AAAA-MM-DD)") },
                        modifier = Modifier.weight(1.2f),
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                    )

                    ExposedDropdownMenuBox(
                        expanded = timeExpanded,
                        onExpandedChange = { timeExpanded = !timeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = timeStr,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Horário") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = timeExpanded,
                            onDismissRequest = { timeExpanded = false }
                        ) {
                            availableTimes.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = {
                                        timeStr = t
                                        timeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Observações
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (opcional)") },
                    placeholder = { Text("Ex: Primeira vez no salão, prefere café...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Confirmation Button
                Button(
                    onClick = {
                        val serv = selectedService ?: return@Button
                        val prof = selectedProfessional ?: return@Button
                        val finalName = if (clientName.isBlank()) "Cliente Salão" else clientName
                        val finalPhone = if (clientPhone.isBlank()) "11999999999" else clientPhone
                        onConfirm(
                            finalName,
                            finalPhone,
                            selectedClientId,
                            serv,
                            prof,
                            dateStr,
                            timeStr,
                            notes
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("confirm_new_appointment"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Salvar Agendamento", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
