<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.springframework.security.web.csrf.CsrfToken" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<head>
    <title>The Studio - Sign up</title>
</head>

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <main class="container py-5 auth-page">
        <div class="row justify-content-center">
            <div class="col-lg-7">
                <div class="card booking__card">
                    <div class="card-body p-4 p-md-5">
                        <h1 class="h3 mb-3">Create account</h1>
                        <p class="text-muted mb-4">New accounts are created as customers.</p>

                        <%
                            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
                            String errorMessage = (String) request.getAttribute("errorMessage");
                            if (errorMessage != null) {
                        %>
                            <div class="alert alert-danger" role="alert"><%= errorMessage %></div>
                        <%
                            }
                        %>

                        <form method="post" action="/register">
                            <% if (csrfToken != null) { %>
                                <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                            <% } %>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label" for="firstName">First name</label>
                                    <input class="form-control" id="firstName" name="firstName" type="text" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="lastName">Last name</label>
                                    <input class="form-control" id="lastName" name="lastName" type="text" required>
                                </div>
                                <div class="col-md-8">
                                    <label class="form-label" for="emailAddress">Email</label>
                                    <input class="form-control" id="emailAddress" name="emailAddress" type="email" required>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label" for="phoneNumber">Phone</label>
                                    <input class="form-control" id="phoneNumber" name="phoneNumber" type="text">
                                </div>
                                <div class="col-12">
                                    <label class="form-label" for="password">Password</label>
                                    <input class="form-control" id="password" name="password" type="password" minlength="8" required>
                                </div>
                            </div>

                            <div class="d-grid mt-4">
                                <button class="btn btn-primary" type="submit">Create account</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </main>
</body>
</html>
