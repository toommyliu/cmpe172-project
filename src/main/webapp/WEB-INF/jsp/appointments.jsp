<%@ page import="edu.sjsu.cmpe172.salon.model.Appointment" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Customer" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Stylist" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.Speciality" %>
<%@ page import="edu.sjsu.cmpe172.salon.controller.AppointmentController.AppointmentHelper" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="common/header.jsp" />

<head>
    <title>The Studio - Appointments</title>
</head>

<body>
    <jsp:include page="common/navbar.jsp" />

    <div class="page-header">
        <div class="container page-header__container">
            <h1 class="h2 page-header__title">Appointments</h1>
            <p class="text-muted page-header__subtitle">Manage and track your appointments with us.</p>
        </div>
    </div>

    <main class="container">
        <div class="table-responsive bg-white p-4 rounded">
            <table class="table table-striped align-middle">
                <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Customer</th>
                        <th>Stylist</th>
                        <th>Service</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        AppointmentHelper helper = (AppointmentHelper) request.getAttribute("helper");
                        Stylist defaultStylist = (Stylist) request.getAttribute("stylist");
                        List<Appointment> appointments = helper != null ? helper.getAppointments() : null;
                        if (appointments != null && !appointments.isEmpty()) {
                            Customer currentUser = (Customer) request.getAttribute("user");
                            for (Appointment apt : appointments) {
                                String customerName = (currentUser != null && apt.getCustomerUserId() == currentUser.getId())
                                    ? currentUser.getFirstName() + " " + currentUser.getLastName()
                                    : "ID: " + apt.getCustomerUserId();
                                String stylistName = (defaultStylist != null && apt.getStylistUserId() == defaultStylist.getId())
                                    ? defaultStylist.getFirstName() + " " + defaultStylist.getLastName()
                                    : "ID: " + apt.getStylistUserId();
                                String serviceName = Speciality.fromValue(apt.getServiceId()).toString();
                    %>
                        <tr>
                            <td><strong>#<%= apt.getId() %></strong></td>
                            <td><%= customerName %></td>
                            <td><%= stylistName %></td>
                            <td><span class="badge bg-primary text-white"><%= serviceName %></span></td>
                            <td>
                                <button class="btn btn-sm btn-outline-danger">Cancel</button>
                            </td>
                        </tr>
                    <%
                            }
                        } else {
                    %>
                        <tr>
                            <td colspan="6" class="text-center py-4">No appointments found.</td>
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
