package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Appointment
import com.example.data.model.Client
import com.example.data.model.PaymentTransaction
import com.example.ui.AdminTab
import com.example.ui.components.AppointmentCard
import com.example.ui.components.QuickActionsGrid
import com.example.ui.theme.BellaGold
import com.example.ui.theme.BellaPrimary
import com.example.ui.theme.BellaSecondary
import com.example.ui.theme.StatusCanceled
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusConfirmed
import com.example.ui.theme.StatusPending
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    todayAppointments: List<Appointment>,
    allTransactions: List<PaymentTransaction>,
    clients: List<Client>,
    onNewAppointmentClick: () -> Unit,
    onTabSelect: (AdminTab) -> Unit,
    onStatusChange: (Long, String) -> Unit,
    onWhatsAppClick: (String, String) -> Unit,
    onAddToCalendar: (Appointment) -> Unit,
    salonName: String = "Vanira e Vanessa Salão Especializado",
    onSharePortalClick: (() -> Unit)? = null,
    onBackupClick: (() -> Unit)? = null,
    onResetClick: (() -> Unit)? = null,
    onNewClientClick: (() -> Unit)? = null,
    onNewServiceClick: (() -> Unit)? = null,
    onNewProfessionalClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    val errorColor = MaterialTheme.colorScheme.error

    // Counts for today's badges
    val totalToday = todayAppointments.size
    val confirmedCount = todayAppointments.count { it.status == "CONFIRMADO" }
    val pendingCount = todayAppointments.count { it.status == "AGUARDANDO" }
    val canceledCount = todayAppointments.count { it.status == "CANCELADO" }
    val completedCount = todayAppointments.count { it.status == "CONCLUIDO" }

    // Financial Metrics
    val todayRevenue = allTransactions
        .filter { it.status == "PAGO" && it.dateStr == todayAppointments.firstOrNull()?.dateStr }
        .sumOf { it.amount }
    val monthRevenue = allTransactions
        .filter { it.status == "PAGO" }
        .sumOf { it.amount }
    val pendingReceivable = allTransactions
        .filter { it.status == "PENDENTE" }
        .sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Visual Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.salon_hero_banner),
                    contentDescription = "Studio Bella Interior",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC2A081D))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "💇‍♀️ $salonName",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Painel de Controle • Visão Geral em Tempo Real",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }

        // WhatsApp Client Portal Invitation Card
        if (onSharePortalClick != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📱 Portal do Salão via WhatsApp para Celular",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Envie o link do portal para suas clientes agendarem pelo celular. Sincronizado instantaneamente na sua agenda!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF2E7D32),
                                    lineHeight = 16.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSharePortalClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("dashboard_share_portal_btn")
                        ) {
                            Text("Enviar Link do Portal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section "Hoje" with badges (Exact style requested: 🟣 08 atendimentos, 🟢 05 confirmados...)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Hoje",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusMetricCard(
                        count = totalToday.toString().padStart(2, '0'),
                        label = "atendimentos",
                        emoji = "🟣",
                        accentColor = StatusCompleted,
                        modifier = Modifier.weight(1f)
                    )
                    StatusMetricCard(
                        count = confirmedCount.toString().padStart(2, '0'),
                        label = "confirmados",
                        emoji = "🟢",
                        accentColor = StatusConfirmed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusMetricCard(
                        count = pendingCount.toString().padStart(2, '0'),
                        label = "aguardando",
                        emoji = "🟠",
                        accentColor = StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                    StatusMetricCard(
                        count = canceledCount.toString().padStart(2, '0'),
                        label = "cancelamento",
                        emoji = "🔴",
                        accentColor = StatusCanceled,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Financial & Key Metrics Row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Faturamento & Caixa",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FinancialMetricCard(
                        title = "Faturamento Mês",
                        value = currencyFormat.format(monthRevenue),
                        subtitle = "Hoje: ${currencyFormat.format(todayRevenue)}",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                    FinancialMetricCard(
                        title = "Contas a Receber",
                        value = currencyFormat.format(pendingReceivable),
                        subtitle = "Pagamentos pendentes",
                        icon = Icons.Default.AttachMoney,
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Big Action Buttons Grid
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ações Rápidas",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                QuickActionsGrid(
                    onNewAppointmentClick = onNewAppointmentClick,
                    onTabSelect = onTabSelect
                )

                // Cadastros Rápidos Compact Buttons
                if (onNewClientClick != null || onNewServiceClick != null || onNewProfessionalClick != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Cadastros Rápidos:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (onNewClientClick != null) {
                                    OutlinedButton(
                                        onClick = onNewClientClick,
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .testTag("quick_add_client_btn")
                                    ) {
                                        Text(text = "+ Cliente", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                if (onNewServiceClick != null) {
                                    OutlinedButton(
                                        onClick = onNewServiceClick,
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .testTag("quick_add_service_btn")
                                    ) {
                                        Text(text = "+ Serviço", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                if (onNewProfessionalClick != null) {
                                    OutlinedButton(
                                        onClick = onNewProfessionalClick,
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .testTag("quick_add_prof_btn")
                                    ) {
                                        Text(text = "+ Profissional", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Database Backup & Reset Dedicated Section
        if (onBackupClick != null || onResetClick != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🗄️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Base de Dados Local & Segurança",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Faça backup geral em JSON ou limpe registros de testes",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (onBackupClick != null) {
                                Button(
                                    onClick = onBackupClick,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("dashboard_btn_backup")
                                ) {
                                    Text(
                                        text = "💾 Backup Geral",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (onResetClick != null) {
                                OutlinedButton(
                                    onClick = onResetClick,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = errorColor
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("dashboard_btn_reset")
                                ) {
                                    Text(
                                        text = "🧹 Limpar Base",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = errorColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Próximos Horários (Header + List)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Próximos Horários",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                TextButton(onClick = { onTabSelect(AdminTab.AGENDA) }) {
                    Text(text = "Ver Agenda Completa")
                }
            }
        }

        if (todayAppointments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum agendamento para hoje ainda.\nToque em '+ Novo Agendamento' para marcar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(todayAppointments) { app ->
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
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StatusMetricCard(
    count: String,
    label: String,
    emoji: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "$count $label",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
fun FinancialMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}
