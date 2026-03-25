<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />
    <main class="container d-flex justify-content-center align-items-center" style="height: calc(100vh - 60px);">
        <div class="text-center">
            <p class="lead">Welcome to your hair salon!</p>
            <div class="d-grid gap-3 flex-col">
                <% if (request.getUserPrincipal() == null) { %>
                    <a href="/login">Login</a>
                    <a href="/register">Create an account</a>
                <% } else { %>
                    <a href="/dashboard">Go to dashboard</a>
                <% } %>
            </div>
        </div>
    </main>
</body>

</html>
