package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ResetOption {
    CLEAR_ALL_BLANK,           // Banco 100% limpo (zero agendamentos, zero clientes, zero serviços)
    RESTORE_DEFAULT_TEMPLATE,  // Restaurar padrão limpo com Vanira & Vanessa e catálogo
    CLEAR_APPOINTMENTS_ONLY    // Limpar apenas agendamentos e caixa de testes
}

@Composable
fun ResetDataDialog(
    salonName: String,
    onDismiss: () -> Unit,
    onConfirmReset: (option: ResetOption, resetSalonName: Boolean) -> Unit
) {
    var selectedOption by remember { mutableStateOf(ResetOption.CLEAR_ALL_BLANK) }
    var resetSalonNameToDefault by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(52.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = "Limpar dados",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Limpar Dados do Aplicativo",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Deseja resetar as informações salvas no banco de dados e recomeçar? Escolha como prefere iniciar:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Opção 1: Começar do Zero (Banco Limpo)
                ResetOptionCard(
                    title = "Começar do Zero (Banco 100% Vazio)",
                    subtitle = "Remove todos os clientes, agendamentos, serviços, profissionais e estoque. Fica totalmente limpo para você configurar seu salão do seu jeito.",
                    icon = Icons.Filled.CleaningServices,
                    isSelected = selectedOption == ResetOption.CLEAR_ALL_BLANK,
                    onClick = { selectedOption = ResetOption.CLEAR_ALL_BLANK },
                    isDestructive = true
                )

                // Opção 2: Restaurar Catálogo Padrão
                ResetOptionCard(
                    title = "Restaurar Catálogo Padrão (Sem Agendamentos)",
                    subtitle = "Limpa agendamentos e transações de teste, recarregando os serviços essenciais e profissionais padrão (Vanira & Vanessa).",
                    icon = Icons.Filled.Refresh,
                    isSelected = selectedOption == ResetOption.RESTORE_DEFAULT_TEMPLATE,
                    onClick = { selectedOption = ResetOption.RESTORE_DEFAULT_TEMPLATE }
                )

                // Opção 3: Limpar apenas Agendamentos e Caixa
                ResetOptionCard(
                    title = "Limpar Apenas Agendamentos & Caixa",
                    subtitle = "Preserva os serviços, profissionais e clientes cadastrados, apagando somente os horários agendados e movimentações financeiras.",
                    icon = Icons.Filled.Warning,
                    isSelected = selectedOption == ResetOption.CLEAR_APPOINTMENTS_ONLY,
                    onClick = { selectedOption = ResetOption.CLEAR_APPOINTMENTS_ONLY }
                )

                // Checkbox para resetar o nome do salão
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { resetSalonNameToDefault = !resetSalonNameToDefault }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = resetSalonNameToDefault,
                        onCheckedChange = { resetSalonNameToDefault = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Redefinir nome para \"Vanira e Vanessa Salão Especializado\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmReset(selectedOption, resetSalonNameToDefault)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("btn_confirm_reset_data")
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (selectedOption) {
                        ResetOption.CLEAR_ALL_BLANK -> "Zerar Tudo"
                        ResetOption.RESTORE_DEFAULT_TEMPLATE -> "Restaurar Padrão"
                        ResetOption.CLEAR_APPOINTMENTS_ONLY -> "Limpar Agendamentos"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_reset_data")
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ResetOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val borderColor = if (isSelected) {
        if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    val containerColor = if (isSelected) {
        if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected && isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected && isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
