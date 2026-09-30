package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.data.model.Professional
import com.example.data.model.SalonService
import com.example.data.model.getAssignedProfessionalIds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewServiceDialog(
    serviceToEdit: SalonService? = null,
    allProfessionals: List<Professional> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        price: Double,
        durationMinutes: Int,
        description: String,
        assignedProfessionalIds: List<Long>
    ) -> Unit
) {
    var name by remember { mutableStateOf(serviceToEdit?.name ?: "") }
    var category by remember { mutableStateOf(serviceToEdit?.category ?: "Cabelo") }
    var priceStr by remember { mutableStateOf(serviceToEdit?.price?.let { "%.2f".format(it) } ?: "") }
    var durationStr by remember { mutableStateOf(serviceToEdit?.durationMinutes?.toString() ?: "45") }
    var description by remember { mutableStateOf(serviceToEdit?.description ?: "") }

    val initialProfIds = remember(serviceToEdit, allProfessionals) {
        if (serviceToEdit == null) {
            allProfessionals.map { it.id }.toSet()
        } else if (serviceToEdit.professionalIdsCsv == "all" || serviceToEdit.professionalIdsCsv.isBlank()) {
            allProfessionals.map { it.id }.toSet()
        } else {
            serviceToEdit.getAssignedProfessionalIds().toSet()
        }
    }
    var selectedProfIds by remember { mutableStateOf(initialProfIds) }

    val categories = listOf("Cabelo", "Química & Cor", "Unhas", "Tratamentos", "Penteados", "Outros")
    var categoryExpanded by remember { mutableStateOf(false) }

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
                            text = if (serviceToEdit == null) "Cadastrar Serviço" else "Editar Serviço",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = if (serviceToEdit == null) "Adicione nome, preço e detalhes" else "Atualize os dados do serviço",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nome do Serviço
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do serviço *") },
                    placeholder = { Text("Ex: Escova Orgânica, Mechas, etc.") },
                    leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Categoria dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preço e Duração
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Preço (R$) *") },
                        placeholder = { Text("85.00") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("service_price_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it },
                        label = { Text("Duração (min) *") },
                        placeholder = { Text("45") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("service_duration_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detalhes / Descrição do Serviço
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detalhes do serviço") },
                    placeholder = { Text("Ex: Lavagem com produtos premium, técnica visagista, indicação para cabelos secos...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_details_input"),
                    minLines = 3
                )

                if (allProfessionals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Profissionais que executam este serviço:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isAllSelected = selectedProfIds.size == allProfessionals.size
                        FilterChip(
                            selected = isAllSelected,
                            onClick = {
                                selectedProfIds = if (isAllSelected) emptySet() else allProfessionals.map { it.id }.toSet()
                            },
                            label = { Text("Todas") }
                        )

                        allProfessionals.forEach { prof ->
                            val isSelected = selectedProfIds.contains(prof.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedProfIds = if (isSelected) {
                                        selectedProfIds - prof.id
                                    } else {
                                        selectedProfIds + prof.id
                                    }
                                },
                                leadingIcon = {
                                    Text(text = prof.avatarEmoji, fontSize = 12.sp)
                                },
                                label = { Text(prof.name) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                Button(
                    onClick = {
                        val parsedPrice = priceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val parsedDuration = durationStr.toIntOrNull() ?: 30
                        if (name.isNotBlank()) {
                            onSave(
                                name.trim(),
                                category,
                                parsedPrice,
                                parsedDuration,
                                description.trim(),
                                selectedProfIds.toList()
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_service_button"),
                    enabled = name.isNotBlank() && (priceStr.replace(",", ".").toDoubleOrNull() != null),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (serviceToEdit == null) "Cadastrar Serviço" else "Salvar Alterações",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
