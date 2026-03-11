<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">The Studio</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <div class="navbar-nav">
                <a class="nav-link ${pageContext.request.requestURI.endsWith('/') || pageContext.request.requestURI.contains('index') ? 'active' : ''}" href="/">Home</a>
                <a class="nav-link ${pageContext.request.requestURI.contains('appointments') ? 'active' : ''}" href="/appointments">Appointments</a>
            </div>
        </div>
    </div>
</nav>


