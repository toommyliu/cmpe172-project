<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Appointment" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.Speciality" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="common/header.jsp" />

<head>
    <title>The Studio - Appointment Form</title>
</head>

<body>
    <jsp:include page="common/navbar.jsp" />

    <main class="container py-4">
        <%
            Appointment appointment = (Appointment) request.getAttribute("appointment");
            String formAction = (String) request.getAttribute("formAction");
            String pageTitle = (String) request.getAttribute("pageTitle");
            if (appointment == null) {
                appointment = new Appointment();
            }
            if (formAction == null) {
                formAction = "/appointments";
            }
            if (pageTitle == null) {
                pageTitle = "Appointment Form";
            }
        %>

        <h1 class="h3 mb-4"><%= pageTitle %></h1>

        <div class="card booking__card">
            <div class="card-body p-4">
                <form method="post" action="<%= formAction %>">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label" for="customerUserId">Customer ID</label>
                            <input class="form-control" id="customerUserId" name="customerUserId" type="number" min="1"
                                   value="<%= appointment.getCustomerUserId() == 0 ? "" : appointment.getCustomerUserId() %>" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="stylistUserId">Stylist ID</label>
                            <input class="form-control" id="stylistUserId" name="stylistUserId" type="number" min="1"
                                   value="<%= appointment.getStylistUserId() == 0 ? "" : appointment.getStylistUserId() %>" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="serviceId">Service</label>
                            <select class="form-select" id="serviceId" name="serviceId" required>
                                <option value="" disabled <%= appointment.getServiceId() == 0 ? "selected" : "" %>>Choose service</option>
                                <%
                                    for (Speciality s : Speciality.values()) {
                                        if (s == Speciality.None) {
                                            continue;
                                        }
                                %>
                                <option value="<%= s.getValue() %>" <%= appointment.getServiceId() == s.getValue() ? "selected" : "" %>><%= s.toString() %></option>
                                <%
                                    }
                                %>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="availabilitySlotId">Availability Slot ID</label>
                            <input class="form-control" id="availabilitySlotId" name="availabilitySlotId" type="number" min="1"
                                   value="<%= appointment.getAvailabilitySlotId() == 0 ? "" : appointment.getAvailabilitySlotId() %>" required>
                        </div>
                    </div>

                    <div class="d-flex gap-2 mt-4">
                        <button class="btn btn-primary" type="submit">Save</button>
                        <a class="btn btn-outline-secondary" href="/appointments">Cancel</a>
                    </div>
                </form>
            </div>
        </div>
    </main>
</body>

</html>
