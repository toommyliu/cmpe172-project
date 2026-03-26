<%@ page import="edu.sjsu.cmpe172.salon.dto.AppointmentDto" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="org.springframework.security.web.csrf.CsrfToken" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Appointments</title>
</head>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <div class="page-header">
        <div class="container page-header__container">
            <%
                String pageTitle = (String) request.getAttribute("pageTitle");
                if (pageTitle == null) {
                    pageTitle = "Appointments";
                }
                boolean isCustomer = request.isUserInRole("CUSTOMER");
                boolean isStylist = request.isUserInRole("STYLIST");
                boolean isAdmin = request.isUserInRole("ADMIN");
            %>
            <h1 class="h2 page-header__title"><%= pageTitle %></h1>
            <p class="text-muted page-header__subtitle">Manage and track your appointments with us.</p>
        </div>
    </div>

    <main class="container">
        <%
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
            String successMessage = (String) request.getAttribute("successMessage");
            String errorMessage = (String) request.getAttribute("errorMessage");
            Boolean showManagementActions = (Boolean) request.getAttribute("showManagementActions");
            boolean canManage = showManagementActions != null && showManagementActions;
            DateTimeFormatter slotDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a");
            DateTimeFormatter timeOnlyFormatter = DateTimeFormatter.ofPattern("h:mm a");
            if (successMessage != null) {
        %>
            <div class="alert alert-success" role="alert"><%= successMessage %></div>
        <%
            }
            if (errorMessage != null) {
        %>
            <div class="alert alert-danger" role="alert"><%= errorMessage %></div>
        <%
            }
        %>

        <% if (canManage) { %>
            <div class="d-flex justify-content-end mb-3">
                <a class="btn btn-primary" href="/appointments/new">New Appointment</a>
            </div>
        <% } else if (isCustomer) { %>
            <div class="d-flex justify-content-end mb-3">
                <a class="btn btn-primary" href="/available-slots">Book Now</a>
            </div>
        <% } %>

        <div class="table-responsive bg-white p-4 rounded">
            <table class="table table-striped align-middle">
                <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <% if (!isCustomer) { %><th>Customer</th><% } %>
                        <% if (!isStylist) { %><th>Stylist</th><% } %>
                        <th>Service</th>
                        <th>Slot</th>
                        <% if (canManage) { %>
                            <th>Actions</th>
                        <% } %>
                    </tr>
                </thead>
                <tbody>
                    <%
                        List<AppointmentDto> appointments = (List<AppointmentDto>) request.getAttribute("appointments");
                        if (appointments != null && !appointments.isEmpty()) {
                            for (AppointmentDto apt : appointments) {
                                String serviceName = apt.getServiceName();
                                if (serviceName == null || serviceName.isBlank()) {
                                    serviceName = "Service #" + apt.getServiceId();
                                }
                    %>
                        <tr>
                            <td><strong>#<%= apt.getId() %></strong></td>
                            <% if (!isCustomer) { %>
                                <td>
                                    <% if (apt.getCustomerName() != null && !apt.getCustomerName().isBlank()) { %>
                                        <%= apt.getCustomerName() %> (ID <%= apt.getCustomerUserId() %>)
                                    <% } else { %>
                                        Customer ID <%= apt.getCustomerUserId() %>
                                    <% } %>
                                </td>
                            <% } %>
                            <% if (!isStylist) { %>
                                <td>
                                    <% if (apt.getStylistName() != null && !apt.getStylistName().isBlank()) { %>
                                        <%= apt.getStylistName() %> (ID <%= apt.getStylistUserId() %>)
                                    <% } else { %>
                                        Stylist ID <%= apt.getStylistUserId() %>
                                    <% } %>
                                </td>
                            <% } %>
                            <td><span class="badge bg-primary text-white"><%= serviceName %></span></td>
                            <td>
                                <% if (apt.getSlotStartDateTime() != null && apt.getSlotEndDateTime() != null) { %>
                                    <%= apt.getSlotStartDateTime().format(slotDateFormatter) %> -
                                    <%= apt.getSlotEndDateTime().format(timeOnlyFormatter) %>
                                <% } else { %>
                                    Slot <%= apt.getAvailabilitySlotId() %>
                                <% } %>
                            </td>
                            <% if (canManage) { %>
                                <td>
                                    <a class="btn btn-sm btn-outline-secondary" href="/appointments/<%= apt.getId() %>/edit">Edit</a>
                                    <form method="post" action="/appointments/<%= apt.getId() %>/delete" style="display:inline;">
                                        <% if (csrfToken != null) { %>
                                            <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                                        <% } %>
                                        <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                                    </form>
                                </td>
                            <% } %>
                        </tr>
                    <%
                            }
                        } else {
                    %>
                        <tr>
                            <%
                                int colCount = 3 + (isCustomer ? 0 : 1) + (isStylist ? 0 : 1) + (canManage ? 1 : 0);
                            %>
                            <td colspan="<%= colCount %>" class="text-center py-4">No appointments found.</td>
                        </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>
    </main>
</body>
</html>
