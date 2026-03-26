<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Appointment" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Stylist" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.Speciality" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Customer Dashboard</title>
</head>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <main class="container py-5">
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
                                                    <td>Slot <%= apt.getAvailabilitySlotId() %></td>
                                                    <td class="text-end">
                                                        <a class="btn btn-sm btn-secondary" href="/appointments/<%= apt.getId() %>/edit">Modify</a>
                                                    </td>
                                                </tr>
                                            <%
                                                    }
                                                } else {
                                            %>
                                                <tr>
                                                    <td colspan="4" class="text-center py-5 text-muted">
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
                                    <p class="text-muted mb-0" id="stepIndicator">Step 1: Choose a preferred time</p>
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

                                <!-- Step 1: Slot Selection -->
                                <div id="step1" class="booking-step active">
                                    <h4 class="h5 mb-4">Morning</h4>
                                    <div class="row g-3 mb-5">
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(1, '9:00 AM', this)">9:00 AM</div></div>
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(2, '9:30 AM', this)">9:30 AM</div></div>
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(3, '10:00 AM', this)">10:00 AM</div></div>
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(4, '10:30 AM', this)">10:30 AM</div></div>
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(5, '11:00 AM', this)">11:00 AM</div></div>
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light booking__slot--disabled" title="Taken">11:30 AM</div></div>
                                    </div>

                                    <h4 class="h5 mb-4">Afternoon</h4>
                                    <div class="row g-3 mb-5">
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(6, '1:00 PM', this)">1:00 PM</div></div>
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(7, '1:30 PM', this)">1:30 PM</div></div>
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(8, '2:00 PM', this)">2:00 PM</div></div>
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(9, '2:30 PM', this)">2:30 PM</div></div>
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(10, '3:00 PM', this)">3:00 PM</div></div>
                                        <div class="col-md-2 col-4"><div class="booking__slot py-3 text-center rounded bg-light" onclick="selectSlot(11, '3:30 PM', this)">3:30 PM</div></div>
                                    </div>

                                    <div class="d-flex justify-content-end gap-2 mt-5">
                                        <button type="button" class="btn btn-secondary" onclick="cancelBooking()">Cancel</button>
                                        <button type="button" id="nextToStep2" class="btn btn-primary" onclick="goToStep(2)" disabled>Next Step</button>
                                    </div>
                                </div>

                                <!-- Step 2: Service & Stylist -->
                                <div id="step2" class="booking-step">
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
                                                <option selected disabled value="">Any available stylist</option>
                                                <%
                                                    List<Stylist> stylists = (List<Stylist>) request.getAttribute("stylists");
                                                    if (stylists != null) {
                                                        for (Stylist s : stylists) {
                                                %>
                                                    <option value="<%= s.getId() %>"><%= s.getFirstName() %> <%= s.getLastName() %> - <%= s.getSpeciality().toString() %></option>
                                                <%
                                                        }
                                                    }
                                                %>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="alert alert-info mt-5">
                                        <div class="d-flex align-items-center">
                                            <div class="me-3 fs-3"><i class="bi bi-clock"></i></div>
                                            <div>
                                                <h5 class="mb-0 h6">Selected Time</h5>
                                                <p class="mb-0 text-muted" id="selectedTimeSummary">Not selected</p>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="d-flex justify-content-between gap-2 mt-5">
                                        <button type="button" class="btn btn-secondary" onclick="goToStep(1)">Back</button>
                                        <button type="submit" class="btn btn-primary px-5">Confirm Booking</button>
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
                indicator.innerText = "Step 1: Choose a preferred time";
            } else {
                indicator.innerText = "Step 2: Service & Stylist Preference";
            }
        }

        function selectSlot(id, time, el) {
            if (el.classList.contains('booking__slot--disabled')) return;

            document.querySelectorAll('.booking__slot').forEach(s => s.classList.remove('booking__slot--selected'));
            el.classList.add('booking__slot--selected');

            document.getElementById('selectedSlotId').value = id;
            document.getElementById('selectedTimeSummary').innerText = time;
            document.getElementById('nextToStep2').disabled = false;
        }

        function resetForm() {
            document.getElementById('bookingForm').reset();
            document.querySelectorAll('.booking__slot').forEach(s => s.classList.remove('booking__slot--selected'));
            document.getElementById('nextToStep2').disabled = true;
            document.getElementById('selectedSlotId').value = '';
        }
    </script>
</body>
</html>
