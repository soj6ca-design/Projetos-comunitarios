package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Professional
import com.example.data.model.SalonService
import com.example.data.model.executesService
import com.example.data.model.getExecutedServiceIds
import com.example.ui.theme.BellaGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfessionalsScreen(
    professionals: List<Professional>,
    services: List<SalonService>,
    onAddProfessional: (name: String, role: String, phone: String, emoji: String, serviceIds: List<Long>) -> Unit,
    onEditProfessional: (professional: Professional, serviceIds: List<Long>) -> Unit,
    onDeleteProfessional: (professional: Professional) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var professionalToEdit by remember { mutableStateOf<Professional?>(null) }
    var professionalToDelete by remember { mutableStateOf<Professional?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Profissionais",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "${professionals.size} profissionais e especialistas cadastrados",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("add_prof_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Cadastrar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Empty state
        if (professionals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👩‍💼", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhuma profissional cadastrada ainda.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        } else {
            // Professional list cards
            items(professionals, key = { it.id }) { prof ->
                // Services executed by this professional
                val executedServices = remember(prof, services) {
                    if (prof.serviceIdsCsv == "all" || prof.serviceIdsCsv.isBlank()) {
                        services
                    } else {
                        val ids = prof.getExecutedServiceIds()
                        services.filter { ids.contains(it.id) }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prof_card_${prof.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Top row: Emoji avatar, Name, Role, Rating
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = prof.avatarEmoji, fontSize = 26.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prof.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = prof.role,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                if (prof.phone.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = prof.phone,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = BellaGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = prof.rating.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                )
                            }
                        }

                        // Middle row: Serviços que executa
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Serviços que executa (${executedServices.size}):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (executedServices.isEmpty()) {
                            Text(
                                text = "Nenhum serviço vinculado no momento.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.outline,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val isAll = prof.serviceIdsCsv == "all" || prof.serviceIdsCsv.isBlank()
                                if (isAll) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "✨ Executa todos os serviços do salão",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        )
                                    }
                                } else {
                                    executedServices.forEach { service ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = service.name,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom row: Action Buttons (Editar & Remover)
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { professionalToEdit = prof },
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("edit_prof_${prof.id}"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Editar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            FilledTonalButton(
                                onClick = { professionalToDelete = prof },
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("delete_prof_${prof.id}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Remover", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Dialog: Cadastrar Nova Profissional
    if (showAddDialog) {
        ProfessionalFormDialog(
            professionalToEdit = null,
            availableServices = services,
            onDismiss = { showAddDialog = false },
            onSave = { name, role, phone, emoji, serviceIds ->
                onAddProfessional(name, role, phone, emoji, serviceIds)
                showAddDialog = false
            }
        )
    }

    // Modal Dialog: Editar Profissional
    professionalToEdit?.let { prof ->
        ProfessionalFormDialog(
            professionalToEdit = prof,
            availableServices = services,
            onDismiss = { professionalToEdit = null },
            onSave = { name, role, phone, emoji, serviceIds ->
                onEditProfessional(
                    prof.copy(name = name, role = role, phone = phone, avatarEmoji = emoji),
                    serviceIds
                )
                professionalToEdit = null
            }
        )
    }

    // Confirmation Dialog: Apagar Profissional
    professionalToDelete?.let { prof ->
        AlertDialog(
            onDismissRequest = { professionalToDelete = null },
            title = {
                Text(
                    text = "Remover Profissional?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Tem certeza que deseja remover \"${prof.name}\"?\n\nEssa profissional deixará de aparecer na equipe e para agendamentos de serviços.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProfessional(prof)
                        professionalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_prof_btn")
                ) {
                    Text(text = "Remover", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { professionalToDelete = null }) {
                    Text(text = "Cancelar")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfessionalFormDialog(
    professionalToEdit: Professional?,
    availableServices: List<SalonService>,
    onDismiss: () -> Unit,
    onSave: (name: String, role: String, phone: String, emoji: String, serviceIds: List<Long>) -> Unit
) {
    var name by remember { mutableStateOf(professionalToEdit?.name ?: "") }
    var role by remember { mutableStateOf(professionalToEdit?.role ?: "") }
    var phone by remember { mutableStateOf(professionalToEdit?.phone ?: "") }
    var emoji by remember { mutableStateOf(professionalToEdit?.avatarEmoji ?: "💇‍♀️") }

    val emojis = listOf("💇‍♀️", "💅", "💆‍♀️", "✂️", "💄", "🌸", "🪮", "✨", "👩‍🦰", "👩‍🦱")

    val initialSelectedServiceIds = remember(professionalToEdit, availableServices) {
        if (professionalToEdit == null) {
            availableServices.map { it.id }.toSet()
        } else if (professionalToEdit.serviceIdsCsv == "all" || professionalToEdit.serviceIdsCsv.isBlank()) {
            availableServices.map { it.id }.toSet()
        } else {
            professionalToEdit.getExecutedServiceIds().toSet()
        }
    }
    var selectedServiceIds by remember { mutableStateOf(initialSelectedServiceIds) }

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
                    Column {
                        Text(
                            text = if (professionalToEdit == null) "Nova Profissional" else "Editar Profissional",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = if (professionalToEdit == null) "Cadastre a especialista e seus serviços" else "Atualize dados e serviços que executa",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Emoji Selector
                Text(
                    text = "Escolha o Avatar / Emoji:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.forEach { e ->
                        val isSelected = e == emoji
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = e, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nome
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Profissional *") },
                    placeholder = { Text("Ex: Carla Mendes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prof_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Especialidade / Cargo
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Especialidade / Cargo *") },
                    placeholder = { Text("Ex: Cabeleireira & Colorista Master") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prof_role_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Telefone / WhatsApp
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone / WhatsApp") },
                    placeholder = { Text("(11) 98765-4321") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prof_phone_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Serviços que irá executar
                Text(
                    text = "Serviços que irá executar:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (availableServices.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isAllSelected = selectedServiceIds.size == availableServices.size
                        FilterChip(
                            selected = isAllSelected,
                            onClick = {
                                selectedServiceIds = if (isAllSelected) emptySet() else availableServices.map { it.id }.toSet()
                            },
                            label = { Text("Todos os Serviços") }
                        )

                        FilterChip(
                            selected = selectedServiceIds.isEmpty(),
                            onClick = { selectedServiceIds = emptySet() },
                            label = { Text("Limpar") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableServices.forEach { service ->
                            val isSelected = selectedServiceIds.contains(service.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedServiceIds = if (isSelected) {
                                        selectedServiceIds - service.id
                                    } else {
                                        selectedServiceIds + service.id
                                    }
                                },
                                leadingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = service.name,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Cadastre serviços primeiro na aba Serviços.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botão Salvar
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                name.trim(),
                                role.trim().ifBlank { "Especialista" },
                                phone.trim(),
                                emoji,
                                selectedServiceIds.toList()
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_prof_button"),
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (professionalToEdit == null) "Cadastrar Profissional" else "Salvar Alterações",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
