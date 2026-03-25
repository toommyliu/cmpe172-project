<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.springframework.security.web.csrf.CsrfToken" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Login</title>
</head>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <main class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-7 col-lg-5">
                <div class="card border-0">
                    <div class="card-body p-4 p-md-5">
                        <h1 class="h3 mb-4">Sign in</h1>

                        <%
                            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
                            String error = request.getParameter("error");
                            String logout = request.getParameter("logout");
                            String successMessage = (String) request.getAttribute("successMessage");
                            if (error != null) {
                        %>
                            <div class="alert alert-danger" role="alert">Invalid email or password.</div>
                        <%
                            }
                            if (logout != null) {
                        %>
                            <div class="alert alert-success" role="alert">You have been logged out.</div>
                        <%
                            }
                            if (successMessage != null) {
                        %>
                            <div class="alert alert-success" role="alert"><%= successMessage %></div>
                        <%
                            }
                        %>

                        <form method="post" action="/login">
                            <% if (csrfToken != null) { %>
                                <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                            <% } %>
                            <div class="mb-3">
                                <label class="form-label" for="username">Email</label>
                                <input class="form-control" id="username" name="username" type="email" required>
                            </div>
                            <div class="mb-4">
                                <label class="form-label" for="password">Password</label>
                                <input class="form-control" id="password" name="password" type="password" required>
                            </div>
                            <div class="d-grid">
                                <button class="btn btn-primary" type="submit">Login</button>
                            </div>
                        </form>

                        <div class="mt-4 text-center">
                            <p class="text-muted small mb-0">
                                Need an account? <a href="/register">Sign up</a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
</body>
</html>
