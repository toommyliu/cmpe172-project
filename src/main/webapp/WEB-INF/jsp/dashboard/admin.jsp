<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.DayOfWeek" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Service" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Provider" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.ProviderWeeklyHours" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.ProviderDateOverride" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.User" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.Stylist" %>
<%@ page import="org.springframework.security.web.csrf.CsrfToken" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <div class="page-header">
        <div class="container page-header__container">
            <h1 class="h2 page-header__title">Admin Dashboard</h1>
            <p class="page-header__subtitle text-muted">Manage the business, services, and users.</p>
        </div>
    </div>

    <main class="container pb-5">
        <%
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
            String successMessage = (String) request.getAttribute("successMessage");
            String errorMessage = (String) request.getAttribute("errorMessage");
            Provider provider = (Provider) request.getAttribute("provider");
            if (provider == null) {
                provider = new Provider();
                provider.setId(1);
            }
            Map<DayOfWeek, ProviderWeeklyHours> weeklyHoursByDay = (Map<DayOfWeek, ProviderWeeklyHours>) request.getAttribute("weeklyHoursByDay");
            List<ProviderDateOverride> dateOverrides = (List<ProviderDateOverride>) request.getAttribute("dateOverrides");
            DateTimeFormatter inputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter displayTimeFormatter = DateTimeFormatter.ofPattern("h:mm a");
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

        <div>
            <ul class="nav nav-tabs">
                <li class="nav-item">
                    <a class="nav-link active" aria-current="page" data-tab="provider">Provider</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#" data-tab="services">Services</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#" data-tab="users">Users</a>
                </li>
            </ul>
        </div>

        <div class="card border-0 mb-4" data-tab="provider">
            <div class="card-header bg-white border-bottom py-3">
                <h2 class="h5 mb-0 fw-bold">Provider Information</h2>
            </div>
            <div class="card-body">
                <form method="post" action="/admin/provider" class="row g-3">
                    <input type="hidden" name="id" value="<%= provider.getId() > 0 ? provider.getId() : 1 %>">
                    <% if (csrfToken != null) { %>
                        <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                    <% } %>
                    <div class="col-12">
                        <label for="providerName" class="form-label">Name</label>
                        <input id="providerName" type="text" name="name" class="form-control" value="<%= provider.getName() == null ? "" : provider.getName() %>" required>
                    </div>
                    <div class="col-12">
                        <label for="providerAddress" class="form-label">Address</label>
                        <input id="providerAddress" type="text" name="address" class="form-control" value="<%= provider.getAddress() == null ? "" : provider.getAddress() %>">
                    </div>
                    <div class="col-md-6">
                        <label for="providerPhone" class="form-label">Phone Number</label>
                        <input id="providerPhone" type="text" name="phoneNumber" class="form-control" value="<%= provider.getPhoneNumber() == null ? "" : provider.getPhoneNumber() %>">
                    </div>
                    <div class="col-md-6">
                        <label for="providerEmail" class="form-label">Email Address</label>
                        <input id="providerEmail" type="email" name="emailAddress" class="form-control" value="<%= provider.getEmailAddress() == null ? "" : provider.getEmailAddress() %>">
                    </div>
                    <div class="col-12 text-end">
                        <button class="btn btn-primary" type="submit">Save Provider Profile</button>
                    </div>
                </form>
            </div>
        </div>

        <div class="card border-0 mb-4" data-tab="provider">
            <div class="card-header bg-white border-bottom py-3">
                <h2 class="h5 mb-0 fw-bold">Weekly Hours</h2>
            </div>
            <div class="card-body">
                <form method="post" action="/admin/provider/weekly-hours">
                    <input type="hidden" name="id" value="<%= provider.getId() > 0 ? provider.getId() : 1 %>">
                    <% if (csrfToken != null) { %>
                        <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                    <% } %>

                    <div class="table-responsive">
                        <table class="table align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th>Day</th>
                                    <th>Closed</th>
                                    <th>Open</th>
                                    <th>Close</th>
                                </tr>
                            </thead>
                            <tbody>
                            <%
                                for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                                    ProviderWeeklyHours hours = weeklyHoursByDay == null ? null : weeklyHoursByDay.get(dayOfWeek);
                                    boolean closed = hours == null || hours.isClosed();
                                    String openValue = (hours == null || hours.getOpenTime() == null) ? "" : hours.getOpenTime().format(inputTimeFormatter);
                                    String closeValue = (hours == null || hours.getCloseTime() == null) ? "" : hours.getCloseTime().format(inputTimeFormatter);
                                    String dayLabel = switch (dayOfWeek) {
                                        case MONDAY -> "Monday";
                                        case TUESDAY -> "Tuesday";
                                        case WEDNESDAY -> "Wednesday";
                                        case THURSDAY -> "Thursday";
                                        case FRIDAY -> "Friday";
                                        case SATURDAY -> "Saturday";
                                        case SUNDAY -> "Sunday";
                                    };
                            %>
                                <tr>
                                    <td class="fw-semibold"><%= dayLabel %></td>
                                    <td>
                                        <div class="form-check">
                                            <input class="form-check-input js-day-closed" type="checkbox" id="closed_<%= dayOfWeek.name() %>" name="closedDays" value="<%= dayOfWeek.name() %>" <%= closed ? "checked" : "" %>>
                                        </div>
                                    </td>
                                    <td>
                                        <input id="openTime_<%= dayOfWeek.name() %>" type="time" name="openTime_<%= dayOfWeek.name() %>" class="form-control" value="<%= openValue %>" <%= closed ? "disabled" : "" %>>
                                    </td>
                                    <td>
                                        <input id="closeTime_<%= dayOfWeek.name() %>" type="time" name="closeTime_<%= dayOfWeek.name() %>" class="form-control" value="<%= closeValue %>" <%= closed ? "disabled" : "" %>>
                                    </td>
                                </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>

                    <div class="text-end mt-3">
                        <button class="btn btn-primary" type="submit">Save Weekly Hours</button>
                    </div>
                </form>
            </div>
        </div>

        <div class="card border-0 mb-4" data-tab="provider">
            <div class="card-header bg-white border-bottom py-3">
                <h2 class="h5 mb-0 fw-bold">Date Overrides</h2>
            </div>
            <div class="card-body">
                <form method="post" action="/admin/provider/date-overrides" class="row g-3 mb-4">
                    <input type="hidden" name="id" value="<%= provider.getId() > 0 ? provider.getId() : 1 %>">
                    <% if (csrfToken != null) { %>
                        <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                    <% } %>
                    <div class="col-md-3">
                        <label for="overrideDate" class="form-label">Date</label>
                        <input id="overrideDate" type="date" name="overrideDate" class="form-control" required>
                    </div>
                    <div class="col-md-3">
                        <label for="overrideMode" class="form-label">Mode</label>
                        <select id="overrideMode" name="overrideMode" class="form-select" required>
                            <option value="CUSTOM" selected>Custom Hours</option>
                            <option value="CLOSED">Closed All Day</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label for="overrideOpenTime" class="form-label">Open Time</label>
                        <input id="overrideOpenTime" type="time" name="openTime" class="form-control" required>
                    </div>
                    <div class="col-md-3">
                        <label for="overrideCloseTime" class="form-label">Close Time</label>
                        <input id="overrideCloseTime" type="time" name="closeTime" class="form-control" required>
                    </div>
                    <div class="col-12 text-end">
                        <button class="btn btn-primary" type="submit">Add Override</button>
                    </div>
                </form>

                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>Date</th>
                                <th>Rule</th>
                                <th class="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <%
                            if (dateOverrides != null && !dateOverrides.isEmpty()) {
                                for (ProviderDateOverride override : dateOverrides) {
                                    String ruleLabel;
                                    if (override.isClosed()) {
                                        ruleLabel = "Closed all day";
                                    } else if (override.getOpenTime() != null && override.getCloseTime() != null) {
                                        ruleLabel = override.getOpenTime().format(displayTimeFormatter) + " - " + override.getCloseTime().format(displayTimeFormatter);
                                    } else {
                                        ruleLabel = "Invalid override";
                                    }
                        %>
                            <tr>
                                <td><%= override.getOverrideDate() %></td>
                                <td><%= ruleLabel %></td>
                                <td class="text-end">
                                    <form method="post" action="/admin/provider/date-overrides/<%= override.getId() %>/delete" class="d-inline">
                                        <input type="hidden" name="id" value="<%= provider.getId() > 0 ? provider.getId() : 1 %>">
                                        <% if (csrfToken != null) { %>
                                            <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                                        <% } %>
                                        <button class="btn btn-sm btn-outline-danger" type="submit">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        <%
                                }
                            } else {
                        %>
                            <tr>
                                <td colspan="3" class="text-muted text-center py-4">No date overrides yet.</td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="hidden" data-tab="services">
            <div class="card-header bg-white border-bottom py-3">
                <h2 class="h5 mb-0 fw-bold">Service Management</h2>
            </div>
            <div class="card-body">
                <p class="text-muted">Service management features coming soon...</p>
            </div>
        </div>

        <div class="hidden card border-0" data-tab="users">
            <div class="card-header bg-white border-bottom py-3">
                <div class="row align-items-center g-3">
                    <div class="col-md-4"></div>
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
                                <th class="text-center">Service</th>
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
                                            <select class="form-select form-select-sm d-inline-block w-auto" name="serviceId" style="min-width: 180px;">
                                                <%
                                                    List<Service> services = (List<Service>) request.getAttribute("services");
                                                    if (services != null) {
                                                        for (Service service : services) {
                                                %>
                                                <option value="<%= service.getId() %>" <%= (isStylist && ((Stylist) user).getServiceId() == service.getId()) ? "selected" : "" %>><%= service.getName() %></option>
                                                <%
                                                        }
                                                    }
                                                %>
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
                                            <select class="form-select form-select-sm d-inline-block w-auto me-2" name="serviceId" style="min-width: 180px;">
                                                <%
                                                    List<Service> services = (List<Service>) request.getAttribute("services");
                                                    if (services != null) {
                                                        for (Service service : services) {
                                                %>
                                                <option value="<%= service.getId() %>"><%= service.getName() %></option>
                                                <%
                                                        }
                                                    }
                                                %>
                                            </select>
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
        document.addEventListener('DOMContentLoaded', () => {
            let activeTab = 'provider';
            const ul = document.querySelector('ul.nav-tabs');
            /**
             * @param {MouseEvent} ev
             */
            const handleTabClick = (ev) => {
                ev.preventDefault();
                const clickedTab = ev.target.closest('a.nav-link');
                if (!clickedTab) {
                    return;
                }
                const tabName = clickedTab.getAttribute('data-tab');
                if (!tabName || tabName === activeTab) {
                    return;
                }
                activeTab = tabName;
                // Show the tab content
                const tabContents = document.querySelectorAll('div[data-tab]');
                for (const el of tabContents) {
                    // Show the active tab, hide others
                    if (el.getAttribute('data-tab') === activeTab) {
                        el.classList.remove('hidden');
                    } else {
                        el.classList.add('hidden');
                    }
                }
            }
            const tabItems = ul.querySelectorAll('a.nav-link');
            for (const li of tabItems) {
                const tabTrigger = new bootstrap.Tab(li)
                li.addEventListener('click', (ev) => {
                    handleTabClick(ev);
                    tabTrigger.show();
                });
            }
        });
        document.addEventListener('DOMContentLoaded', () => {
            const weekdays = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
            weekdays.forEach(day => {
                const closedCheckbox = document.getElementById('closed_' + day);
                const openInput = document.getElementById('openTime_' + day);
                const closeInput = document.getElementById('closeTime_' + day);
                if (!closedCheckbox || !openInput || !closeInput) {
                    return;
                }
                const syncDisabledState = () => {
                    openInput.disabled = closedCheckbox.checked;
                    closeInput.disabled = closedCheckbox.checked;
                };
                syncDisabledState();
                closedCheckbox.addEventListener('change', syncDisabledState);
            });

            const overrideMode = document.getElementById('overrideMode');
            const overrideOpenTime = document.getElementById('overrideOpenTime');
            const overrideCloseTime = document.getElementById('overrideCloseTime');
            const syncOverrideMode = () => {
                if (!overrideMode || !overrideOpenTime || !overrideCloseTime) {
                    return;
                }
                const closedAllDay = overrideMode.value === 'CLOSED';
                overrideOpenTime.disabled = closedAllDay;
                overrideCloseTime.disabled = closedAllDay;
                overrideOpenTime.required = !closedAllDay;
                overrideCloseTime.required = !closedAllDay;
            };
            syncOverrideMode();
            if (overrideMode) {
                overrideMode.addEventListener('change', syncOverrideMode);
            }

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
