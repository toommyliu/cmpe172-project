<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.Speciality" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Stylist" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Book Appointment</title>
</head>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <div class="page-header">
        <div class="container page-header__container">
            <h1 class="h2 page-header__title">Complete Your Booking</h1>
            <p class="text-muted page-header__subtitle">Customize your experience with us.</p>
        </div>
    </div>

    <main class="container mb-5">
        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card booking__card">
                    <div class="card-body p-4 p-md-5">
                        <form onsubmit="alert('Success!'); window.location.href='/booking-confirmation'; return false;">
                            <div class="mb-5">
                                <h4 class="mb-4 d-flex align-items-center">
                                    <span class="booking__step-number">1</span> Service Selection
                                </h4>
                                <div class="row g-3">
                                    <div class="col-12">
                                        <label for="service" class="form-label fw-semibold">Which service would you like?</label>
                                        <select class="form-select" id="service" name="serviceId" required>
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
                                </div>
                            </div>

                            <div class="mb-5">
                                <h4 class="mb-4 d-flex align-items-center">
                                    <span class="booking__step-number">2</span> Stylist Preference
                                </h4>
                                <div class="col-12">
                                    <label for="stylist" class="form-label fw-semibold">Select your preferred stylist</label>
                                    <select class="form-select" id="stylist" name="stylistId" required>
                                        <option selected disabled value="">Any available stylist</option>
                                        <%
                                            List<Stylist> stylists = (List<Stylist>) request.getAttribute("stylists");
                                            if (stylists != null) {
                                                for (Stylist s : stylists) {
                                        %>
                                            <option value="<%= s.getId() %>"><%= s.getFirstName() %> <%= s.getLastName() %> - <%= s.getSpeciality() %></option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </div>
                            </div>

                            <div class="d-grid gap-2 pt-3 border-top">
                                <button type="submit" class="btn btn-primary booking__confirm-btn">
                                    Confirm & Book Appointment
                                </button>
                                <a href="/available-slots" class="btn btn-link text-muted">Go Back to Time Selection</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="booking__summary" style="top: 2rem;">
                    <h5 class="fw-bold mb-4">Booking Summary</h5>

                    <div class="d-flex mb-3">
                        <div class="me-3">
                            <i class="bi bi-calendar-event fs-4"></i>
                        </div>
                        <div>
                            <div class="fw-semibold">Date & Time</div>
                            <div class="text-muted">Tuesday, March 10, 2026</div>
                            <div class="text-muted">10:00 AM</div>
                        </div>
                    </div>

                    <div class="d-flex mb-4">
                        <div class="me-3">
                            <i class="bi bi-geo-alt fs-4"></i>
                        </div>
                        <div>
                            <div class="fw-semibold">Location</div>
                            <div class="text-muted">The Studio</div>
                            <div class="text-muted small">One Washington Square, San Jose, CA</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
</body>
</html>
