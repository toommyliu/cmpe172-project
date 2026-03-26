<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Appointment" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.AvailabilitySlot" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="org.springframework.security.web.csrf.CsrfToken" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Stylist Dashboard</title>
</head>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <main class="container py-5">
        <%
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
            String successMessage = (String) request.getAttribute("successMessage");
            String errorMessage = (String) request.getAttribute("errorMessage");
            DateTimeFormatter slotDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a");
            DateTimeFormatter timeOnlyFormatter = DateTimeFormatter.ofPattern("h:mm a");
        %>
        <% if (successMessage != null) { %>
            <div class="alert alert-success" role="alert"><%= successMessage %></div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="alert alert-danger" role="alert"><%= errorMessage %></div>
        <% } %>

        <div class="row g-4">
            <div class="col-12">
                <div class="card booking__card border-0">
                    <div class="card-body p-4 p-md-5">
                        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                            <div>
                                <h2 class="h4 mb-1">Set One-Off Availability</h2>
                                <p class="text-muted mb-0">Create one-off time slots for customers to book.</p>
                            </div>
                        </div>

                        <form method="post" action="/stylist/availability" class="row g-3 align-items-end">
                            <% if (csrfToken != null) { %>
                                <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                            <% } %>
                            <div class="col-md-4">
                                <label for="startDateTime" class="form-label">Start</label>
                                <input id="startDateTime" name="startDateTime" type="datetime-local" class="form-control" required>
                            </div>
                            <div class="col-md-4">
                                <label for="endDateTime" class="form-label">End</label>
                                <input id="endDateTime" name="endDateTime" type="datetime-local" class="form-control" required>
                            </div>
                            <div class="col-md-4 d-grid">
                                <button type="submit" class="btn btn-primary">Add Slot</button>
                            </div>
                        </form>

                        <div class="mt-5">
                            <h3 class="h6 mb-3">Your Availability Slots</h3>
                            <div class="table-responsive bg-white rounded">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Slot ID</th>
                                            <th>Start</th>
                                            <th>End</th>
                                            <th>Status</th>
                                            <th class="text-end">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            List<AvailabilitySlot> slots = (List<AvailabilitySlot>) request.getAttribute("availabilitySlots");
                                            if (slots != null && !slots.isEmpty()) {
                                                for (AvailabilitySlot slot : slots) {
                                        %>
                                            <tr>
                                                <td><strong>#<%= slot.getId() %></strong></td>
                                                <td><%= slot.getStartDateTime().format(slotDateFormatter) %></td>
                                                <td><%= slot.getEndDateTime().format(slotDateFormatter) %></td>
                                                <td>
                                                    <%
                                                        String badgeClass = "bg-secondary";
                                                        if (slot.getStatus() == AvailabilitySlotStatus.Available) {
                                                            badgeClass = "bg-success";
                                                        } else if (slot.getStatus() == AvailabilitySlotStatus.Booked) {
                                                            badgeClass = "bg-primary";
                                                        } else if (slot.getStatus() == AvailabilitySlotStatus.Cancelled) {
                                                            badgeClass = "bg-dark";
                                                        }
                                                    %>
                                                    <span class="badge <%= badgeClass %>"><%= slot.getStatus().toString() %></span>
                                                </td>
                                                <td class="text-end">
                                                    <% if (slot.getStatus() == AvailabilitySlotStatus.Available) { %>
                                                        <form method="post" action="/stylist/availability/<%= slot.getId() %>/delete" style="display:inline;">
                                                            <% if (csrfToken != null) { %>
                                                                <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                                                            <% } %>
                                                            <button type="submit" class="btn btn-sm btn-outline-danger">Cancel Slot</button>
                                                        </form>
                                                    <% } else { %>
                                                        <span class="text-muted small">-</span>
                                                    <% } %>
                                                </td>
                                            </tr>
                                        <%
                                                }
                                            } else {
                                        %>
                                            <tr>
                                                <td colspan="5" class="text-center py-4 text-muted">No availability slots created yet.</td>
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

            <div class="col-12">
                <div class="card booking__card border-0">
                    <div class="card-body p-4 p-md-5">
                        <div class="mb-4">
                            <h1 class="h3 mb-1">Stylist Schedule</h1>
                            <p class="text-muted mb-0">Review your assigned appointments for today and prepare for upcoming services.</p>
                        </div>

                        <div class="mt-5">
                            <h2 class="h5 mb-4">Your Upcoming Services</h2>
                            <div class="table-responsive bg-white rounded">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                            <tr>
                                                <th>ID</th>
                                                <th>Service</th>
                                                <th>Customer</th>
                                                <th>Slot</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        <%
                                            List<Appointment> appointments = (List<Appointment>) request.getAttribute("appointments");
                                            if (appointments != null && !appointments.isEmpty()) {
                                                for (Appointment apt : appointments) {
                                                    String serviceName = apt.getServiceName();
                                                    if (serviceName == null || serviceName.isBlank()) {
                                                        serviceName = "Service #" + apt.getServiceId();
                                                    }
                                        %>
                                            <tr>
                                                <td><strong>#<%= apt.getId() %></strong></td>
                                                <td><span class="badge bg-primary text-white"><%= serviceName %></span></td>
                                                <td>
                                                    <% if (apt.getCustomerName() != null && !apt.getCustomerName().isBlank()) { %>
                                                        <%= apt.getCustomerName() %> (<%= apt.getCustomerUserId() %>)
                                                    <% } else { %>
                                                        Customer ID <%= apt.getCustomerUserId() %>
                                                    <% } %>
                                                </td>
                                                <td>
                                                    <% if (apt.getSlotStartDateTime() != null && apt.getSlotEndDateTime() != null) { %>
                                                        <%= apt.getSlotStartDateTime().format(slotDateFormatter) %> -
                                                        <%= apt.getSlotEndDateTime().format(timeOnlyFormatter) %>
                                                    <% } else { %>
                                                        Slot <%= apt.getAvailabilitySlotId() %>
                                                    <% } %>
                                                </td>
                                            </tr>
                                        <%
                                                }
                                            } else {
                                        %>
                                            <tr>
                                                <td colspan="4" class="text-center py-5 text-muted">
                                                    No appointments scheduled yet.
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
    </main>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const startInput = document.getElementById('startDateTime');
            const endInput = document.getElementById('endDateTime');
            if (!startInput || !endInput) {
                return;
            }

            const now = new Date();
            now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
            const minValue = now.toISOString().slice(0, 16);
            startInput.min = minValue;
            endInput.min = minValue;

            startInput.addEventListener('change', function() {
                if (startInput.value) {
                    endInput.min = startInput.value;
                }
            });
        });
    </script>
</body>
</html>
