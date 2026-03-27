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

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <main class="container py-5">
        <%
            String successMessage = (String) request.getAttribute("successMessage");
            String errorMessage = (String) request.getAttribute("errorMessage");
            DateTimeFormatter slotFormatter = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a");
        %>
        <% if (successMessage != null) { %>
            <div class="alert alert-success" role="alert"><%= successMessage %></div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="alert alert-danger" role="alert"><%= errorMessage %></div>
        <% } %>
        <div id="dashboardView">
            <div class="row g-4">
                <div class="col-12">
                    <div class="card booking__card border-0">
                        <div class="card-body p-4 p-md-5">
                            <div class="d-flex justify-content-between align-items-center mb-4">
                                <div>
                                    <h1 class="h3 mb-1">Welcome Back!</h1>
                                    <p class="text-muted mb-0">Manage your appointments or book a new session.</p>
                                </div>
                                <button class="btn btn-primary" onclick="showBookingFlow()">Book New Appointment</button>
                            </div>

                            <div class="mt-5">
                                <h2 class="h5 mb-4">Your Upcoming Appointments</h2>
                                <div class="table-responsive bg-white rounded overflow-hidden">
                                    <table class="table table-hover align-middle mb-0">
                                        <thead class="table-light">
                                            <tr>
                                                <th>ID</th>
                                                <th>Service</th>
                                                <th>Stylist</th>
                                                <th>Slot</th>
                                                <th class="text-end">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <%
                                                List<AppointmentDto> appointments = (List<AppointmentDto>) request.getAttribute("appointments");
                                                List<AppointmentDto> upcoming = new ArrayList<>();
                                                List<AppointmentDto> history = new ArrayList<>();
                                                
                                                if (appointments != null) {
                                                    for (AppointmentDto apt : appointments) {
                                                        if (apt.getStatus() == AppointmentStatus.Booked || apt.getStatus() == AppointmentStatus.Pending) {
                                                            upcoming.add(apt);
                                                        } else {
                                                            history.add(apt);
                                                        }
                                                    }
                                                }

                                                if (!upcoming.isEmpty()) {
                                                    for (AppointmentDto apt : upcoming) {
                                                        String serviceName = apt.getServiceName();
                                                        if (serviceName == null || serviceName.isBlank()) {
                                                            serviceName = "Service #" + apt.getServiceId();
                                                        }
                                            %>
                                                <tr>
                                                    <td><strong>#<%= apt.getId() %></strong></td>
                                                    <td><span class="badge bg-primary text-white"><%= serviceName %></span></td>
                                                    <td>
                                                        <% if (apt.getStylistName() != null && !apt.getStylistName().isBlank()) { %>
                                                            <%= apt.getStylistName() %>
                                                        <% } else { %>
                                                            Stylist ID <%= apt.getStylistUserId() %>
                                                        <% } %>
                                                    </td>
                                                    <td>
                                                        <% if (apt.getSlotStartDateTime() != null && apt.getSlotEndDateTime() != null) { %>
                                                            <%= apt.getSlotStartDateTime().format(slotFormatter) %>
                                                        <% } else { %>
                                                            Slot <%= apt.getAvailabilitySlotId() %>
                                                        <% } %>
                                                    </td>
                                                    <td class="text-end">
                                                        <button class="btn btn-sm btn-outline-danger" onclick="cancelAppointment(<%= apt.getId() %>)">
                                                            <i class="bi bi-x-circle me-1"></i>Cancel
                                                        </button>
                                                    </td>
                                                </tr>
                                            <%
                                                    }
                                                } else {
                                            %>
                                                <tr>
                                                    <td colspan="5" class="text-center py-5 text-muted">
                                                        <p class="mb-0">You have no upcoming appointments.</p>
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

                            <div class="mt-5 pt-4">
                                <h2 class="h5 mb-4">Appointment History</h2>
                                <div class="table-responsive bg-white rounded overflow-hidden">
                                    <table class="table table-hover align-middle mb-0">
                                        <thead class="table-light">
                                            <tr>
                                                <th>ID</th>
                                                <th>Service</th>
                                                <th>Stylist</th>
                                                <th>Date</th>
                                                <th class="text-end">Status</th>
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
                                                        String statusClass = apt.getStatus() == AppointmentStatus.Complete ? "bg-success" : "bg-secondary opacity-75";
                                            %>
                                                <tr>
                                                    <td><strong>#<%= apt.getId() %></strong></td>
                                                    <td><span class="badge bg-light text-dark border"><%= serviceName %></span></td>
                                                    <td><%= apt.getStylistName() %></td>
                                                    <td><%= apt.getSlotStartDateTime().format(slotFormatter) %></td>
                                                    <td class="text-end">
                                                        <span class="badge <%= statusClass %> text-white"><%= apt.getStatus().toString() %></span>
                                                    </td>
                                                </tr>
                                            <%
                                                    }
                                                } else {
                                            %>
                                                <tr>
                                                    <td colspan="5" class="text-center py-4 text-muted small">
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
                </div>
            </div>
        </div>

        <div id="bookingView" style="display: none;">
            <div class="row justify-content-center">
                <div class="col-lg-10">
                        <div class="card-body p-4 p-md-5">
                            <div class="d-flex justify-content-between align-items-center mb-5 pb-4">
                                <div>
                                    <h2 class="h3 mb-1">Book an Appointment</h2>
                                    <p class="text-muted mb-0" id="stepIndicator">Step 1: Choose your service and stylist</p>
                                </div>
                                <button class="btn-close" onclick="cancelBooking()" aria-label="Close"></button>
                            </div>

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
                                        <button type="button" class="btn btn-secondary" onclick="cancelBooking()">Cancel</button>
                                        <button type="button" id="nextToStep2" class="btn btn-primary" onclick="loadAndShowSlots()" disabled>Next Step</button>
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

                                    <div class="alert alert-info mt-5">
                                        <div class="d-flex align-items-center">
                                            <div class="me-3 fs-3"><i class="bi bi-clock"></i></div>
                                            <div>
                                                <h5 class="mb-0 h6">Selected Time</h5>
                                                <p class="mb-1 text-muted" id="selectedTimeSummary">Not selected</p>
                                                <p class="mb-0 text-muted small" id="selectedDurationSummary"></p>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="d-flex justify-content-between gap-2 mt-5">
                                        <button type="button" class="btn btn-secondary" onclick="goToStep(1)">Back</button>
                                        <button type="submit" id="confirmBooking" class="btn btn-primary px-5" disabled>Confirm Booking</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <script>
        document.addEventListener('DOMContentLoaded', async () => {
            const url = new URL(window.location.href);
            // ?new=true to automatically show the booking flow (e.g. coming from the home page)
            if (url.searchParams?.get('new') === 'true') {
                showBookingFlow();
            }
            filterStylists();
        });

        const slotContainer = document.getElementById('slotContainer');
        const slotEmptyState = document.getElementById('slotEmptyState');
        const stylistSelect = document.getElementById('stylist');
        const serviceSelect = document.getElementById('service');
        const nextButton = document.getElementById('nextToStep2');
        const confirmButton = document.getElementById('confirmBooking');

        function showBookingFlow() {
            document.getElementById('dashboardView').style.display = 'none';
            document.getElementById('bookingView').style.display = 'block';
            goToStep(1);
        }

        function cancelBooking() {
            document.getElementById('bookingView').style.display = 'none';
            document.getElementById('dashboardView').style.display = 'block';
            resetForm();
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
        }

        function selectSlot(slot, el) {
            document.querySelectorAll('.booking__slot').forEach(s => s.classList.remove('booking__slot--selected'));
            el.classList.add('booking__slot--selected');

            document.getElementById('selectedSlotId').value = slot.id;
            document.getElementById('selectedTimeSummary').innerText = slot.rangeLabel;
            document.getElementById('selectedDurationSummary').innerText = 'Duration: ' + slot.durationMinutes + ' minutes';
            confirmButton.disabled = false;
        }

        function resetForm() {
            document.getElementById('bookingForm').reset();
            nextButton.disabled = true;
            resetSlotSelection();
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

            // Show/hide stylist options based on service selection
            Array.from(stylistSelect.options).forEach(option => {
                if (!option.value) {
                    // Skip the "Choose a stylist..." option
                    return;
                }

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

            // Show hint if no stylists match the service
            const hint = document.getElementById('noStylistsHint');
            if (selectedServiceId && matchCount === 0) {
                hint.classList.remove('d-none');
                stylistSelect.disabled = true;
            } else {
                hint.classList.add('d-none');
                stylistSelect.disabled = false;
            }

            // If the currently selected stylist is no longer valid, reset the selection
            if (currentStylistId && !currentStylistStillValid) {
                stylistSelect.value = '';
                validateStepOne();
            }
        }

        function loadAndShowSlots() {
            const stylistId = stylistSelect.value;
            if (!stylistId) {
                return;
            }

            const selectedStylistOption = stylistSelect.options[stylistSelect.selectedIndex];
            const selectedServiceOption = serviceSelect.options[serviceSelect.selectedIndex];

            const serviceName = selectedServiceOption.text.split(' (')[0]; // Remove duration
            const stylistName = selectedStylistOption.text.split(' - ')[0]; // Remove service name
            document.getElementById('selectedServiceSummaryText').innerText = serviceName;
            document.getElementById('selectedStylistSummaryText').innerText = stylistName;

            const serviceId = serviceSelect.value;
            fetch('/customer/stylists/' + stylistId + '/available-slots?serviceId=' + encodeURIComponent(serviceId))
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to load available slots.');
                    }
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
                        button.className = 'booking__slot py-3 text-center rounded bg-light w-100';
                        button.innerHTML = '<div class="fw-semibold">' + slot.startLabel + '</div>' +
                            '<div class="small text-muted">Ends ' + slot.endLabel + ' (' + slot.durationMinutes + ' min)</div>';
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
            if (!confirm('Are you sure you want to cancel this appointment? This action cannot be undone.')) {
                return;
            }

            const formData = new FormData();
            const csrfToken = document.querySelector('input[name="_csrf"]')?.value;
            if (csrfToken) {
                formData.append('_csrf', csrfToken);
            }

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
                    alert('Failed to cancel appointment: ' + (message || 'Unknown error (' + response.status + ')'));
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
        stylistSelect.addEventListener('change', validateStepOne);
        stylistSelect.addEventListener('change', resetSlotSelection);
    </script>
</body>
</html>
