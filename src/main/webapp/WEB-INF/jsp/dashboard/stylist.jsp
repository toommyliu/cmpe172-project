<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Appointment" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.Speciality" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Stylist Dashboard</title>
</head>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <main class="container py-5">
        <div class="row g-4">
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
                                            <th>Customer ID</th>
                                            <th>Slot</th>
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
                                                <td>Customer #<%= apt.getCustomerUserId() %></td>
                                                <td>Slot <%= apt.getAvailabilitySlotId() %></td>
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
</body>
</html>
