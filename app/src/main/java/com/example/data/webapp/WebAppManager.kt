package com.example.data.webapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Appointment
import com.example.data.model.Client
import com.example.data.model.Professional
import com.example.data.model.SalonService
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WebAppManager(private val context: Context) {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    /**
     * Generates a complete, standalone, responsive HTML5 Progressive Web App (PWA)
     * containing the salon's actual services, professionals, and appointment booking flow.
     */
    fun generateWebAppHtml(
        salonName: String,
        services: List<SalonService>,
        professionals: List<Professional>,
        targetClient: Client? = null,
        salonPhone: String = "5511999998888"
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        val todayFormatted = dateFormat.format(Date())

        // Build JSON representation of services
        val servicesJsArray = services.joinToString(",\n") { s ->
            """
            {
                id: ${s.id},
                name: "${escapeJs(s.name)}",
                category: "${escapeJs(s.category)}",
                price: ${s.price},
                priceFormatted: "${escapeJs(currencyFormat.format(s.price))}",
                duration: ${s.durationMinutes},
                description: "${escapeJs(s.description)}",
                icon: "${escapeJs(s.iconName)}"
            }
            """.trimIndent()
        }

        // Build JSON representation of professionals
        val professionalsJsArray = professionals.filter { it.active }.joinToString(",\n") { p ->
            """
            {
                id: ${p.id},
                name: "${escapeJs(p.name)}",
                role: "${escapeJs(p.role)}",
                avatar: "${escapeJs(p.avatarEmoji)}"
            }
            """.trimIndent()
        }

        val clientPreloadName = targetClient?.name ?: ""
        val clientPreloadPhone = targetClient?.phone ?: ""

        return """
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>${escapeHtml(salonName)} - Portal de Agendamento</title>
    <meta name="theme-color" content="#9C27B0">
    <meta name="description" content="Agendamento online e consulta de horários para ${escapeHtml(salonName)}">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
    <meta name="apple-mobile-web-app-title" content="${escapeHtml(salonName)}">
    <style>
        :root {
            --primary: #8E24AA;
            --primary-dark: #6A1B9A;
            --primary-light: #F3E5F5;
            --gold: #D4AF37;
            --gold-light: #FFF9C4;
            --surface: #FFFFFF;
            --background: #FDFBF7;
            --text-main: #2D2727;
            --text-secondary: #757575;
            --border: #E0E0E0;
            --success: #2E7D32;
            --success-bg: #E8F5E9;
            --radius-lg: 16px;
            --radius-md: 10px;
            --radius-sm: 6px;
            --shadow: 0 4px 20px rgba(142, 36, 170, 0.08);
            --shadow-card: 0 2px 10px rgba(0, 0, 0, 0.05);
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            -webkit-tap-highlight-color: transparent;
        }

        body {
            background-color: var(--background);
            color: var(--text-main);
            padding-bottom: 90px;
        }

        /* Top Header */
        header {
            background: linear-gradient(135deg, #8E24AA 0%, #D81B60 100%);
            color: white;
            padding: 24px 16px 20px;
            border-bottom-left-radius: 24px;
            border-bottom-right-radius: 24px;
            box-shadow: 0 4px 15px rgba(142, 36, 170, 0.25);
            text-align: center;
            position: relative;
        }

        .salon-avatar {
            width: 60px;
            height: 60px;
            background: white;
            color: var(--primary);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 32px;
            margin: 0 auto 10px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.15);
        }

        h1 {
            font-size: 20px;
            font-weight: 700;
            letter-spacing: -0.3px;
        }

        .subtitle {
            font-size: 13px;
            opacity: 0.9;
            margin-top: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
        }

        .badge-online {
            background: #4CAF50;
            color: white;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
        }

        /* Container */
        .container {
            max-width: 500px;
            margin: -12px auto 0;
            padding: 0 14px;
        }

        /* App Banner / Direct Link */
        .app-link-card {
            background: white;
            border-radius: var(--radius-md);
            padding: 12px 14px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            box-shadow: var(--shadow-card);
            border: 1px solid #E1BEE7;
            margin-bottom: 14px;
        }

        .app-link-text {
            font-size: 12px;
            color: var(--primary-dark);
            font-weight: 600;
        }

        .btn-open-app {
            background: var(--primary);
            color: white;
            border: none;
            padding: 6px 12px;
            border-radius: 8px;
            font-size: 11px;
            font-weight: 700;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
        }

        /* Tabs */
        .tab-bar {
            display: flex;
            background: #ECEFF1;
            border-radius: 12px;
            padding: 4px;
            margin-bottom: 16px;
        }

        .tab-btn {
            flex: 1;
            padding: 10px 4px;
            border: none;
            background: transparent;
            font-size: 13px;
            font-weight: 600;
            color: var(--text-secondary);
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.2s;
            text-align: center;
        }

        .tab-btn.active {
            background: white;
            color: var(--primary);
            box-shadow: 0 2px 6px rgba(0,0,0,0.08);
        }

        /* Step Progress */
        .steps-nav {
            display: flex;
            justify-content: space-between;
            margin-bottom: 16px;
            position: relative;
        }

        .step-indicator {
            flex: 1;
            text-align: center;
            font-size: 11px;
            font-weight: 600;
            color: var(--text-secondary);
            position: relative;
        }

        .step-indicator.active {
            color: var(--primary);
        }

        .step-circle {
            width: 26px;
            height: 26px;
            border-radius: 50%;
            background: #E0E0E0;
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 4px;
            font-size: 12px;
            font-weight: 700;
        }

        .step-indicator.active .step-circle {
            background: var(--primary);
        }

        .step-indicator.completed .step-circle {
            background: var(--success);
        }

        /* Step Sections */
        .step-section {
            display: none;
        }

        .step-section.active {
            display: block;
            animation: fadeIn 0.25s ease-out;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(6px); }
            to { opacity: 1; transform: translateY(0); }
        }

        /* Service Cards */
        .section-title {
            font-size: 15px;
            font-weight: 700;
            margin-bottom: 10px;
            color: var(--text-main);
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .search-box {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            font-size: 13px;
            margin-bottom: 12px;
            background: white;
            outline: none;
        }

        .search-box:focus {
            border-color: var(--primary);
        }

        .card-list {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .item-card {
            background: white;
            border: 1.5px solid var(--border);
            border-radius: var(--radius-md);
            padding: 14px;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: var(--shadow-card);
        }

        .item-card:hover, .item-card.selected {
            border-color: var(--primary);
            background: #FAF5FB;
        }

        .item-card.selected {
            box-shadow: 0 0 0 2px var(--primary);
        }

        .item-info h3 {
            font-size: 14px;
            font-weight: 700;
            color: var(--text-main);
            margin-bottom: 4px;
        }

        .item-info p {
            font-size: 12px;
            color: var(--text-secondary);
            margin-bottom: 6px;
        }

        .item-meta {
            display: flex;
            gap: 10px;
            font-size: 12px;
        }

        .badge-price {
            font-weight: 700;
            color: var(--primary-dark);
            background: #F3E5F5;
            padding: 2px 8px;
            border-radius: var(--radius-sm);
        }

        .badge-time {
            color: var(--text-secondary);
        }

        /* Professionals Cards */
        .prof-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
        }

        .prof-card {
            background: white;
            border: 1.5px solid var(--border);
            border-radius: var(--radius-md);
            padding: 14px;
            text-align: center;
            cursor: pointer;
            transition: all 0.2s;
            box-shadow: var(--shadow-card);
        }

        .prof-card.selected {
            border-color: var(--primary);
            background: #FAF5FB;
            box-shadow: 0 0 0 2px var(--primary);
        }

        .prof-avatar {
            width: 46px;
            height: 46px;
            background: #E1BEE7;
            color: var(--primary-dark);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            font-weight: 700;
            margin: 0 auto 8px;
        }

        .prof-name {
            font-size: 13px;
            font-weight: 700;
            margin-bottom: 2px;
        }

        .prof-role {
            font-size: 11px;
            color: var(--text-secondary);
        }

        /* Date & Time Picker */
        .date-input {
            width: 100%;
            padding: 12px;
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            font-size: 14px;
            background: white;
            margin-bottom: 14px;
            outline: none;
        }

        .times-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 8px;
        }

        .time-pill {
            background: white;
            border: 1px solid var(--border);
            border-radius: 8px;
            padding: 10px 4px;
            text-align: center;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.15s;
        }

        .time-pill.selected {
            background: var(--primary);
            color: white;
            border-color: var(--primary);
        }

        /* Form Inputs */
        .form-group {
            margin-bottom: 14px;
        }

        .form-label {
            display: block;
            font-size: 12px;
            font-weight: 700;
            margin-bottom: 6px;
            color: var(--text-main);
        }

        .form-input {
            width: 100%;
            padding: 12px 14px;
            border: 1.5px solid var(--border);
            border-radius: var(--radius-md);
            font-size: 14px;
            background: white;
            outline: none;
            box-sizing: border-box;
        }

        .form-input:focus {
            border-color: var(--primary);
        }

        /* Bottom Action Bar */
        .action-bar {
            position: fixed;
            bottom: 0;
            left: 0;
            right: 0;
            background: white;
            border-top: 1px solid var(--border);
            padding: 12px 16px;
            display: flex;
            gap: 10px;
            box-shadow: 0 -4px 15px rgba(0,0,0,0.06);
            max-width: 500px;
            margin: 0 auto;
            z-index: 100;
        }

        .btn-prev {
            flex: 1;
            padding: 12px;
            background: #ECEFF1;
            color: var(--text-main);
            border: none;
            border-radius: var(--radius-md);
            font-weight: 600;
            font-size: 14px;
            cursor: pointer;
        }

        .btn-next {
            flex: 2;
            padding: 12px;
            background: var(--primary);
            color: white;
            border: none;
            border-radius: var(--radius-md);
            font-weight: 700;
            font-size: 14px;
            cursor: pointer;
        }

        .btn-whatsapp-confirm {
            width: 100%;
            padding: 14px;
            background: #25D366;
            color: white;
            border: none;
            border-radius: var(--radius-md);
            font-size: 15px;
            font-weight: 700;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            cursor: pointer;
            box-shadow: 0 4px 12px rgba(37, 211, 102, 0.35);
        }

        /* Success Summary Box */
        .summary-card {
            background: white;
            border: 1px solid #E1BEE7;
            border-radius: var(--radius-md);
            padding: 16px;
            margin-bottom: 16px;
            box-shadow: var(--shadow-card);
        }

        .summary-item {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px dashed #EEEEEE;
            font-size: 13px;
        }

        .summary-item:last-child {
            border-bottom: none;
            padding-top: 12px;
            font-size: 15px;
            font-weight: 700;
            color: var(--primary-dark);
        }

        /* Appointments Tab */
        .appointment-card {
            background: white;
            border-radius: var(--radius-md);
            padding: 14px;
            margin-bottom: 10px;
            border-left: 4px solid var(--primary);
            box-shadow: var(--shadow-card);
        }

        .app-status {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
            background: var(--success-bg);
            color: var(--success);
            margin-bottom: 6px;
        }
    </style>
</head>
<body>

    <!-- Header -->
    <header>
        <div class="salon-avatar">💇‍♀️</div>
        <h1>${escapeHtml(salonName)}</h1>
        <div class="subtitle">
            <span>Portal do Cliente</span>
            <span class="badge-online">● Online</span>
        </div>
    </header>

    <div class="container">
        <!-- Direct App Deep Link Banner -->
        <div class="app-link-card">
            <div>
                <div class="app-link-text">📱 Tem o Aplicativo instalado?</div>
                <div style="font-size: 11px; color: #757575;">Abra direto no app com confirmação instantânea</div>
            </div>
            <a href="vaniraevanessa://portal?mode=client&client_only=true" class="btn-open-app">Abrir no App</a>
        </div>

        <!-- Navigation Tabs -->
        <div class="tab-bar">
            <button class="tab-btn active" onclick="switchMainTab('agendar')">✨ Novo Agendamento</button>
            <button class="tab-btn" onclick="switchMainTab('meus')">📅 Meus Horários</button>
        </div>

        <!-- TAB 1: Booking Wizard -->
        <div id="tab-agendar">
            <!-- Step Indicators -->
            <div class="steps-nav">
                <div class="step-indicator active" id="ind-1">
                    <div class="step-circle">1</div>
                    <span>Serviço</span>
                </div>
                <div class="step-indicator" id="ind-2">
                    <div class="step-circle">2</div>
                    <span>Profissional</span>
                </div>
                <div class="step-indicator" id="ind-3">
                    <div class="step-circle">3</div>
                    <span>Data & Hora</span>
                </div>
                <div class="step-indicator" id="ind-4">
                    <div class="step-circle">4</div>
                    <span>Confirmar</span>
                </div>
            </div>

            <!-- STEP 1: Select Service -->
            <div class="step-section active" id="step-1">
                <div class="section-title">✂️ Escolha o Serviço Desejado</div>
                <input type="text" class="search-box" id="service-search" placeholder="Buscar corte, escova, mechas, manicure..." oninput="filterServices()">
                <div class="card-list" id="services-list">
                    <!-- Populated by JS -->
                </div>
            </div>

            <!-- STEP 2: Select Professional -->
            <div class="step-section" id="step-2">
                <div class="section-title">✨ Escolha a Profissional</div>
                <div class="prof-grid" id="professionals-list">
                    <!-- Populated by JS -->
                </div>
            </div>

            <!-- STEP 3: Select Date & Time -->
            <div class="step-section" id="step-3">
                <div class="section-title">📅 Escolha a Data</div>
                <input type="date" class="date-input" id="booking-date" value="${todayFormatted}">

                <div class="section-title" style="margin-top: 14px;">⏰ Horários Disponíveis</div>
                <div class="times-grid" id="times-list">
                    <!-- Populated by JS -->
                </div>
            </div>

            <!-- STEP 4: Client Info & Confirmation -->
            <div class="step-section" id="step-4">
                <div class="section-title">👤 Seus Dados de Contato</div>
                <div class="form-group">
                    <label class="form-label">Seu Nome Completo:</label>
                    <input type="text" class="form-input" id="client-name" placeholder="Ex: Maria Silva" value="${escapeHtml(clientPreloadName)}">
                </div>
                <div class="form-group">
                    <label class="form-label">WhatsApp com DDD:</label>
                    <input type="tel" class="form-input" id="client-phone" placeholder="Ex: 11987654321" value="${escapeHtml(clientPreloadPhone)}">
                </div>
                <div class="form-group">
                    <label class="form-label">Observações (opcional):</label>
                    <input type="text" class="form-input" id="client-notes" placeholder="Ex: Cabelo com mechas, prefiro corte em camadas...">
                </div>

                <div class="section-title" style="margin-top: 16px;">📋 Resumo do Agendamento</div>
                <div class="summary-card" id="summary-content">
                    <!-- Populated by JS -->
                </div>

                <button class="btn-whatsapp-confirm" onclick="confirmBooking()">
                    <span>💬 Confirmar via WhatsApp do Salão</span>
                </button>
                <div style="font-size: 11px; color: #757575; text-align: center; margin-top: 8px;">
                    O agendamento é enviado instantaneamente para a equipe do salão para confirmação na agenda!
                </div>
            </div>

            <!-- Bottom Wizard Navigation -->
            <div class="action-bar" id="wizard-bar">
                <button class="btn-prev" id="btn-back" onclick="prevStep()" style="display: none;">Voltar</button>
                <button class="btn-next" id="btn-forward" onclick="nextStep()">Avançar</button>
            </div>
        </div>

        <!-- TAB 2: My Appointments -->
        <div id="tab-meus" style="display: none;">
            <div class="section-title">📅 Seus Agendamentos Salvos</div>
            <div id="my-appointments-list">
                <div style="text-align: center; padding: 30px 10px; color: #757575;">
                    <div style="font-size: 32px; margin-bottom: 8px;">💆‍♀️</div>
                    <p style="font-size: 13px;">Você ainda não possui agendamentos gravados neste navegador.</p>
                    <p style="font-size: 11px; margin-top: 4px;">Ao realizar um agendamento, ele ficará disponível aqui para consulta!</p>
                </div>
            </div>
        </div>
    </div>

    <!-- Embedded Application Logic -->
    <script>
        const SALON_NAME = "${escapeJs(salonName)}";
        const SALON_PHONE = "${escapeJs(salonPhone)}";
        const SERVICES = [
            $servicesJsArray
        ];
        const PROFESSIONALS = [
            $professionalsJsArray
        ];
        const DEFAULT_TIMES = ["09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00"];

        let currentStep = 1;
        let selectedService = SERVICES[0] || null;
        let selectedProfessional = PROFESSIONALS[0] || null;
        let selectedTime = "10:00";

        // Initialize App
        document.addEventListener('DOMContentLoaded', () => {
            renderServices(SERVICES);
            renderProfessionals();
            renderTimes();
            loadMyAppointments();

            // Set default date to today
            const today = new Date().toISOString().split('T')[0];
            const dateInput = document.getElementById('booking-date');
            if (dateInput) {
                dateInput.value = today;
                dateInput.min = today;
            }
        });

        function switchMainTab(tab) {
            const btns = document.querySelectorAll('.tab-btn');
            btns.forEach(b => b.classList.remove('active'));
            if (tab === 'agendar') {
                btns[0].classList.add('active');
                document.getElementById('tab-agendar').style.display = 'block';
                document.getElementById('tab-meus').style.display = 'none';
                document.getElementById('wizard-bar').style.display = 'flex';
            } else {
                btns[1].classList.add('active');
                document.getElementById('tab-agendar').style.display = 'none';
                document.getElementById('tab-meus').style.display = 'block';
                document.getElementById('wizard-bar').style.display = 'none';
                loadMyAppointments();
            }
        }

        function renderServices(list) {
            const container = document.getElementById('services-list');
            if (!container) return;
            if (list.length === 0) {
                container.innerHTML = '<div style="text-align:center; padding:20px; color:#757575;">Nenhum serviço encontrado.</div>';
                return;
            }
            container.innerHTML = list.map(s => `
                <div class="item-card ${'$'}{selectedService && selectedService.id === s.id ? 'selected' : ''}" onclick="selectService(${'$'}{s.id})">
                    <div class="item-info">
                        <h3>${'$'}{s.name}</h3>
                        <p>${'$'}{s.description || s.category}</p>
                        <div class="item-meta">
                            <span class="badge-price">${'$'}{s.priceFormatted}</span>
                            <span class="badge-time">⏱️ ${'$'}{s.duration} min</span>
                        </div>
                    </div>
                    <div style="font-size: 18px; color: #8E24AA;">${'$'}{selectedService && selectedService.id === s.id ? '✓' : '›'}</div>
                </div>
            `).join('');
        }

        function filterServices() {
            const query = document.getElementById('service-search').value.toLowerCase();
            const filtered = SERVICES.filter(s =>
                s.name.toLowerCase().includes(query) ||
                s.category.toLowerCase().includes(query) ||
                (s.description && s.description.toLowerCase().includes(query))
            );
            renderServices(filtered);
        }

        function selectService(id) {
            selectedService = SERVICES.find(s => s.id === id);
            renderServices(SERVICES);
        }

        function renderProfessionals() {
            const container = document.getElementById('professionals-list');
            if (!container) return;
            container.innerHTML = PROFESSIONALS.map(p => `
                <div class="prof-card ${'$'}{selectedProfessional && selectedProfessional.id === p.id ? 'selected' : ''}" onclick="selectProfessional(${'$'}{p.id})">
                    <div class="prof-avatar">${'$'}{p.name.charAt(0)}</div>
                    <div class="prof-name">${'$'}{p.name}</div>
                    <div class="prof-role">${'$'}{p.role || 'Especialista'}</div>
                </div>
            `).join('');
        }

        function selectProfessional(id) {
            selectedProfessional = PROFESSIONALS.find(p => p.id === id);
            renderProfessionals();
        }

        function renderTimes() {
            const container = document.getElementById('times-list');
            if (!container) return;
            container.innerHTML = DEFAULT_TIMES.map(t => `
                <div class="time-pill ${'$'}{selectedTime === t ? 'selected' : ''}" onclick="selectTime('${'$'}{t}')">${'$'}{t}</div>
            `).join('');
        }

        function selectTime(t) {
            selectedTime = t;
            renderTimes();
        }

        function updateStepIndicators() {
            for (let i = 1; i <= 4; i++) {
                const ind = document.getElementById(`ind-${'$'}{i}`);
                if (ind) {
                    ind.classList.remove('active', 'completed');
                    if (i === currentStep) ind.classList.add('active');
                    else if (i < currentStep) ind.classList.add('completed');
                }
            }
        }

        function nextStep() {
            if (currentStep === 1 && !selectedService) {
                alert('Por favor, selecione um serviço.');
                return;
            }
            if (currentStep === 2 && !selectedProfessional) {
                alert('Por favor, selecione uma profissional.');
                return;
            }
            if (currentStep === 3 && !selectedTime) {
                alert('Por favor, escolha um horário disponível.');
                return;
            }

            if (currentStep < 4) {
                document.getElementById(`step-${'$'}{currentStep}`).classList.remove('active');
                currentStep++;
                document.getElementById(`step-${'$'}{currentStep}`).classList.add('active');
                updateStepIndicators();

                document.getElementById('btn-back').style.display = 'block';

                if (currentStep === 4) {
                    document.getElementById('btn-forward').style.display = 'none';
                    renderSummary();
                }
            }
        }

        function prevStep() {
            if (currentStep > 1) {
                document.getElementById(`step-${'$'}{currentStep}`).classList.remove('active');
                currentStep--;
                document.getElementById(`step-${'$'}{currentStep}`).classList.add('active');
                updateStepIndicators();

                if (currentStep === 1) {
                    document.getElementById('btn-back').style.display = 'none';
                }
                document.getElementById('btn-forward').style.display = 'block';
            }
        }

        function renderSummary() {
            const summary = document.getElementById('summary-content');
            const dateVal = document.getElementById('booking-date').value;
            let formattedDate = dateVal;
            if (dateVal.includes('-')) {
                const parts = dateVal.split('-');
                formattedDate = `${'$'}{parts[2]}/${'$'}{parts[1]}/${'$'}{parts[0]}`;
            }

            summary.innerHTML = `
                <div class="summary-item">
                    <span style="color:#757575;">Serviço:</span>
                    <strong>${'$'}{selectedService ? selectedService.name : '-'}</strong>
                </div>
                <div class="summary-item">
                    <span style="color:#757575;">Profissional:</span>
                    <strong>${'$'}{selectedProfessional ? selectedProfessional.name : '-'}</strong>
                </div>
                <div class="summary-item">
                    <span style="color:#757575;">Data:</span>
                    <strong>${'$'}{formattedDate}</strong>
                </div>
                <div class="summary-item">
                    <span style="color:#757575;">Horário:</span>
                    <strong>${'$'}{selectedTime}</strong>
                </div>
                <div class="summary-item">
                    <span style="color:#757575;">Duração Estimada:</span>
                    <span>${'$'}{selectedService ? selectedService.duration : 30} min</span>
                </div>
                <div class="summary-item">
                    <span>Valor Total:</span>
                    <span>${'$'}{selectedService ? selectedService.priceFormatted : 'R$ 0,00'}</span>
                </div>
            `;
        }

        function confirmBooking() {
            const name = document.getElementById('client-name').value.trim();
            const phone = document.getElementById('client-phone').value.trim();
            const notes = document.getElementById('client-notes').value.trim();
            const dateVal = document.getElementById('booking-date').value;

            if (!name) {
                alert('Por favor, informe seu nome.');
                document.getElementById('client-name').focus();
                return;
            }
            if (!phone) {
                alert('Por favor, informe seu número de WhatsApp com DDD.');
                document.getElementById('client-phone').focus();
                return;
            }

            let formattedDate = dateVal;
            if (dateVal.includes('-')) {
                const parts = dateVal.split('-');
                formattedDate = `${'$'}{parts[2]}/${'$'}{parts[1]}/${'$'}{parts[0]}`;
            }

            // Save to LocalStorage
            saveAppointmentLocally({
                id: Date.now(),
                clientName: name,
                clientPhone: phone,
                serviceName: selectedService.name,
                servicePrice: selectedService.priceFormatted,
                professionalName: selectedProfessional.name,
                date: formattedDate,
                time: selectedTime,
                notes: notes,
                timestamp: Date.now()
            });

            // Build WhatsApp Message
            const msg = `Olá! Gostaria de agendar pelo *Portal Web ${'$'}{SALON_NAME}*:\n\n` +
                `👤 *Cliente:* ${'$'}{name}\n` +
                `📱 *WhatsApp:* ${'$'}{phone}\n` +
                `✂️ *Serviço:* ${'$'}{selectedService.name} (${'$'}{selectedService.priceFormatted})\n` +
                `💇‍♀️ *Profissional:* ${'$'}{selectedProfessional.name}\n` +
                `📅 *Data:* ${'$'}{formattedDate} às ${'$'}{selectedTime}\n` +
                (notes ? `📝 *Observações:* ${'$'}{notes}\n` : '') +
                `\nFavor confirmar meu horário na agenda! ✨`;

            const encoded = encodeURIComponent(msg);
            const targetPhone = SALON_PHONE.replace(/\D/g, '');
            const waUrl = targetPhone ? `https://wa.me/${'$'}{targetPhone}?text=${'$'}{encoded}` : `https://api.whatsapp.com/send?text=${'$'}{encoded}`;

            window.open(waUrl, '_blank');

            alert('✨ Agendamento enviado! O WhatsApp será aberto para confirmação com a equipe do salão.');
            switchMainTab('meus');
        }

        function saveAppointmentLocally(app) {
            try {
                let saved = JSON.parse(localStorage.getItem('my_salon_bookings') || '[]');
                saved.unshift(app);
                localStorage.setItem('my_salon_bookings', JSON.stringify(saved.slice(0, 20)));
            } catch (e) {
                console.error('LocalStorage error', e);
            }
        }

        function loadMyAppointments() {
            const container = document.getElementById('my-appointments-list');
            if (!container) return;
            try {
                const saved = JSON.parse(localStorage.getItem('my_salon_bookings') || '[]');
                if (saved.length === 0) {
                    container.innerHTML = `
                        <div style="text-align: center; padding: 30px 10px; color: #757575;">
                            <div style="font-size: 32px; margin-bottom: 8px;">💆‍♀️</div>
                            <p style="font-size: 13px;">Você ainda não possui agendamentos gravados neste navegador.</p>
                            <p style="font-size: 11px; margin-top: 4px;">Ao realizar um agendamento, ele ficará disponível aqui para consulta!</p>
                        </div>
                    `;
                    return;
                }
                container.innerHTML = saved.map(a => `
                    <div class="appointment-card">
                        <div class="app-status">CONFIRMADO / PENDENTE</div>
                        <h3 style="font-size:14px; font-weight:700; color:var(--text-main);">${'$'}{a.serviceName}</h3>
                        <p style="font-size:12px; color:var(--text-secondary); margin: 4px 0;">💇‍♀️ Com ${'$'}{a.professionalName} • ${'$'}{a.servicePrice}</p>
                        <p style="font-size:12px; font-weight:600; color:var(--primary-dark);">📅 ${'$'}{a.date} às ${'$'}{a.time}</p>
                        <p style="font-size:11px; color:#9E9E9E; margin-top:4px;">Cliente: ${'$'}{a.clientName} (${'$'}{a.clientPhone})</p>
                    </div>
                `).join('');
            } catch (e) {
                console.error(e);
            }
        }
    </script>
</body>
</html>
        """.trimIndent()
    }

    /**
     * Saves the Web App HTML to internal storage.
     */
    fun saveWebAppFile(htmlContent: String): File {
        val webAppDir = File(context.filesDir, "webapp").apply { if (!exists()) mkdirs() }
        val file = File(webAppDir, "portal_cliente_vanira_vanessa.html")
        file.writeText(htmlContent, Charsets.UTF_8)
        return file
    }

    /**
     * Shares the Web App HTML file as a document via WhatsApp or system share sheet.
     */
    fun shareWebAppFile(file: File, salonName: String, clientPhone: String = "", clientName: String = "") {
        try {
            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val greeting = if (clientName.isNotBlank()) "Olá, ${clientName.trim()}! ✨" else "Olá! ✨"
            val shareText = """
$greeting

Aqui é do *$salonName*! 💇‍♀️💅

Segue seu *Web App do Portal do Cliente* em anexo!
Basta abrir o arquivo no seu navegador (Chrome ou Safari) para agendar online e acompanhar seus horários em tempo real, sem precisar instalar nada!

✨ Atualizado com nossos serviços, valores e profissionais!
Te esperamos com todo carinho! 💕
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Web App Portal do Cliente - $salonName")
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // If phone provided, try WhatsApp package directly or fallback
            if (clientPhone.isNotBlank()) {
                val cleanPhone = clientPhone.replace("\\D".toRegex(), "")
                val targetPhone = if (cleanPhone.startsWith("55")) cleanPhone else "55$cleanPhone"
                intent.putExtra("jid", "$targetPhone@s.whatsapp.net")
            }

            val chooser = Intent.createChooser(intent, "Enviar Web App do Cliente").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao compartilhar Web App: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Opens the generated Web App directly in the device's browser for testing.
     */
    fun openWebAppInBrowser(file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "text/html")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao abrir Web App no navegador: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun escapeJs(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }
}
