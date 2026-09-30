package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Client

@Composable
fun ShareClientPortalDialog(
    salonName: String,
    portalUrl: String,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onSendWhatsApp: (phone: String, message: String) -> Unit,
    onCopyLink: (String) -> Unit,
    onUpdatePortalUrl: ((String) -> Unit)? = null,
    onTestUrlConnection: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onOpenClientMode: ((Client?) -> Unit)? = null,
    onShareWebApp: ((Client?, phone: String, name: String) -> Unit)? = null,
    onOpenWebAppInBrowser: ((Client?) -> Unit)? = null,
    onLockDeviceAsClient: (() -> Unit)? = null,
    isCurrentDeviceClient: Boolean = false
) {
    val context = LocalContext.current
    var selectedChannelTab by remember { mutableIntStateOf(0) } // 0: Web App (.html), 1: App Android (.apk), 2: Link Web
    var selectedClientTab by remember { mutableIntStateOf(0) } // 0: Cadastrada, 1: Digitar

    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var clientSearchQuery by remember { mutableStateOf("") }

    var customName by remember { mutableStateOf("") }
    var customPhone by remember { mutableStateOf("") }

    var currentConfiguredUrl by remember(portalUrl) { mutableStateOf(portalUrl) }
    var showUrlEditor by remember { mutableStateOf(false) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultFeedback by remember { mutableStateOf<String?>(null) }
    var testResultSuccess by remember { mutableStateOf(true) }

    val filteredClients = remember(clients, clientSearchQuery) {
        if (clientSearchQuery.isBlank()) clients
        else clients.filter {
            it.name.contains(clientSearchQuery, ignoreCase = true) ||
                    it.phone.contains(clientSearchQuery)
        }
    }

    val targetClientName = when (selectedClientTab) {
        0 -> selectedClient?.name ?: ""
        else -> customName
    }

    val targetClientPhone = when (selectedClientTab) {
        0 -> selectedClient?.phone ?: ""
        else -> customPhone
    }

    val computedLink = remember(currentConfiguredUrl, selectedClient) {
        val base = currentConfiguredUrl.trim()
        val token = selectedClient?.getPortalToken()
        val separator = if (base.contains("?")) "&" else "?"
        val tokenPart = if (token != null) "token=$token&" else ""
        "$base${separator}${tokenPart}mode=client&client_only=true"
    }

    val directAppLink = remember(selectedClient) {
        val token = selectedClient?.getPortalToken()
        val tokenPart = if (token != null) "&token=$token" else ""
        "vaniraevanessa://portal?mode=client&client_only=true&lock_client=true$tokenPart"
    }

    val defaultMessage = remember(salonName, targetClientName, computedLink, directAppLink, selectedChannelTab) {
        val greeting = if (targetClientName.isNotBlank()) "Olá, ${targetClientName.trim()}! ✨" else "Olá! ✨"
        when (selectedChannelTab) {
            0 -> """
$greeting

Aqui é do *$salonName*! 💇‍♀️💅

Segue seu *Web App do Portal do Cliente* em anexo!
Você pode abrir direto no seu navegador (Chrome ou Safari) para agendar online e acompanhar seus horários em tempo real, sem precisar baixar nada!

✨ Nossos serviços, valores e profissionais atualizados
✅ Escolha data e horário com confirmação rápida
Te esperamos com todo carinho! 💕
            """.trimIndent()

            1 -> """
$greeting

Aqui é do *$salonName*! 💇‍♀️💅

Agora você tem o *Aplicativo Exclusivo do Portal do Cliente* no seu celular!

📱 Para abrir seu Portal no Aplicativo:
$directAppLink

✅ Agendamento 24h pelo celular com atualização automática na agenda do salão!
✅ Escolha sua profissional favorita (Vanira, Vanessa e equipe)
✅ Seus dados e horários sempre sincronizados!

Te esperamos com todo carinho! 💕
            """.trimIndent()

            else -> """
$greeting

Aqui é do *$salonName*! 💇‍♀️💅

Acesse seu Portal de Agendamento Online no celular:
$computedLink

📱 Link rápido direto no Aplicativo:
$directAppLink

✅ Veja nossos serviços, valores e profissionais atualizados
✅ Confirmação instantânea na agenda do salão!

Te esperamos com todo carinho! 💕
            """.trimIndent()
        }
    }

    var editableMessage by remember(defaultMessage) { mutableStateOf(defaultMessage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF1E7E34),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Portal do Cliente",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Enviar Web App ou Aplicativo para cliente",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                // Channel Selection Tab: Web App vs App Android vs Link Web
                TabRow(selectedTabIndex = selectedChannelTab) {
                    Tab(
                        selected = selectedChannelTab == 0,
                        onClick = { selectedChannelTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Web App (.html)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedChannelTab == 1,
                        onClick = { selectedChannelTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("App (.apk)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedChannelTab == 2,
                        onClick = { selectedChannelTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Link Web", fontSize = 11.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Explanatory Banner based on tab
                when (selectedChannelTab) {
                    0 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Web App Completo (Sem Erro de Página / 404)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF1B5E20),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Gera um Web App responsivo com os serviços e profissionais do salão. Abre direto no WhatsApp/Chrome/Safari de qualquer celular, sem tela em branco!",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF1B5E20),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                )

                                if (targetClientName.isNotBlank() || targetClientPhone.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Destinatária: ${targetClientName.ifBlank { "Cliente" }} • 📱 ${targetClientPhone.ifBlank { "Sem WhatsApp informado" }}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF1B5E20),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onShareWebApp?.invoke(selectedClient, targetClientPhone, targetClientName)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("share_webapp_file_btn")
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (targetClientName.isNotBlank()) "Enviar para $targetClientName" else "Enviar Web App (.html)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onOpenWebAppInBrowser?.invoke(selectedClient)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("preview_webapp_btn")
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Testar", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Smartphone,
                                        contentDescription = null,
                                        tint = Color(0xFF6A1B9A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Aplicativo no Modo Cliente (Apenas Portal)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF4A148C),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "O aplicativo abre exclusivamente no Portal de Agendamento, ocultando as abas de administração do salão e gravando agendamentos direto na base de dados.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF4A148C),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onOpenClientMode?.invoke(selectedClient)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("open_client_mode_now_btn")
                                    ) {
                                        Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Abrir Portal no App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (onLockDeviceAsClient != null) {
                                        OutlinedButton(
                                            onClick = onLockDeviceAsClient,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("lock_device_client_btn")
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isCurrentDeviceClient) "Aparelho Travado" else "Travar Aparelho", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "URL Web Hospedada:",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = computedLink,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                    Row {
                                        IconButton(
                                            onClick = { onCopyLink(computedLink) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { showUrlEditor = !showUrlEditor },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            isTestingConnection = true
                                            testResultFeedback = null
                                            if (onTestUrlConnection != null) {
                                                onTestUrlConnection(computedLink) { success, msg ->
                                                    isTestingConnection = false
                                                    testResultSuccess = success
                                                    testResultFeedback = msg
                                                }
                                            } else {
                                                isTestingConnection = false
                                                testResultSuccess = true
                                                testResultFeedback = "Link testado!"
                                            }
                                        },
                                        enabled = !isTestingConnection,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (isTestingConnection) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Testando...", fontSize = 11.sp)
                                        } else {
                                            Text("🧪 Testar Link Web", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (testResultFeedback != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (testResultSuccess) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (testResultSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (testResultSuccess) Color(0xFF2E7D32) else Color(0xFFE65100),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = testResultFeedback ?: "",
                                                fontSize = 11.sp,
                                                color = if (testResultSuccess) Color(0xFF1B5E20) else Color(0xFFE65100),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = showUrlEditor) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        OutlinedTextField(
                                            value = currentConfiguredUrl,
                                            onValueChange = { currentConfiguredUrl = it },
                                            label = { Text("URL Base do Portal Web") },
                                            placeholder = { Text("https://meu-salao.com") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = {
                                                onUpdatePortalUrl?.invoke(currentConfiguredUrl)
                                                showUrlEditor = false
                                                Toast.makeText(context, "URL salva!", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Text("Salvar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Client Selection Tabs
                TabRow(selectedTabIndex = selectedClientTab) {
                    Tab(
                        selected = selectedClientTab == 0,
                        onClick = { selectedClientTab = 0 },
                        text = { Text("Cliente Cadastrada", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedClientTab == 1,
                        onClick = { selectedClientTab = 1 },
                        text = { Text("Digitar Contato", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedClientTab) {
                    0 -> {
                        OutlinedTextField(
                            value = clientSearchQuery,
                            onValueChange = { clientSearchQuery = it },
                            placeholder = { Text("Buscar cliente por nome ou WhatsApp...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 110.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredClients) { client ->
                                val isSelected = selectedClient?.id == client.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .clickable { selectedClient = client }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = client.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "📱 ${client.phone}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Nome da Cliente") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customPhone,
                            onValueChange = { customPhone = it },
                            label = { Text("WhatsApp com DDD") },
                            placeholder = { Text("Ex: 11987654321") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Editable Message
                Text(
                    text = "Mensagem que será enviada no WhatsApp:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editableMessage,
                    onValueChange = { editableMessage = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 120.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            val phoneToSend = targetClientPhone.trim()
            val canSend = phoneToSend.isNotBlank()

            Button(
                onClick = {
                    onSendWhatsApp(phoneToSend, editableMessage)
                    onDismiss()
                },
                enabled = canSend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("send_portal_whatsapp_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Enviar via WhatsApp",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Fechar")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
