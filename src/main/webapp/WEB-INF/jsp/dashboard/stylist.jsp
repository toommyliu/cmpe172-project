<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Stylist Dashboard</title>
</head>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <main class="container py-5">
        <div class="card booking__card">
            <div class="card-body p-4 p-md-5">
                <h1 class="h3 mb-3">Stylist Dashboard</h1>
                <p class="text-muted mb-4">Review your assigned appointments and prepare for upcoming services.</p>
                <a class="btn btn-primary" href="/stylist/appointments">View My Schedule</a>
            </div>
        </div>
    </main>
</body>
</html>
