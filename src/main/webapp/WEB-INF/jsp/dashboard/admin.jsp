<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.Speciality" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.User" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Stylist" %>
<%@ page import="org.springframework.security.web.csrf.CsrfToken" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <div class="page-header">
        <div class="container">
            <h1 class="page-header__title">Admin Dashboard</h1>
            <p class="page-header__subtitle text-secondary">Manage platform users and assign stylist roles.</p>
        </div>
    </div>

    <main class="container pb-5">
        <%
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
            String successMessage = (String) request.getAttribute("successMessage");
            String errorMessage = (String) request.getAttribute("errorMessage");
            if (successMessage != null) {
        %>
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i><%= successMessage %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <%
            }
            if (errorMessage != null) {
        %>
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i><%= errorMessage %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <%
            }
        %>

        <div class="card border-0">
            <div class="card-header bg-white border-bottom py-3">
                <div class="row align-items-center g-3">
                    <div class="col-md-4">
                        <h2 class="h5 mb-0 fw-bold">User Management</h2>
                    </div>
                    <div class="col-md-8">
                        <div class="d-flex flex-column flex-sm-row gap-2 justify-content-md-end">
                            <div class="input-group input-group-sm" style="max-width: 300px;">
                                <span class="input-group-text bg-white border-end-0">
                                    <i class="bi bi-search text-muted"></i>
                                </span>
                                <input type="text" id="userSearch" class="form-control border-start-0" placeholder="Search by name or email...">
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <label for="roleFilter" class="small text-muted text-nowrap">Filter by:</label>
                                <select id="roleFilter" class="form-select form-select-sm" style="width: auto; min-width: 120px;">
                                    <option value="ALL">All Roles</option>
                                    <option value="ADMIN">Admin</option>
                                    <option value="STYLIST">Stylist</option>
                                    <option value="CUSTOMER">Customer</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table id="userTable" class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th class="ps-4">ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th class="text-center">Role</th>
                                <th class="text-center">Specialty</th>
                                <th class="pe-4 text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <%
                            List<User> users = (List<User>) request.getAttribute("users");
                            if (users != null) {
                                for (User user : users) {
                                    boolean isStylist = user.isStylist();
                                    boolean isCustomer = user.isCustomer();
                                    boolean isAdmin = user.isAdmin();
                                    String fullName = user.getFirstName() + " " + user.getLastName();
                                    String roleStr = user.getRole().toString().toUpperCase();
                        %>
                            <tr data-search="<%= fullName %> <%= user.getEmailAddress() %>" data-role="<%= roleStr %>">
                                <td class="ps-4">
                                    <span class="text-secondary font-monospace small">#<%= user.getId() %></span>
                                </td>
                                <td>
                                    <div class="fw-semibold"><%= fullName %></div>
                                </td>
                                <td>
                                    <div class="text-secondary small text-nowrap"><%= user.getEmailAddress() %></div>
                                </td>
                                <td class="text-center">
                                    <%
                                        String badgeClass = isStylist ? "text-bg-primary" :
                                                           isAdmin ? "text-bg-dark" : "text-bg-light border";
                                    %>
                                    <span class="badge rounded-pill <%= badgeClass %>">
                                        <%= user.getRole().toString() %>
                                    </span>
                                </td>
                                <td class="text-center">
                                    <% if (isStylist) { %>
                                        <form id="form-<%= user.getId() %>" method="post" action="/admin/users/assign-stylist">
                                            <input type="hidden" name="userId" value="<%= user.getId() %>">
                                            <% if (csrfToken != null) { %>
                                                <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                                            <% } %>
                                            <select class="form-select form-select-sm d-inline-block w-auto" name="specialityId" style="min-width: 140px;">
                                                <%
                                                    for (Speciality speciality : Speciality.values()) {
                                                        if (speciality == Speciality.None) continue;
                                                %>
                                                <option value="<%= speciality.getValue() %>" <%= (isStylist && ((Stylist)user).getSpeciality() == speciality) ? "selected" : "" %>><%= speciality.toString() %></option>
                                                <% } %>
                                            </select>
                                        </form>
                                    <% } else { %>
                                        <span class="text-muted small">—</span>
                                    <% } %>
                                </td>
                                <td class="pe-4 text-end">
                                    <% if (isStylist) { %>
                                        <button class="btn btn-sm btn-outline-primary px-3" type="submit" form="form-<%= user.getId() %>">
                                            Update
                                        </button>
                                    <% } else if (isCustomer) { %>
                                        <form method="post" action="/admin/users/assign-stylist">
                                            <input type="hidden" name="userId" value="<%= user.getId() %>">
                                            <input type="hidden" name="specialityId" value="5"> <!-- Default to Styling -->
                                            <% if (csrfToken != null) { %>
                                                <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                                            <% } %>
                                            <button class="btn btn-sm btn-primary px-3" type="submit">
                                                Promote
                                            </button>
                                        </form>
                                    <% } else { %>
                                        <span class="text-muted small">—</span>
                                    <% } %>
                                </td>
                            </tr>
                        <%
                                }
                            }
                        %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>

    <script>
    document.addEventListener('DOMContentLoaded', function() {
        const searchInput = document.getElementById('userSearch');
        const roleFilter = document.getElementById('roleFilter');
        const rows = document.querySelectorAll('#userTable tbody tr');

        function filterTable() {
            const searchTerm = searchInput.value.toLowerCase().trim();
            const selectedRole = roleFilter.value.toUpperCase();

            rows.forEach(row => {
                const searchData = row.getAttribute('data-search').toLowerCase();
                const userRole = row.getAttribute('data-role').toUpperCase();

                const matchesSearch = searchData.includes(searchTerm);
                const matchesRole = selectedRole === 'ALL' || userRole === selectedRole;

                if (matchesSearch && matchesRole) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        }

        searchInput.addEventListener('input', filterTable);
        roleFilter.addEventListener('change', filterTable);
    });
    </script>
</body>
</html>
