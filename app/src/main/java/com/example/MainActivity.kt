package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SalonDatabase
import com.example.data.SalonRepository
import com.example.ui.AdminTab
import com.example.ui.AppMode
import com.example.ui.SalonViewModel
import com.example.ui.SalonViewModelFactory
import com.example.ui.components.AdminPinUnlockDialog
import com.example.ui.components.DatabaseBackupDialog
import com.example.ui.components.EditSalonNameDialog
import com.example.ui.components.NewAppointmentDialog
import com.example.ui.components.NewClientDialog
import com.example.ui.components.NewScheduleBlockDialog
import com.example.ui.components.NewServiceDialog
import com.example.ui.components.ResetDataDialog
import com.example.ui.components.ResetOption
import com.example.ui.components.SalonTopBar
import com.example.ui.components.ShareClientPortalDialog
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.ClientPortalScreen
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinancialScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.ProfessionalsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.SupabaseConfigScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            val database = SalonDatabase.getDatabase(applicationContext, lifecycleScope)
            val repository = SalonRepository(database.salonDao(), applicationContext)
            val factory = SalonViewModelFactory(repository)

            setContent {
                MyApplicationTheme {
                    val viewModel: SalonViewModel = viewModel(factory = factory)
                    SalonApp(viewModel = viewModel)
                }
            }
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "Fatal error during MainActivity initialization", e)
            setContent {
                MyApplicationTheme {
                    androidx.compose.material3.Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize()
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = "Iniciando Vanira e Vanessa Salão Especializado...",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun SalonApp(viewModel: SalonViewModel) {
    val context = LocalContext.current
    val currentActivity = context as? ComponentActivity

    // Handle incoming deep link or web portal link click
    androidx.compose.runtime.LaunchedEffect(currentActivity?.intent?.data) {
        currentActivity?.intent?.data?.let { uri ->
            viewModel.handleDeepLink(uri)
        }
    }

    val appMode by viewModel.appMode.collectAsStateWithLifecycle()
    val currentAdminTab by viewModel.currentAdminTab.collectAsStateWithLifecycle()
    val salonName by viewModel.salonName.collectAsStateWithLifecycle()
    val clientPortalUrl by viewModel.clientPortalUrl.collectAsStateWithLifecycle()

    val professionals by viewModel.allProfessionals.collectAsStateWithLifecycle()
    val activeProfessionals by viewModel.activeProfessionals.collectAsStateWithLifecycle()
    val services by viewModel.allServices.collectAsStateWithLifecycle()
    val clients by viewModel.allClients.collectAsStateWithLifecycle()
    val filteredClients by viewModel.filteredClients.collectAsStateWithLifecycle()
    val clientSearchQuery by viewModel.clientSearchQuery.collectAsStateWithLifecycle()

    val allAppointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val filteredAppointments by viewModel.filteredAppointments.collectAsStateWithLifecycle()
    val selectedAgendaDate by viewModel.selectedAgendaDate.collectAsStateWithLifecycle()
    val selectedProfFilter by viewModel.selectedProfessionalFilter.collectAsStateWithLifecycle()

    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val pendingTransactions by viewModel.pendingTransactions.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val activeClientForPortal by viewModel.activeClientForPortal.collectAsStateWithLifecycle()
    val supabaseUrl by viewModel.supabaseUrl.collectAsStateWithLifecycle()
    val supabaseKey by viewModel.supabaseKey.collectAsStateWithLifecycle()
    val isSupabaseRealtimeEnabled by viewModel.isSupabaseRealtimeEnabled.collectAsStateWithLifecycle()

    val allScheduleBlocks by viewModel.allScheduleBlocks.collectAsStateWithLifecycle()
    val lastBackupInfo by viewModel.lastBackupInfo.collectAsStateWithLifecycle()
    val isBackingUp by viewModel.isBackingUp.collectAsStateWithLifecycle()
    val isAutoBackupActive by viewModel.isAutoBackupActive.collectAsStateWithLifecycle()
    val isClientOnlyMode by viewModel.isClientOnlyMode.collectAsStateWithLifecycle()

    // Dialogs state
    var showNewAppointmentDialog by remember { mutableStateOf(false) }
    var showNewClientDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<com.example.data.model.Client?>(null) }
    var showNewServiceDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<com.example.data.model.SalonService?>(null) }
    var showSharePortalDialog by remember { mutableStateOf(false) }
    var showEditSalonNameDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showScheduleBlockDialog by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }

    // Today's appointments for Dashboard
    val todayAppointments = remember(allAppointments, viewModel.todayDateStr) {
        allAppointments.filter { it.dateStr == viewModel.todayDateStr }.sortedBy { it.timeStr }
    }

    // Handle back button
    BackHandler(enabled = (appMode == AppMode.CLIENT_PORTAL && !isClientOnlyMode) || currentAdminTab != AdminTab.DASHBOARD) {
        if (appMode == AppMode.CLIENT_PORTAL && !isClientOnlyMode) {
            viewModel.switchMode(AppMode.ADMIN)
        } else if (currentAdminTab != AdminTab.DASHBOARD) {
            viewModel.selectAdminTab(AdminTab.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SalonTopBar(
                currentMode = appMode,
                onModeChange = { viewModel.switchMode(it) },
                salonName = salonName,
                onEditSalonNameClick = { showEditSalonNameDialog = true },
                onSharePortalClick = { showSharePortalDialog = true },
                onBackupClick = { showBackupDialog = true },
                onResetClick = { showResetDialog = true },
                isClientOnly = isClientOnlyMode,
                onAdminUnlockClick = { showAdminPinDialog = true }
            )
        },
        bottomBar = {
            if (appMode == AppMode.ADMIN && !isClientOnlyMode) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentAdminTab == AdminTab.DASHBOARD,
                        onClick = { viewModel.selectAdminTab(AdminTab.DASHBOARD) },
                        icon = {
                            Icon(
                                imageVector = if (currentAdminTab == AdminTab.DASHBOARD) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Início"
                            )
                        },
                        label = { Text("Início") },
                        modifier = Modifier.testTag("nav_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentAdminTab == AdminTab.AGENDA,
                        onClick = { viewModel.selectAdminTab(AdminTab.AGENDA) },
                        icon = {
                            Icon(
                                imageVector = if (currentAdminTab == AdminTab.AGENDA) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                contentDescription = "Agenda"
                            )
                        },
                        label = { Text("Agenda") },
                        modifier = Modifier.testTag("nav_agenda")
                    )

                    NavigationBarItem(
                        selected = currentAdminTab == AdminTab.CLIENTES,
                        onClick = { viewModel.selectAdminTab(AdminTab.CLIENTES) },
                        icon = {
                            Icon(
                                imageVector = if (currentAdminTab == AdminTab.CLIENTES) Icons.Filled.Group else Icons.Outlined.Group,
                                contentDescription = "Clientes"
                            )
                        },
                        label = { Text("Clientes") },
                        modifier = Modifier.testTag("nav_clients")
                    )

                    NavigationBarItem(
                        selected = currentAdminTab == AdminTab.FINANCEIRO,
                        onClick = { viewModel.selectAdminTab(AdminTab.FINANCEIRO) },
                        icon = {
                            Icon(
                                imageVector = if (currentAdminTab == AdminTab.FINANCEIRO) Icons.Filled.Payments else Icons.Outlined.Payments,
                                contentDescription = "Financeiro"
                            )
                        },
                        label = { Text("Financeiro") },
                        modifier = Modifier.testTag("nav_financial")
                    )

                    NavigationBarItem(
                        selected = currentAdminTab == AdminTab.IA_ASSISTENTE,
                        onClick = { viewModel.selectAdminTab(AdminTab.IA_ASSISTENTE) },
                        icon = {
                            Icon(
                                imageVector = if (currentAdminTab == AdminTab.IA_ASSISTENTE) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                contentDescription = "IA",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("IA") },
                        modifier = Modifier.testTag("nav_ai")
                    )

                    NavigationBarItem(
                        selected = currentAdminTab in listOf(AdminTab.SERVICOS, AdminTab.RELATORIOS, AdminTab.PROFISSIONAIS),
                        onClick = { moreMenuExpanded = true },
                        icon = {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "Mais opções"
                                )
                                DropdownMenu(
                                    expanded = moreMenuExpanded,
                                    onDismissRequest = { moreMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("📱 Enviar Portal via WhatsApp") },
                                        onClick = {
                                            moreMenuExpanded = false
                                            showSharePortalDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("✏️ Alterar Nome do Salão") },
                                        onClick = {
                                            moreMenuExpanded = false
                                            showEditSalonNameDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("💇 Catálogo de Serviços") },
                                        onClick = {
                                            viewModel.selectAdminTab(AdminTab.SERVICOS)
                                            moreMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("📊 Relatórios & Gráficos") },
                                        onClick = {
                                            viewModel.selectAdminTab(AdminTab.RELATORIOS)
                                            moreMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("👩‍💼 Gestão da Equipe") },
                                        onClick = {
                                            viewModel.selectAdminTab(AdminTab.PROFISSIONAIS)
                                            moreMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("📦 Controle de Estoque") },
                                        onClick = {
                                            viewModel.selectAdminTab(AdminTab.ESTOQUE)
                                            moreMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🗄️ Supabase Cloud & Realtime") },
                                        onClick = {
                                            viewModel.selectAdminTab(AdminTab.SUPABASE_CLOUD)
                                            moreMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("💾 Backup & Restauração (JSON)") },
                                        onClick = {
                                            moreMenuExpanded = false
                                            showBackupDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🧹 Limpar / Resetar Dados") },
                                        onClick = {
                                            moreMenuExpanded = false
                                            showResetDialog = true
                                        }
                                    )
                                }
                            }
                        },
                        label = { Text("Mais") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (appMode) {
                AppMode.CLIENT_PORTAL -> {
                    ClientPortalScreen(
                        services = services,
                        professionals = activeProfessionals,
                        allAppointments = allAppointments,
                        todayDate = viewModel.todayDateStr,
                        salonName = salonName,
                        activeClient = activeClientForPortal,
                        occupiedTimesProvider = { profId, dateStr ->
                            viewModel.getOccupiedTimesFor(profId, dateStr)
                        },
                        onBookAppointment = { name, phone, serv, prof, date, time, notes ->
                            viewModel.addAppointment(
                                clientName = name,
                                clientPhone = phone,
                                clientId = null,
                                service = serv,
                                professional = prof,
                                dateStr = date,
                                timeStr = time,
                                notes = notes
                            )
                        },
                        onCancelAppointment = { id ->
                            viewModel.cancelAppointmentByClient(id)
                        },
                        onRescheduleAppointment = { app, newDate, newTime ->
                            viewModel.rescheduleAppointment(app, newDate, newTime)
                        },
                        onAddToCalendar = { app ->
                            viewModel.addToGoogleCalendar(context, app)
                        },
                        onWhatsAppClick = { phone, msg ->
                            viewModel.openWhatsApp(context, phone, msg)
                        },
                        onSharePortalClick = {
                            showSharePortalDialog = true
                        },
                        onSaveClientProfile = { updatedClient ->
                            viewModel.updateClient(updatedClient)
                        },
                        clients = clients,
                        onShareWebApp = { client, phone, name ->
                            viewModel.shareWebApp(client, phone, name)
                        },
                        onOpenWebAppInBrowser = { client ->
                            viewModel.openWebAppInBrowser(client)
                        }
                    )
                }

                AppMode.ADMIN -> {
                    when (currentAdminTab) {
                        AdminTab.DASHBOARD -> {
                            DashboardScreen(
                                todayAppointments = todayAppointments,
                                allTransactions = allTransactions,
                                clients = clients,
                                onNewAppointmentClick = { showNewAppointmentDialog = true },
                                onTabSelect = { viewModel.selectAdminTab(it) },
                                onStatusChange = { id, status -> viewModel.updateAppointmentStatus(id, status) },
                                onWhatsAppClick = { phone, msg -> viewModel.openWhatsApp(context, phone, msg) },
                                onAddToCalendar = { app -> viewModel.addToGoogleCalendar(context, app) },
                                salonName = salonName,
                                onSharePortalClick = { showSharePortalDialog = true },
                                onBackupClick = { showBackupDialog = true },
                                onResetClick = { showResetDialog = true },
                                onNewClientClick = {
                                    clientToEdit = null
                                    showNewClientDialog = true
                                },
                                onNewServiceClick = {
                                    serviceToEdit = null
                                    showNewServiceDialog = true
                                },
                                onNewProfessionalClick = {
                                    viewModel.selectAdminTab(AdminTab.PROFISSIONAIS)
                                }
                            )
                        }

                        AdminTab.AGENDA -> {
                            ScheduleScreen(
                                appointments = filteredAppointments,
                                professionals = activeProfessionals,
                                selectedDate = selectedAgendaDate,
                                todayDate = viewModel.todayDateStr,
                                selectedProfessionalId = selectedProfFilter,
                                onSelectDate = { viewModel.selectAgendaDate(it) },
                                onFilterProfessional = { viewModel.filterByProfessional(it) },
                                onNewAppointmentClick = { showNewAppointmentDialog = true },
                                onStatusChange = { id, status -> viewModel.updateAppointmentStatus(id, status) },
                                onWhatsAppClick = { phone, msg -> viewModel.openWhatsApp(context, phone, msg) },
                                onAddToCalendar = { app -> viewModel.addToGoogleCalendar(context, app) },
                                salonName = salonName,
                                scheduleBlocks = allScheduleBlocks,
                                onAddBlockClick = { showScheduleBlockDialog = true },
                                onDeleteBlock = { blockId -> viewModel.deleteScheduleBlock(blockId) }
                            )
                        }

                        AdminTab.CLIENTES -> {
                            ClientsScreen(
                                clients = filteredClients,
                                searchQuery = clientSearchQuery,
                                onSearchChange = { viewModel.updateClientSearch(it) },
                                onAddClientClick = {
                                    clientToEdit = null
                                    showNewClientDialog = true
                                },
                                onWhatsAppClick = { phone, msg -> viewModel.openWhatsApp(context, phone, msg) },
                                salonName = salonName,
                                onEditClient = { client -> viewModel.updateClient(client) },
                                onDeleteClient = { client -> viewModel.deleteClient(client) },
                                onOpenInPortal = { client ->
                                    viewModel.setActiveClientForPortal(client)
                                    viewModel.switchMode(AppMode.CLIENT_PORTAL)
                                },
                                onShareWebApp = { client, phone, name ->
                                    viewModel.shareWebApp(client, phone, name)
                                },
                                onSharePortalClick = {
                                    showSharePortalDialog = true
                                }
                            )
                        }

                        AdminTab.SERVICOS -> {
                            ServicesScreen(
                                services = services,
                                professionals = professionals,
                                onAddServiceClick = {
                                    serviceToEdit = null
                                    showNewServiceDialog = true
                                },
                                onEditServiceClick = { service ->
                                    serviceToEdit = service
                                    showNewServiceDialog = true
                                },
                                onDeleteServiceClick = { service ->
                                    viewModel.deleteService(service)
                                }
                            )
                        }

                        AdminTab.FINANCEIRO -> {
                            FinancialScreen(
                                transactions = allTransactions,
                                onMarkPaid = { id, method -> viewModel.markTransactionPaid(id, method) },
                                onWhatsAppClick = { phone, msg -> viewModel.openWhatsApp(context, phone, msg) }
                            )
                        }

                        AdminTab.IA_ASSISTENTE -> {
                            AiAssistantScreen(
                                messages = chatMessages,
                                isLoading = isAiLoading,
                                onSendMessage = { viewModel.sendAiPrompt(it) }
                            )
                        }

                        AdminTab.RELATORIOS -> {
                            ReportsScreen(
                                appointments = allAppointments,
                                transactions = allTransactions,
                                clients = clients,
                                salonName = salonName
                            )
                        }

                        AdminTab.PROFISSIONAIS -> {
                            ProfessionalsScreen(
                                professionals = professionals,
                                services = services,
                                onAddProfessional = { name, role, phone, emoji, serviceIds ->
                                    viewModel.addProfessional(name, role, phone, emoji, serviceIds)
                                },
                                onEditProfessional = { prof, serviceIds ->
                                    viewModel.updateProfessional(prof, serviceIds)
                                },
                                onDeleteProfessional = { prof ->
                                    viewModel.deleteProfessional(prof)
                                }
                            )
                        }

                        AdminTab.ESTOQUE -> {
                            InventoryScreen(
                                products = allProducts,
                                onAddProduct = { name, brand, cat, qty, min, cost, sell, barcode, desc ->
                                    viewModel.addProduct(name, brand, cat, qty, min, cost, sell, barcode, desc)
                                },
                                onUpdateProduct = { prod ->
                                    viewModel.updateProduct(prod)
                                },
                                onDeleteProduct = { prod ->
                                    viewModel.deleteProduct(prod)
                                },
                                onAdjustStock = { id, delta ->
                                    viewModel.adjustProductStock(id, delta)
                                },
                                salonName = salonName
                            )
                        }

                        AdminTab.SUPABASE_CLOUD -> {
                            SupabaseConfigScreen(
                                currentUrl = supabaseUrl,
                                currentKey = supabaseKey,
                                isRealtimeEnabled = isSupabaseRealtimeEnabled,
                                onSaveConfig = { url, key, realtime ->
                                    viewModel.updateSupabaseConfig(url, key, realtime)
                                },
                                sqlScript = viewModel.generateSupabaseSqlScript(),
                                salonName = salonName
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showNewAppointmentDialog) {
        NewAppointmentDialog(
            clients = clients,
            services = services,
            professionals = activeProfessionals,
            defaultDate = selectedAgendaDate,
            onDismiss = { showNewAppointmentDialog = false },
            onConfirm = { name, phone, clientId, serv, prof, date, time, notes ->
                viewModel.addAppointment(
                    clientName = name,
                    clientPhone = phone,
                    clientId = clientId,
                    service = serv,
                    professional = prof,
                    dateStr = date,
                    timeStr = time,
                    notes = notes
                )
                showNewAppointmentDialog = false
            }
        )
    }

    if (showNewClientDialog) {
        NewClientDialog(
            clientToEdit = clientToEdit,
            onDismiss = {
                showNewClientDialog = false
                clientToEdit = null
            },
            onSave = { name, phone, birthDate, address, hairPreferences, notes ->
                val toEdit = clientToEdit
                if (toEdit != null) {
                    viewModel.updateClient(
                        toEdit.copy(
                            name = name,
                            phone = phone,
                            birthDate = birthDate,
                            address = address,
                            hairPreferences = hairPreferences,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.addClient(name, phone, birthDate, address, hairPreferences, notes)
                }
                showNewClientDialog = false
                clientToEdit = null
            }
        )
    }

    if (showNewServiceDialog) {
        NewServiceDialog(
            serviceToEdit = serviceToEdit,
            allProfessionals = professionals,
            onDismiss = {
                showNewServiceDialog = false
                serviceToEdit = null
            },
            onSave = { name, category, price, duration, description, assignedProfIds ->
                val toEdit = serviceToEdit
                if (toEdit != null) {
                    viewModel.updateService(
                        toEdit.copy(
                            name = name,
                            category = category,
                            price = price,
                            durationMinutes = duration,
                            description = description
                        ),
                        assignedProfIds
                    )
                } else {
                    viewModel.addService(name, category, price, duration, description, assignedProfIds)
                }
                showNewServiceDialog = false
                serviceToEdit = null
            }
        )
    }

    if (showSharePortalDialog) {
        ShareClientPortalDialog(
            salonName = salonName,
            portalUrl = clientPortalUrl,
            clients = clients,
            onDismiss = { showSharePortalDialog = false },
            onSendWhatsApp = { phone, message ->
                viewModel.openWhatsApp(context, phone, message)
            },
            onCopyLink = {
                viewModel.copyClientPortalLink(context)
            },
            onUpdatePortalUrl = { newUrl ->
                viewModel.updateClientPortalUrl(newUrl)
            },
            onTestUrlConnection = { url, onResult ->
                viewModel.testPortalUrlConnection(url, onResult)
            },
            onOpenClientMode = { client ->
                showSharePortalDialog = false
                viewModel.openClientOnlyPortal(client)
            },
            onShareWebApp = { client, phone, name ->
                viewModel.shareWebApp(client, phone, name)
            },
            onOpenWebAppInBrowser = { client ->
                viewModel.openWebAppInBrowser(client)
            },
            onLockDeviceAsClient = {
                viewModel.setDeviceClientLock(true)
                showSharePortalDialog = false
                android.widget.Toast.makeText(context, "Aparelho configurado como Modo Cliente (Apenas Portal).", android.widget.Toast.LENGTH_LONG).show()
            },
            isCurrentDeviceClient = isClientOnlyMode
        )
    }

    if (showAdminPinDialog) {
        AdminPinUnlockDialog(
            onDismiss = { showAdminPinDialog = false },
            onVerifyPin = { pin -> viewModel.verifyAdminPin(pin) },
            onUnlockSuccess = { setAsSalonDevice ->
                if (setAsSalonDevice) {
                    viewModel.setDeviceClientLock(false)
                } else {
                    viewModel.exitClientOnlyMode()
                }
                android.widget.Toast.makeText(context, "Acesso administrativo liberado!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showEditSalonNameDialog) {
        EditSalonNameDialog(
            currentName = salonName,
            onDismiss = { showEditSalonNameDialog = false },
            onSave = { newName ->
                viewModel.updateSalonName(newName)
            }
        )
    }

    if (showBackupDialog) {
        DatabaseBackupDialog(
            salonName = salonName,
            lastBackupInfo = lastBackupInfo,
            isBackingUp = isBackingUp,
            isAutoBackupEnabled = isAutoBackupActive,
            onToggleAutoBackup = { viewModel.setAutoBackupActive(it) },
            onTriggerBackup = { onResult -> viewModel.triggerLocalBackup(onResult) },
            onShareBackup = { json -> viewModel.shareCurrentBackup(json) },
            onRestoreBackup = { json, onResult -> viewModel.restoreBackup(json, onResult) },
            onDismiss = { showBackupDialog = false }
        )
    }

    if (showResetDialog) {
        ResetDataDialog(
            salonName = salonName,
            onDismiss = { showResetDialog = false },
            onConfirmReset = { option, resetName ->
                when (option) {
                    ResetOption.CLEAR_ALL_BLANK -> viewModel.resetDatabaseBlank(resetName) {
                        showResetDialog = false
                    }
                    ResetOption.RESTORE_DEFAULT_TEMPLATE -> viewModel.restoreDatabaseDefaults(resetName) {
                        showResetDialog = false
                    }
                    ResetOption.CLEAR_APPOINTMENTS_ONLY -> viewModel.clearAppointmentsAndFinancial {
                        showResetDialog = false
                    }
                }
            }
        )
    }

    if (showScheduleBlockDialog) {
        NewScheduleBlockDialog(
            professionals = activeProfessionals,
            defaultDate = selectedAgendaDate,
            onDismiss = { showScheduleBlockDialog = false },
            onConfirm = { profId, date, start, end, reason ->
                viewModel.addScheduleBlock(profId, date, start, end, reason)
                showScheduleBlockDialog = false
            }
        )
    }
}
