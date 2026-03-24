<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">The Studio</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <%
                // get all available request URI attributes to determine the current path
                String requestUri = (String) request.getAttribute("jakarta.servlet.forward.request_uri");
                if (requestUri == null) {
                    requestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
                }
                if (requestUri == null) {
                    requestUri = request.getRequestURI();
                }
                String contextPath = request.getContextPath();
                String path = requestUri.substring(contextPath.length());
            %>
            <div class="navbar-nav">
                <a class="nav-link <%= "/".equals(path) ? "active" : "" %>" href="<%= contextPath %>/">Home</a>
                <a class="nav-link <%= path.startsWith("/appointments") ? "active" : "" %>" href="<%= contextPath %>/appointments">Appointments</a>
            </div>
        </div>
    </div>
</nav>


