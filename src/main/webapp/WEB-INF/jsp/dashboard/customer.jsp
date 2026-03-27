<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.cmpe172.salon.dto.AppointmentDto" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Stylist" %>
<%@ page import="edu.sjsu.cmpe172.salon.dto.StylistDto" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Service" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.AppointmentStatus" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="org.springframework.security.web.csrf.CsrfToken" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Customer Dashboard</title>
</head>

<style>
    .dashboard-wrapper {
        display: flex;
        flex-direction: column;
        height: calc(100vh - 56px);
        overflow: hidden;
        background-color: var(--bs-body-bg);
    }

    .dashboard-header {
        flex-shrink: 0;
        background-color: var(--bs-body-bg);
    }

    .dashboard-content {
        flex-grow: 1;
        overflow-y: auto;
        padding-bottom: 3rem;
        background-color: var(--bs-tertiary-bg);
    }

    .tabs-wrapper {
        background-color: var(--bs-tertiary-bg) !important;
        padding-top: 1rem;
    }

    .tabs-inner-container {
        border-bottom: 1px solid var(--bs-border-color);
    }

    .booking-step {
        display: none;
    }

    .booking-step.active {
        display: block;
    }
</style>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <div class="dashboard-wrapper">
        <header class="dashboard-header">
            <div class="page-header mb-0">
                <div class="container page-header__container">
                    <h1 class="h2 page-header__title">Customer Dashboard</h1>
                    <p class="page-header__subtitle text-muted mb-3">Manage your appointments or book a new session.</p>
                </div>
            </div>

            <div class="tabs-wrapper">
                <div class="container">
                    <div class="tabs-inner-container d-flex justify-content-between align-items-center">
                        <ul class="nav nav-tabs border-bottom-0">
                            <li class="nav-item">
                                <a class="nav-link active" href="#" data-tab="upcoming">Upcoming</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#" data-tab="history">History</a>
                            </li>
                        </ul>
                        <button class="btn btn-primary btn-sm mb-2 px-3" data-tab="book-now">
                            <i class="bi bi-plus-lg me-1"></i> Book Now
                        </button>
                    </div>
                </div>
            </div>
        </header>

        <main class="dashboard-content" id="dashboard-scroll-area">
            <div class="container py-4">
                <%
                    String successMessage = (String) request.getAttribute("successMessage");
                    String errorMessage = (String) request.getAttribute("errorMessage");
                    DateTimeFormatter slotFormatter = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a");

                    if (successMessage != null) {
                %>
                <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                    <%= successMessage %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <%
                    }
                    if (errorMessage != null) {
                %>
                <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                    <%= errorMessage %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <%
                    }

                    List<AppointmentDto> appointments = (List<AppointmentDto>) request.getAttribute("appointments");
                    List<AppointmentDto> upcoming = new ArrayList<>();
                    List<AppointmentDto> history = new ArrayList<>();
                    
                    if (appointments != null) {
                        for (AppointmentDto apt : appointments) {
                            if (apt.getStatus() == AppointmentStatus.Booked) {
                                upcoming.add(apt);
                            } else {
                                history.add(apt);
                            }
                        }
                    }
                %>

                <div id="upcoming-tab-content" data-tab="upcoming">
                    <div class="card border-0 mb-5">
                        <div class="card-header bg-white border-bottom py-3">
                            <h2 class="h5 mb-0 fw-bold">Your Upcoming Appointments</h2>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th class="ps-4">ID</th>
                                            <th>Service</th>
                                            <th>Stylist</th>
                                            <th>Slot</th>
                                            <th class="pe-4 text-end">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            if (!upcoming.isEmpty()) {
                                                for (AppointmentDto apt : upcoming) {
                                                    String serviceName = apt.getServiceName();
                                                    if (serviceName == null || serviceName.isBlank()) {
                                                        serviceName = "Service #" + apt.getServiceId();
                                                    }
                                        %>
                                            <tr>
                                                <td class="ps-4"><strong>#<%= apt.getId() %></strong></td>
                                                <td><span class="badge bg-primary text-white"><%= serviceName %></span></td>
                                                <td>
                                                    <div class="fw-semibold">
                                                    <% if (apt.getStylistName() != null && !apt.getStylistName().isBlank()) { %>
                                                        <%= apt.getStylistName() %>
                                                    <% } else { %>
                                                        Stylist ID <%= apt.getStylistUserId() %>
                                                    <% } %>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="small">
                                                    <% if (apt.getSlotStartDateTime() != null && apt.getSlotEndDateTime() != null) { %>
                                                        <%= apt.getSlotStartDateTime().format(slotFormatter) %>
                                                    <% } else { %>
                                                        Slot <%= apt.getAvailabilitySlotId() %>
                                                    <% } %>
                                                    </div>
                                                </td>
                                                <td class="pe-4 text-end">
                                                    <button class="btn btn-sm btn-outline-danger" onclick="cancelAppointment(<%= apt.getId() %>)">
                                                       Cancel
                                                    </button>
                                                </td>
                                            </tr>
                                        <%
                                                }
                                            } else {
                                        %>
                                            <tr>
                                                <td colspan="5" class="text-center py-5">
                                                    <p class="text-muted mb-2">You have no upcoming appointments.</p>
                                                    <button class="btn btn-link px-0" onclick="showBookingFlow()">Book one today</button>
                                                </td>
                                            </tr>
                                        <%
                                            }
                                        %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <div id="history-tab-content" class="hidden" data-tab="history">
                    <div class="card border-0 mb-5">
                        <div class="card-header bg-white border-bottom py-3">
                            <h2 class="h5 mb-0 fw-bold">Appointment History</h2>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th class="ps-4">ID</th>
                                            <th>Service</th>
                                            <th>Stylist</th>
                                            <th>Date</th>
                                            <th class="pe-4 text-end">Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            if (!history.isEmpty()) {
                                                for (AppointmentDto apt : history) {
                                                    String serviceName = apt.getServiceName();
                                                    if (serviceName == null || serviceName.isBlank()) {
                                                        serviceName = "Service #" + apt.getServiceId();
                                                    }
                                                    String statusBadgeClass = apt.getStatus() == AppointmentStatus.Complete ? "bg-success" : "bg-secondary opacity-75";
                                        %>
                                            <tr>
                                                <td class="ps-4"><strong>#<%= apt.getId() %></strong></td>
                                                <td><span class="badge bg-primary text-white"><%= serviceName %></span></td>
                                                <td><div class="fw-medium"><%= apt.getStylistName() %></div></td>
                                                <td><div class="small text-muted"><%= apt.getSlotStartDateTime().format(slotFormatter) %></div></td>
                                                <td class="pe-4 text-end">
                                                    <span class="badge <%= statusBadgeClass %> text-white"><%= apt.getStatus().toString() %></span>
                                                </td>
                                            </tr>
                                        <%
                                                }
                                            } else {
                                        %>
                                            <tr>
                                                <td colspan="5" class="text-center py-5 text-muted small">
                                                    No past appointments found.
                                                </td>
                                            </tr>
                                        <%
                                            }
                                        %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <div id="book-now-tab-content" class="hidden" data-tab="book-now">
                    <div class="card border-0 mb-5">
                        <div class="card-header bg-white border-bottom py-3">
                            <h2 class="h5 mb-1 fw-bold">Book an Appointment</h2>
                            <p class="text-muted small mb-0" id="stepIndicator">Step 1: Choose your service and stylist</p>
                        </div>
                        <div class="card-body p-4 p-md-5">
                            <form id="bookingForm" method="post" action="/appointments">
                                <%
                                    CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
                                    if (csrfToken != null) {
                                %>
                                    <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                                <% } %>
                                <input type="hidden" id="selectedSlotId" name="availabilitySlotId" value="">

                                <!-- Step 1: Service & Stylist -->
                                <div id="step1" class="booking-step active">
                                    <div class="row g-4">
                                        <div class="col-md-6">
                                            <label for="service" class="form-label fw-semibold">Which service would you like?</label>
                                            <select class="form-select form-select-lg" id="service" name="serviceId" required>
                                                <option selected disabled value="">Choose a service...</option>
                                                <%
                                                    List<Service> services = (List<Service>) request.getAttribute("services");
                                                    if (services != null) {
                                                        for (Service s : services) {
                                                %>
                                                    <option value="<%= s.getId() %>" data-duration-minutes="<%= s.getDurationMinutes() %>"><%= s.getName() %> (<%= s.getDurationMinutes() %> min)</option>
                                                <%
                                                        }
                                                    }
                                                %>
                                            </select>
                                        </div>
                                        <div class="col-md-6">
                                            <label for="stylist" class="form-label fw-semibold">Select your preferred stylist</label>
                                            <select class="form-select form-select-lg" id="stylist" name="stylistUserId" required>
                                                <option selected disabled value="">Choose a stylist...</option>
                                                <%
                                                    List<StylistDto> stylists = (List<StylistDto>) request.getAttribute("stylists");
                                                    if (stylists != null) {
                                                        for (StylistDto s : stylists) {
                                                %>
                                                    <option value="<%= s.getId() %>" data-service-id="<%= s.getServiceId() %>"><%= s.getFirstName() %> <%= s.getLastName() %> - <%= s.getServiceName() %></option>
                                                <%
                                                        }
                                                    }
                                                %>
                                            </select>
                                            <div id="noStylistsHint" class="alert alert-warning d-none mt-2">
                                                No stylists are currently available for this service.
                                            </div>
                                        </div>
                                    </div>

                                    <div class="d-flex justify-content-end gap-2 mt-5">
                                        <button type="button" id="nextToStep2" class="btn btn-primary px-4" onclick="loadAndShowSlots()" disabled>Next Step</button>
                                    </div>
                                </div>

                                <!-- Step 2: Slot Selection -->
                                <div id="step2" class="booking-step">
                                    <div class="mb-4 text-muted small d-flex flex-wrap align-items-center gap-2">
                                        <span>Booking: <strong class="text-dark" id="selectedServiceSummaryText">--</strong></span>
                                        <span class="text-secondary opacity-50">•</span>
                                        <span>With: <strong class="text-dark" id="selectedStylistSummaryText">--</strong></span>
                                        <a href="javascript:void(0)" class="ms-md-auto text-primary text-decoration-none fw-semibold" onclick="goToStep(1)">
                                            <i class="bi bi-pencil-square me-1"></i>Change
                                        </a>
                                    </div>

                                    <h4 class="h5 mb-4">Available time slots</h4>
                                    <div id="slotContainer" class="row g-3 mb-4"></div>
                                    <div id="slotEmptyState" class="alert alert-warning d-none">No available slots for this stylist right now.</div>

                                    <div class="alert alert-info mt-5 border-0">
                                        <div class="d-flex align-items-center">
                                            <div class="me-3 fs-3 text-primary"><i class="bi bi-clock-history"></i></div>
                                            <div>
                                                <h5 class="mb-0 h6 fw-bold">Selected Time</h5>
                                                <p class="mb-1 text-muted" id="selectedTimeSummary">Not selected</p>
                                                <p class="mb-0 text-muted small" id="selectedDurationSummary"></p>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="d-flex justify-content-between gap-2 mt-5">
                                        <button type="button" class="btn btn-outline-secondary px-4" onclick="goToStep(1)">Back</button>
                                        <button type="submit" id="confirmBooking" class="btn btn-primary px-5" disabled>Confirm Booking</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', async () => {
            const tabLinks = document.querySelectorAll('[data-tab]:not(div)');
            const tabContents = document.querySelectorAll('div[data-tab]');
            const scrollArea = document.getElementById('dashboard-scroll-area');

            function switchTab(targetTab) {
                tabLinks.forEach(l => {
                    if (l.getAttribute('data-tab') === targetTab) {
                        l.classList.add('active');
                        if (l.tagName === 'BUTTON') {
                            l.classList.replace('btn-primary', 'btn-dark');
                        }
                    } else {
                        l.classList.remove('active');
                        if (l.tagName === 'BUTTON') {
                            l.classList.replace('btn-dark', 'btn-primary');
                        }
                    }
                });

                tabContents.forEach(content => {
                    if (content.getAttribute('data-tab') === targetTab) {
                        content.classList.remove('hidden');
                    } else {
                        content.classList.add('hidden');
                    }
                });

                // Update URL without reloading
                const url = new URL(window.location.href);
                url.searchParams.set('tab', targetTab);
                // Also clear 'new' parameter if explicitly switching tabs
                if (url.searchParams.has('new')) {
                    url.searchParams.delete('new');
                }
                window.history.pushState({path: url.href}, '', url.href);
                
                scrollArea.scrollTop = 0;
            }

            tabLinks.forEach(link => {
                link.addEventListener('click', (ev) => {
                    ev.preventDefault();
                    const targetTab = link.getAttribute('data-tab');
                    switchTab(targetTab);
                });
            });

            // Initial Tab setup
            const urlParams = new URLSearchParams(window.location.search);
            const initialTab = urlParams.get('tab') || (urlParams.get('new') === 'true' ? 'book-now' : 'upcoming');
            switchTab(initialTab);

            filterStylists();
        });

        const slotContainer = document.getElementById('slotContainer');
        const slotEmptyState = document.getElementById('slotEmptyState');
        const stylistSelect = document.getElementById('stylist');
        const serviceSelect = document.getElementById('service');
        const nextButton = document.getElementById('nextToStep2');
        const confirmButton = document.getElementById('confirmBooking');

        function showBookingFlow() {
            const bookButton = document.querySelector('[data-tab="book-now"]');
            if (bookButton) {
                bookButton.click();
            }
            goToStep(1);
        }

        function goToStep(stepNum) {
            document.querySelectorAll('.booking-step').forEach(s => s.classList.remove('active'));
            document.getElementById('step' + stepNum).classList.add('active');

            const indicator = document.getElementById('stepIndicator');
            if (stepNum === 1) {
                indicator.innerText = "Step 1: Choose your service and stylist";
            } else {
                indicator.innerText = "Step 2: Choose an available time slot";
            }
            document.getElementById('dashboard-scroll-area').scrollTop = 0;
        }

        function selectSlot(slot, el) {
            document.querySelectorAll('.booking__slot').forEach(s => s.classList.remove('booking__slot--selected'));
            el.classList.add('booking__slot--selected');

            document.getElementById('selectedSlotId').value = slot.id;
            document.getElementById('selectedTimeSummary').innerText = slot.rangeLabel;
            document.getElementById('selectedDurationSummary').innerText = 'Duration: ' + slot.durationMinutes + ' minutes';
            confirmButton.disabled = false;
        }

        function resetSlotSelection() {
            document.querySelectorAll('.booking__slot').forEach(s => s.classList.remove('booking__slot--selected'));
            confirmButton.disabled = true;
            document.getElementById('selectedSlotId').value = '';
            document.getElementById('selectedTimeSummary').innerText = 'Not selected';
            document.getElementById('selectedDurationSummary').innerText = '';
            if (slotContainer) {
                slotContainer.innerHTML = '';
            }
            if (slotEmptyState) {
                slotEmptyState.classList.add('d-none');
            }
        }

        function validateStepOne() {
            nextButton.disabled = !serviceSelect.value || !stylistSelect.value;
        }

        function filterStylists() {
            const selectedServiceId = serviceSelect.value;
            const currentStylistId = stylistSelect.value;
            let currentStylistStillValid = false;
            let matchCount = 0;

            Array.from(stylistSelect.options).forEach(option => {
                if (!option.value) return;

                const stylistServiceId = option.getAttribute('data-service-id');
                if (!selectedServiceId || stylistServiceId === selectedServiceId) {
                    option.style.display = '';
                    option.disabled = false;
                    matchCount++;
                    if (option.value === currentStylistId) {
                        currentStylistStillValid = true;
                    }
                } else {
                    option.style.display = 'none';
                    option.disabled = true;
                }
            });

            const hint = document.getElementById('noStylistsHint');
            if (selectedServiceId && matchCount === 0) {
                hint.classList.remove('d-none');
                stylistSelect.disabled = true;
            } else {
                hint.classList.add('d-none');
                stylistSelect.disabled = false;
            }

            if (currentStylistId && !currentStylistStillValid) {
                stylistSelect.value = '';
                validateStepOne();
            }
        }

        function loadAndShowSlots() {
            const stylistId = stylistSelect.value;
            if (!stylistId) return;

            const selectedStylistOption = stylistSelect.options[stylistSelect.selectedIndex];
            const selectedServiceOption = serviceSelect.options[serviceSelect.selectedIndex];

            const serviceName = selectedServiceOption.text.split(' (')[0];
            const stylistName = selectedStylistOption.text.split(' - ')[0];
            document.getElementById('selectedServiceSummaryText').innerText = serviceName;
            document.getElementById('selectedStylistSummaryText').innerText = stylistName;

            const serviceId = serviceSelect.value;
            fetch('/customer/stylists/' + stylistId + '/available-slots?serviceId=' + encodeURIComponent(serviceId))
                .then(response => {
                    if (!response.ok) throw new Error('Failed to load available slots.');
                    return response.json();
                })
                .then(slots => {
                    slotContainer.innerHTML = '';
                    resetSlotSelection();

                    if (!slots.length) {
                        slotEmptyState.innerText = 'No available slots for this stylist right now.';
                        slotEmptyState.classList.remove('d-none');
                    } else {
                        slotEmptyState.classList.add('d-none');
                    }

                    slots.forEach(slot => {
                        const col = document.createElement('div');
                        col.className = 'col-md-3 col-6';
                        const button = document.createElement('button');
                        button.type = 'button';
                        button.className = 'booking__slot py-3 text-center rounded bg-light w-100 border-0';
                        button.innerHTML = '<div class="fw-bold">' + slot.startLabel + '</div>' +
                            '<div class="small text-muted">Ends ' + slot.endLabel + '</div>';
                        button.addEventListener('click', () => {
                            selectSlot(slot, button);
                        });
                        col.appendChild(button);
                        slotContainer.appendChild(col);
                    });

                    goToStep(2);
                })
                .catch(error => {
                    slotContainer.innerHTML = '';
                    slotEmptyState.classList.remove('d-none');
                    slotEmptyState.innerText = error.message;
                    goToStep(2);
                });
        }

        async function cancelAppointment(id) {
            if (!confirm('Are you sure you want to cancel this appointment? This action cannot be undone.')) return;

            const formData = new FormData();
            const csrfToken = document.querySelector('input[name="_csrf"]')?.value;
            if (csrfToken) formData.append('_csrf', csrfToken);

            try {
                const response = await fetch('/appointments/' + id + '/cancel', {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    if (response.redirected) {
                        window.location.href = response.url;
                    } else {
                        window.location.reload();
                    }
                } else {
                    const message = await response.text();
                    alert('Failed to cancel appointment: ' + (message || 'Unknown error'));
                }
            } catch (error) {
                console.error('Error cancelling appointment:', error);
                alert('An error occurred while cancelling the appointment.');
            }
        }

        serviceSelect.addEventListener('change', () => {
            filterStylists();
            validateStepOne();
            resetSlotSelection();
        });
        stylistSelect.addEventListener('change', () => {
            validateStepOne();
            resetSlotSelection();
        });
    </script>
</body>
</html>
