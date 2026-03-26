<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">The Studio</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <%
                String requestUri = (String) request.getAttribute("jakarta.servlet.forward.request_uri");
                if (requestUri == null) {
                    requestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
                }
                if (requestUri == null) {
                    requestUri = request.getRequestURI();
                }
                String contextPath = request.getContextPath();
                String path = requestUri.substring(contextPath.length());
                boolean authenticated = request.getUserPrincipal() != null;
                boolean isAdmin = request.isUserInRole("ADMIN");
                boolean isStylist = request.isUserInRole("STYLIST");
                boolean isCustomer = request.isUserInRole("CUSTOMER");
                org.springframework.security.web.csrf.CsrfToken csrfToken =
                        (org.springframework.security.web.csrf.CsrfToken) request.getAttribute("_csrf");
            %>
            <div class="navbar-nav me-auto">
                <a class="nav-link <%= "/".equals(path) ? "active" : "" %>" href="<%= contextPath %>/">Home</a>
                <% if (isAdmin) { %>
                    <a class="nav-link <%= "/dashboard".equals(path) ? "active" : "" %>" href="<%= contextPath %>/dashboard">Dashboard</a>
                    <a class="nav-link <%= path.startsWith("/appointments") ? "active" : "" %>" href="<%= contextPath %>/appointments">Appointments</a>
                <% } %>
                <% if (isStylist || isCustomer) { %>
                    <a class="nav-link <%= "/dashboard".equals(path) ? "active" : "" %>" href="<%= contextPath %>/dashboard">Dashboard</a>
                <% } %>
            </div>
            <div class="navbar-nav ms-auto align-items-lg-center gap-lg-2">
                <% if (!authenticated) { %>
                    <a class="nav-link <%= path.startsWith("/login") ? "active" : "" %>" href="<%= contextPath %>/login">Login</a>
                    <a class="btn btn-outline-light btn-sm <%= path.startsWith("/register") ? "active" : "" %>" href="<%= contextPath %>/register">Sign up</a>
                <% } else { %>
                    <span class="navbar-text small text-light opacity-75"><%= request.getUserPrincipal().getName() %></span>
                    <form method="post" action="<%= contextPath %>/logout" class="d-inline mb-0">
                        <% if (csrfToken != null) { %>
                            <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                        <% } %>
                        <button class="btn btn-outline-light btn-sm" type="submit">Logout</button>
                    </form>
                <% } %>
            </div>
        </div>
    </div>
</nav>

