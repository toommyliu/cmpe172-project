<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Available Slots</title>
</head>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <div class="page-header">
        <div class="container page-header__container">
            <h1 class="h2 page-header__title">Select a Time</h1>
            <p class="text-muted page-header__subtitle">Choose your preferred appointment time for March 10, 2026.</p>
        </div>
    </div>

    <main class="container">
        <div class="row">
            <div class="col-lg-8">
                <div class="card booking__card">
                    <div class="card-body p-4">
                    <h3 class="h5 mb-4">Available Slots</h3>

                    <div class="booking__slots">
                        <div class="booking__slots-title">Morning</div>
                        <div class="booking__slots-grid">
                            <div class="booking__slot">9:00 AM</div>
                            <div class="booking__slot">9:30 AM</div>
                            <div class="booking__slot booking__slot--selected">10:00 AM</div>
                            <div class="booking__slot">10:30 AM</div>
                            <div class="booking__slot">11:00 AM</div>
                            <div class="booking__slot booking__slot--disabled" 
                                data-bs-toggle="tooltip" data-bs-placement="top"
                                data-bs-title="This time is unavailable."
                            >11:30 AM</div>
                        </div>
                        <div class="booking__slots-title">Afternoon</div>
                        <div class="booking__slots-grid">
                            <div class="booking__slot">1:00 PM</div>
                            <div class="booking__slot">1:30 PM</div>
                            <div class="booking__slot">2:00 PM</div>
                            <div class="booking__slot">2:30 PM</div>
                            <div class="booking__slot">3:00 PM</div>
                            <div class="booking__slot">3:30 PM</div>
                            <div class="booking__slot">4:00 PM</div>
                            <div class="booking__slot">4:30 PM</div>
                        </div>
                    </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="booking__summary" style="top: 2rem;">
                    <h5 class="fw-bold mb-4">Booking Summary</h5>

                    <div class="d-flex mb-3">
                        <div class="me-3">
                            <i class="bi bi-scissors fs-4"></i>
                        </div>
                        <div>
                            <div class="fw-semibold">Service & Stylist</div>
                            <div class="text-muted">Coloring</div>
                            <div class="text-muted">Bob Smith</div>
                        </div>
                    </div>

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

                    <div class="d-grid pt-3 border-top">
                        <button class="btn btn-primary" onclick="window.location.href='/book-appointment'">Continue</button>
                    </div>
                </div>
            </div>
        </div>
    </main>
</body>
</html>
