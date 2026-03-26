<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Appointment" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Stylist" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.Speciality" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
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
                                <div class="table-responsive bg-white rounded">
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
                                                List<Appointment> appointments = (List<Appointment>) request.getAttribute("appointments");
                                                if (appointments != null && !appointments.isEmpty()) {
                                                    for (Appointment apt : appointments) {
                                                        String serviceName = Speciality.fromValue(apt.getServiceId()).toString();
                                            %>
                                                <tr>
                                                    <td><strong>#<%= apt.getId() %></strong></td>
                                                    <td><span class="badge bg-primary text-white"><%= serviceName %></span></td>
                                                    <td>
                                                        <% if (apt.getStylistName() != null && !apt.getStylistName().isBlank()) { %>
                                                            <%= apt.getStylistName() %> (<%= apt.getStylistUserId() %>)
                                                        <% } else { %>
                                                            Stylist ID <%= apt.getStylistUserId() %>
                                                        <% } %>
                                                    </td>
                                                    <td>
                                                        <% if (apt.getSlotStartDateTime() != null && apt.getSlotEndDateTime() != null) { %>
                                                            <%= apt.getSlotStartDateTime().format(slotFormatter) %> - <%= apt.getSlotEndDateTime().format(DateTimeFormatter.ofPattern("h:mm a")) %>
                                                        <% } else { %>
                                                            Slot <%= apt.getAvailabilitySlotId() %>
                                                        <% } %>
                                                    </td>
                                                    <td class="text-end">
                                                        <span class="text-muted small">placeholder</span>
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
                                    org.springframework.security.web.csrf.CsrfToken csrfToken =
                                            (org.springframework.security.web.csrf.CsrfToken) request.getAttribute("_csrf");
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
                                                    List<Speciality> specialities = (List<Speciality>) request.getAttribute("specialities");
                                                    if (specialities != null) {
                                                        for (Speciality s : specialities) {
                                                %>
                                                    <option value="<%= s.getValue() %>"><%= s.toString() %></option>
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
                                                    List<Stylist> stylists = (List<Stylist>) request.getAttribute("stylists");
                                                    if (stylists != null) {
                                                        for (Stylist s : stylists) {
                                                %>
                                                    <option value="<%= s.getId() %>" data-speciality-id="<%= s.getSpeciality().getValue() %>"><%= s.getFirstName() %> <%= s.getLastName() %> - <%= s.getSpeciality().toString() %></option>
                                                <%
                                                        }
                                                    }
                                                %>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="d-flex justify-content-end gap-2 mt-5">
                                        <button type="button" class="btn btn-secondary" onclick="cancelBooking()">Cancel</button>
                                        <button type="button" id="nextToStep2" class="btn btn-primary" onclick="loadAndShowSlots()" disabled>Next Step</button>
                                    </div>
                                </div>

                                <!-- Step 2: Slot Selection -->
                                <div id="step2" class="booking-step">
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

        function loadAndShowSlots() {
            const stylistId = stylistSelect.value;
            if (!stylistId) {
                return;
            }

            const selectedStylistOption = stylistSelect.options[stylistSelect.selectedIndex];
            const selectedServiceId = serviceSelect.value;
            const selectedStylistSpeciality = selectedStylistOption.getAttribute('data-speciality-id');
            if (selectedServiceId !== selectedStylistSpeciality) {
                alert('Please choose a stylist whose speciality matches your service.');
                return;
            }

            fetch('/customer/stylists/' + stylistId + '/available-slots')
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
                        button.addEventListener('click', function() {
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

        serviceSelect.addEventListener('change', validateStepOne);
        stylistSelect.addEventListener('change', validateStepOne);
        serviceSelect.addEventListener('change', resetSlotSelection);
        stylistSelect.addEventListener('change', resetSlotSelection);
    </script>
</body>
</html>
