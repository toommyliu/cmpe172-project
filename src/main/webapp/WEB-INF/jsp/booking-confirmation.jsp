<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="common/header.jsp" />

<head>
    <title>The Studio - Booking Confirmed</title>
</head>

<body>
    <jsp:include page="common/navbar.jsp" />

    <main class="container py-5 d-flex align-items-center justify-content-center" style="height: calc(100vh - 60px);">
        <div class="row justify-content-center w-100">
            <div class="col-lg-6 text-center">
                <div class="mb-4">
                    <div class="display-1 text-success mb-3">
                        <i class="bi bi-check-circle-fill"></i>
                    </div>
                    <h1 class="h2 fw-bold">Booking Confirmed!</h1>
                    <p class="text-muted fs-5">Thank you! Your appointment at The Studio is scheduled.</p>
                </div>

                <div class="card booking__card mb-4 text-start">
                    <div class="card-body p-4">
                        <h5 class="card-title fw-bold mb-4">Appointment Details</h5>

                        <div class="d-flex mb-3">
                            <div class="text-muted me-3" style="width: 24px;"><i class="bi bi-person"></i></div>
                            <div>
                                <div class="text-muted small">Stylist</div>
                                <div class="fw-medium">Bob Smith.</div>
                            </div>
                        </div>

                        <div class="d-flex mb-3">
                            <div class="text-muted me-3" style="width: 24px;"><i class="bi bi-scissors"></i></div>
                            <div>
                                <div class="text-muted small">Service</div>
                                <div class="fw-medium">Coloring</div>
                            </div>
                        </div>

                        <div class="d-flex mb-3">
                            <div class="text-muted me-3" style="width: 24px;"><i class="bi bi-calendar-event"></i></div>
                            <div>
                                <div class="text-muted small">Date & Time</div>
                                <div>
                                    <span class="fw-medium" id="date"></span>
                                </div>
                                <div>
                                    <span class="fw-medium" id="time"></span>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex">
                            <div class="text-muted me-3" style="width: 24px;"><i class="bi bi-geo-alt"></i></div>
                            <div>
                                <div class="text-muted small">Location</div>
                                <div class="fw-medium">The Studio</div>
                                <div class="text-muted small">One Washington Square, San Jose, CA</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <script>
        document.addEventListener('DOMContentLoaded', () => {
            const today = new Date();
            const today_1hr30 = new Date(today.getTime() + (90 * 60 * 1_000)); // 90 min * 60 sec/min * 1_000 ms / sec

            const date = document.querySelector('#date');
            const dateOptions = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
            date.innerHTML = today.toLocaleDateString('en-US', dateOptions);

            const time = document.querySelector('#time');
            /**
             * @param {Date} d
             **/
            const formatTime = (d) => d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true });
            time.innerHTML = formatTime(today) + " - " + formatTime(today_1hr30);
        });
    </script>
</body>
</html>
