<!DOCTYPE html>
<html lang="en">
<jsp:include page="common/header.jsp" />

<body>
    <jsp:include page="common/navbar.jsp" />
    <main class="container d-flex justify-content-center align-items-center" style="height: calc(100vh - 60px);">
        <div class="text-center">
            <p class="lead">Welcome to your hair salon!</p>
            <div class="d-grid gap-3 flex-col">
                <a href="/appointments">Demo appointments</a>
                <a href="/available-slots" >Demo available slots</a>
                <a href="/book-appointment">Demo appointment booking</a>
                <a href="/booking-confirmation">Demo booking confirmation</a>
            </div>
        </div>
    </main>
</body>

</html>