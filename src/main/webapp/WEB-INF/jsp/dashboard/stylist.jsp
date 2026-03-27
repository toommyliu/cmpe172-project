<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.cmpe172.salon.dto.AppointmentDto" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.AvailabilitySlot" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus" %>
<%@ page import="edu.sjsu.cmpe172.salon.enums.AppointmentStatus" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="org.springframework.security.web.csrf.CsrfToken" %>
<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />
<head>
    <title>The Studio - Stylist Dashboard</title>
</head>
<style>
    #availability-scrollspy .nav-link {
        color: var(--bs-secondary);
        border-radius: 0.5rem;
        padding: 0.5rem 1rem;
    }

    #availability-scrollspy .nav-link:hover {
        background-color: var(--bs-light);
    }

    #availability-scrollspy .nav-link.active {
        background-color: var(--bs-primary);
        color: white;
    }

    #availability-scrollspy .nav-link.disabled {
        pointer-events: none;
    }

    [data-bs-spy="scroll"] {
        position: relative;
    }

    .card {
        scroll-margin-top: 6rem;
    }

    .availability-settings-sticky {
        position: sticky;
        top: 0;
        z-index: 1020;
        background-color: var(--bs-tertiary-bg);
        padding-top: 1.5rem; /* Opaque cap to bridge gap to tabs */
        padding-bottom: 2rem;
        transition: none;
    }

    .availability-settings-sticky::after {
        position: absolute;
        bottom: 1rem;
        left: 0;
        right: 0;
        height: 1rem;
        pointer-events: none;
    }

    .dashboard-wrapper {
        display: flex;
        flex-direction: column;
        height: calc(100vh - 56px);
        overflow: hidden;
        background-color: var(--bs-body-bg);
    }

    .dashboard-header {
        flex-shrink: 0;
        background-color: var(--bs-body-bg);
    }

    .dashboard-content {
        flex-grow: 1;
        overflow-y: auto;
        padding-bottom: 3rem;
        background-color: var(--bs-tertiary-bg);
    }

    .tabs-wrapper {
        background-color: var(--bs-tertiary-bg) !important;
        padding-top: 1rem;
    }

    .tabs-inner-container {
        border-bottom: 1px solid var(--bs-border-color);
    }
</style>

<body class="bg-light">
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />

    <div class="dashboard-wrapper">
        <header class="dashboard-header">
            <div class="page-header mb-0">
                <div class="container page-header__container">
                    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-2">
                        <h1 class="h2 page-header__title mb-0">Stylist Dashboard</h1>
                        <%
                            Long upcomingTodayCount = (Long) request.getAttribute("upcomingTodayCount");
                            if (upcomingTodayCount != null && upcomingTodayCount > 0) {
                        %>
                        <div class="d-flex align-items-center bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 rounded-pill px-3 py-1">
                            <span><%= upcomingTodayCount %></span>
                            <span class="ms-1 small">upcoming today</span>
                        </div>
                        <% } %>
                    </div>
                    <p class="page-header__subtitle text-muted mb-3">Manage your schedule and professional availability.</p>
                </div>
            </div>

            <div class="tabs-wrapper">
                <div class="container">
                    <div class="tabs-inner-container">
                        <ul class="nav nav-tabs border-bottom-0">
                            <li class="nav-item">
                                <a class="nav-link active" href="#" data-tab="appointments">Appointments</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#" data-tab="availability">Availability</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </header>

        <main class="dashboard-content" id="dashboard-scroll-area" data-bs-spy="scroll" data-bs-target="#availability-scrollspy" data-bs-offset="100">
            <div class="container pb-4">
        <%
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
            String successMessage = (String) request.getAttribute("successMessage");
            String errorMessage = (String) request.getAttribute("errorMessage");
            String stylistServiceName = (String) request.getAttribute("stylistServiceName");
            Integer stylistServiceDurationMinutes = (Integer) request.getAttribute("stylistServiceDurationMinutes");
            if (stylistServiceDurationMinutes == null || stylistServiceDurationMinutes <= 0) {
                stylistServiceDurationMinutes = 60;
            }
            DateTimeFormatter slotDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a");
            DateTimeFormatter timeOnlyFormatter = DateTimeFormatter.ofPattern("h:mm a");

            if (successMessage != null) {
        %>
        <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
            <%= successMessage %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <%
            }
            if (errorMessage != null) {
        %>
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <%= errorMessage %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <%
            }
        %>

        <div id="appointments-tab-content" data-tab="appointments">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <ul class="nav nav-pills gap-2" id="appointment-subtabs">
                    <li class="nav-item">
                        <button class="btn btn-sm btn-primary px-4 active" data-subtab="upcoming">Upcoming</button>
                    </li>
                    <li class="nav-item">
                        <button class="btn btn-sm btn-outline-secondary px-4" data-subtab="history">History</button>
                    </li>
                </ul>
            </div>

            <div id="upcoming-services-content" data-subtab-content="upcoming">
                <div class="card border-0 mb-5">
                    <div class="card-header bg-white border-bottom py-3">
                        <h2 class="h5 mb-0 fw-bold">Your Upcoming Services</h2>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th class="ps-4">Service</th>
                                        <th>Customer</th>
                                        <th>Slot</th>
                                        <th>Status</th>
                                        <th class="pe-4 text-end">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <%
                                    List<AppointmentDto> upcomingAppointments = (List<AppointmentDto>) request.getAttribute("upcomingAppointments");
                                    if (upcomingAppointments != null && !upcomingAppointments.isEmpty()) {
                                        for (AppointmentDto apt : upcomingAppointments) {
                                            String serviceName = apt.getServiceName();
                                            if (serviceName == null || serviceName.isBlank()) {
                                                serviceName = "Service #" + apt.getServiceId();
                                            }
                                %>
                                    <tr>
                                        <td class="ps-4"><span class="badge bg-primary text-white"><%= serviceName %></span></td>
                                        <td>
                                            <div class="small fw-medium">
                                            <% if (apt.getCustomerName() != null && !apt.getCustomerName().isBlank()) { %>
                                                <%= apt.getCustomerName() %>
                                            <% } else { %>
                                                Customer #<%= apt.getCustomerUserId() %>
                                            <% } %>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="small text-muted">
                                            <% if (apt.getSlotStartDateTime() != null && apt.getSlotEndDateTime() != null) { %>
                                                <%= apt.getSlotStartDateTime().format(slotDateFormatter) %> -
                                                <%= apt.getSlotEndDateTime().format(timeOnlyFormatter) %>
                                            <% } else { %>
                                                Slot <%= apt.getAvailabilitySlotId() %>
                                            <% } %>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="badge bg-primary">Booked</span>
                                        </td>
                                        <td class="pe-4 text-end">
                                            <form method="post" action="/appointments/<%= apt.getId() %>/cancel" class="d-inline">
                                                <% if (csrfToken != null) { %>
                                                    <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                                                <% } %>
                                                <button type="submit" class="btn btn-sm btn-outline-danger" onclick="return confirm('Are you sure you want to cancel this appointment?')">Cancel</button>
                                            </form>
                                        </td>
                                    </tr>
                                <%
                                        }
                                    } else {
                                %>
                                    <tr>
                                        <td colspan="5" class="text-center py-5">
                                            <p class="text-muted mb-0">No upcoming appointments scheduled.</p>
                                        </td>
                                    </tr>
                                <%
                                    }
                                %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div id="history-services-content" class="hidden" data-subtab-content="history">
                <div class="card border-0 mb-5">
                    <div class="card-header bg-white border-bottom py-3">
                        <h2 class="h5 mb-0 fw-bold">Service History</h2>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th class="ps-4">Service</th>
                                        <th>Customer</th>
                                        <th>Slot</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <%
                                    List<AppointmentDto> pastAppointments = (List<AppointmentDto>) request.getAttribute("pastAppointments");
                                    if (pastAppointments != null && !pastAppointments.isEmpty()) {
                                        for (AppointmentDto apt : pastAppointments) {
                                            String serviceName = apt.getServiceName();
                                            if (serviceName == null || serviceName.isBlank()) {
                                                serviceName = "Service #" + apt.getServiceId();
                                            }
                                %>
                                    <tr>
                                        <td class="ps-4"><span class="badge bg-secondary text-white"><%= serviceName %></span></td>
                                        <td>
                                            <div class="small fw-medium">
                                            <% if (apt.getCustomerName() != null && !apt.getCustomerName().isBlank()) { %>
                                                <%= apt.getCustomerName() %>
                                            <% } else { %>
                                                Customer #<%= apt.getCustomerUserId() %>
                                            <% } %>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="small text-muted">
                                            <% if (apt.getSlotStartDateTime() != null) { %>
                                                <%= apt.getSlotStartDateTime().format(slotDateFormatter) %>
                                            <% } else { %>
                                                Slot <%= apt.getAvailabilitySlotId() %>
                                            <% } %>
                                            </div>
                                        </td>
                                        <td>
                                            <%
                                                String aptBadgeClass = "bg-secondary";
                                                if (apt.getStatus() == AppointmentStatus.Complete) {
                                                    aptBadgeClass = "bg-success";
                                                } else if (apt.getStatus() == AppointmentStatus.Canceled) {
                                                    aptBadgeClass = "bg-danger";
                                                }
                                            %>
                                            <span class="badge <%= aptBadgeClass %>"><%= apt.getStatus().toString() %></span>
                                        </td>
                                    </tr>
                                <%
                                        }
                                    } else {
                                %>
                                    <tr>
                                        <td colspan="5" class="text-center py-5">
                                            <p class="text-muted mb-0">No past appointments found.</p>
                                        </td>
                                    </tr>
                                <%
                                    }
                                %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="availability-tab-content" class="hidden" data-tab="availability">
            <div class="row g-4">
                <div class="col-lg-3 d-none d-lg-block">
                    <div class="sticky-top" style="top: 0; z-index: 10; background-color: var(--bs-tertiary-bg); padding-top: 1.5rem;">
                        <nav id="availability-scrollspy" class="nav nav-pills flex-column">
                            <a class="nav-link" href="#create-availability">Create Availability</a>
                            <a class="nav-link ms-3 my-1" href="#one-off-slot">One-Off Slot</a>
                            <a class="nav-link ms-3 my-1" href="#bulk-create">Bulk Create</a>
                            <a class="nav-link" href="#your-slots">Your Slots</a>
                        </nav>
                    </div>
                </div>

                <div class="col-lg-9">
                    <div class="availability-settings-sticky">
                        <div id="create-availability" class="card border-0 mb-0">
                            <div class="card-header bg-white border-bottom py-3">
                                <h2 class="h5 mb-0 fw-bold">Availability Settings</h2>
                            </div>
                            <div class="card-body">
                                <p class="text-muted mb-0">
                                    Assigned Service: <strong><%= stylistServiceName == null ? "Not Assigned" : stylistServiceName %></strong>
                                    · Base Timing: <strong><%= stylistServiceDurationMinutes %> minutes</strong>
                                </p>
                            </div>
                        </div>
                    </div>

                    <div id="one-off-slot" class="card border-0 mb-4">
                        <div class="card-header bg-white border-bottom py-3">
                            <h3 class="h6 mb-0 fw-bold">Add One-Off Slot</h3>
                        </div>
                        <div class="card-body">
                            <form method="post" action="/stylist/availability" class="row g-3">
                                <% if (csrfToken != null) { %>
                                    <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                                <% } %>
                                <div class="col-md-8">
                                    <label for="startDateTime" class="form-label small text-muted">Start Date & Time</label>
                                    <input id="startDateTime" name="startDateTime" type="datetime-local" class="form-control" required>
                                </div>
                                <div class="col-md-4 d-flex align-items-end">
                                    <button type="submit" class="btn btn-primary w-100">Add Slot</button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div id="bulk-create" class="card border-0 mb-4">
                        <div class="card-header bg-white border-bottom py-3">
                            <h3 class="h6 mb-0 fw-bold">Bulk Create Slots</h3>
                        </div>
                        <div class="card-body">
                            <form method="post" action="/stylist/availability/bulk" class="row g-3">
                                <% if (csrfToken != null) { %>
                                    <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                                <% } %>
                                <div class="col-md-6">
                                    <label for="bulkStartDate" class="form-label small text-muted">Start Date</label>
                                    <input id="bulkStartDate" name="startDate" type="date" class="form-control" required>
                                </div>
                                <div class="col-md-6">
                                    <label for="bulkEndDate" class="form-label small text-muted">End Date</label>
                                    <input id="bulkEndDate" name="endDate" type="date" class="form-control" required>
                                </div>
                                <div class="col-md-6">
                                    <label for="bulkDayStartTime" class="form-label small text-muted">Daily Start Time</label>
                                    <input id="bulkDayStartTime" name="dayStartTime" type="time" class="form-control" required>
                                </div>
                                <div class="col-md-6">
                                    <label for="bulkDayEndTime" class="form-label small text-muted">Daily End Time</label>
                                    <input id="bulkDayEndTime" name="dayEndTime" type="time" class="form-control" required>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small text-muted d-block mb-2">Active Weekdays</label>
                                    <div class="d-flex flex-wrap gap-2">
                                        <% String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                                           for (String day : days) { %>
                                            <div class="flex-grow-1 flex-sm-grow-0">
                                                <input type="checkbox" class="btn-check" id="wd<%= day.substring(0,3) %>" name="weekdays" value="<%= day.toUpperCase() %>" autocomplete="off">
                                                <label class="btn btn-outline-primary btn-sm w-100" for="wd<%= day.substring(0,3) %>"><%= day.substring(0,3) %></label>
                                            </div>
                                        <% } %>
                                    </div>
                                </div>
                                <div class="col-12 text-end mt-4">
                                    <button type="submit" class="btn btn-primary px-4">Generate Slots</button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div id="your-slots" class="card border-0 mb-5">
                        <div class="card-header bg-white border-bottom py-3">
                            <h2 class="h5 mb-0 fw-bold">Your Availability Slots</h2>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Start</th>
                                            <th>End</th>
                                            <th>Status</th>
                                            <th class="pe-4 text-end">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            List<AvailabilitySlot> slots = (List<AvailabilitySlot>) request.getAttribute("availabilitySlots");
                                            if (slots != null && !slots.isEmpty()) {
                                                for (AvailabilitySlot slot : slots) {
                                        %>
                                            <tr>
                                                <td><div class="small fw-medium"><%= slot.getStartDateTime().format(slotDateFormatter) %></div></td>
                                                <td><div class="small text-muted"><%= slot.getEndDateTime().format(slotDateFormatter) %></div></td>
                                                <td>
                                                    <%
                                                        String badgeClass = "bg-secondary";
                                                        if (slot.getStatus() == AvailabilitySlotStatus.Available) {
                                                            badgeClass = "bg-success";
                                                        } else if (slot.getStatus() == AvailabilitySlotStatus.Booked) {
                                                            badgeClass = "bg-primary";
                                                        } else if (slot.getStatus() == AvailabilitySlotStatus.Cancelled) {
                                                            badgeClass = "bg-dark";
                                                        }
                                                    %>
                                                    <span class="badge <%= badgeClass %>"><%= slot.getStatus().toString() %></span>
                                                </td>
                                                <td class="pe-4 text-end">
                                                    <% if (slot.getStatus() == AvailabilitySlotStatus.Available) { %>
                                                        <form method="post" action="/stylist/availability/<%= slot.getId() %>/delete" class="d-inline">
                                                            <% if (csrfToken != null) { %>
                                                                <input type="hidden" name="<%= csrfToken.getParameterName() %>" value="<%= csrfToken.getToken() %>">
                                                            <% } %>
                                                            <button type="submit" class="btn btn-sm btn-outline-danger">Cancel Slot</button>
                                                        </form>
                                                    <% } else { %>
                                                        <span class="text-muted small">-</span>
                                                    <% } %>
                                                </td>
                                            </tr>
                                        <%
                                                }
                                            } else {
                                        %>
                                            <tr>
                                                <td colspan="5" class="text-center py-5 text-muted">No availability slots created yet.</td>
                                            </tr>
                                        <%
                                            }
                                        %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
            </div>
        </main>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', () => {
            const tabLinks = document.querySelectorAll('header.dashboard-header ul.nav-tabs .nav-link');
            const tabContents = document.querySelectorAll('div[data-tab]');

            function switchTab(targetTab) {
                tabLinks.forEach(l => {
                    if (l.getAttribute('data-tab') === targetTab) {
                        l.classList.add('active');
                    } else {
                        l.classList.remove('active');
                    }
                });

                tabContents.forEach(content => {
                    if (content.getAttribute('data-tab') === targetTab) {
                        content.classList.remove('hidden');
                    } else {
                        content.classList.add('hidden');
                    }
                });

                // Update URL without reloading
                const url = new URL(window.location.href);
                url.searchParams.set('tab', targetTab);
                window.history.pushState({path: url.href}, '', url.href);

                // Refresh Scrollspy if availability is active
                if (targetTab === 'availability') {
                    setTimeout(() => {
                        const scrollArea = document.getElementById('dashboard-scroll-area');
                        const scrollspyInstance = bootstrap.ScrollSpy.getInstance(scrollArea);
                        if (scrollspyInstance) {
                            scrollspyInstance.refresh();
                        }
                    }, 100);
                }
            }

            const urlParams = new URLSearchParams(window.location.search);
            const initialTab = urlParams.get('tab') || 'appointments';
            switchTab(initialTab);

            tabLinks.forEach(link => {
                link.addEventListener('click', (ev) => {
                    ev.preventDefault();
                    const targetTab = link.getAttribute('data-tab');
                    switchTab(targetTab);
                    document.getElementById('dashboard-scroll-area').scrollTop = 0;
                });
            });

            const subTabButtons = document.querySelectorAll('button[data-subtab]');
            const subTabContents = document.querySelectorAll('div[data-subtab-content]');

            function switchSubTab(targetSubTab) {
                subTabButtons.forEach(btn => {
                    if (btn.getAttribute('data-subtab') === targetSubTab) {
                        btn.classList.add('active', 'btn-primary');
                        btn.classList.remove('btn-outline-secondary');
                    } else {
                        btn.classList.remove('active', 'btn-primary');
                        btn.classList.add('btn-outline-secondary');
                    }
                });

                subTabContents.forEach(content => {
                    if (content.getAttribute('data-subtab-content') === targetSubTab) {
                        content.classList.remove('hidden');
                    } else {
                        content.classList.add('hidden');
                    }
                });
            }

            subTabButtons.forEach(btn => {
                btn.addEventListener('click', () => {
                    switchSubTab(btn.getAttribute('data-subtab'));
                });
            });

            const startInput = document.getElementById('startDateTime');
            const bulkStartDateInput = document.getElementById('bulkStartDate');
            const bulkEndDateInput = document.getElementById('bulkEndDate');
            const weekdayInputs = document.querySelectorAll('input[name="weekdays"]');

            const now = new Date();
            now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
            if (startInput) {
                const minDateTimeValue = now.toISOString().slice(0, 16);
                startInput.min = minDateTimeValue;
            }

            if (bulkStartDateInput && bulkEndDateInput) {
                const minDateValue = now.toISOString().slice(0, 10);
                bulkStartDateInput.min = minDateValue;
                bulkEndDateInput.min = minDateValue;
                bulkStartDateInput.addEventListener('change', function() {
                    if (bulkStartDateInput.value) {
                        bulkEndDateInput.min = bulkStartDateInput.value;
                    }
                });
            }

            weekdayInputs.forEach(function(input) {
                input.addEventListener('change', function() {
                    updateWeekdayRequirement();
                });
            });

            function updateWeekdayRequirement() {
                const selected = Array.from(weekdayInputs).some(function(el) { return el.checked; });
                weekdayInputs.forEach(function(el) {
                    el.required = false;
                    el.setCustomValidity('');
                });
                if (!selected && weekdayInputs.length > 0) {
                    weekdayInputs[0].required = true;
                    weekdayInputs[0].setCustomValidity('Select at least one weekday.');
                }
            }

            updateWeekdayRequirement();
        });
    </script>
</body>
</html>
