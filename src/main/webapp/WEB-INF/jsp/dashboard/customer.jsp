<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Customer Dashboard</title>
</head>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <main class="container py-5">
        <div class="card booking__card">
            <div class="card-body p-4 p-md-5">
                <h1 class="h3 mb-3">Customer Dashboard</h1>
                <p class="text-muted mb-4">Book your next appointment or review your booking history.</p>
                <div class="d-flex gap-2 flex-wrap">
                    <a class="btn btn-primary" href="/book-appointment">Book Appointment</a>
                    <a class="btn btn-outline-secondary" href="/customer/appointments">My Appointments</a>
                </div>
            </div>
        </div>
    </main>
</body>
</html>
