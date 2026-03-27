<%@ page import="edu.sjsu.cmpe172.salon.model.ProviderWeeklyHours" %>
<%@ page import="edu.sjsu.cmpe172.salon.model.ProviderDateOverride" %>
<%@ page import="java.time.DayOfWeek" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.format.TextStyle" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>

<!DOCTYPE html>
<html lang="en">
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<body>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp" />
    <main class="container d-flex justify-content-center align-items-center" style="height: calc(100vh - 60px);">
        <div class="text-center">
            <div class="mx-auto mb-5" style="max-width: 320px;">
                <div class="d-flex align-items-center justify-content-center mb-3 opacity-50">
                    <hr class="flex-grow-1 m-0">
                    <span class="mx-3 small">Business hours</span>
                    <hr class="flex-grow-1 m-0">
                </div>

                <div class="small">
                    <%
                        Map<DayOfWeek, ProviderWeeklyHours> weeklyHours =
                            (Map<DayOfWeek, ProviderWeeklyHours>) request.getAttribute("weeklyHours");
                        List<ProviderDateOverride> dateOverrides =
                            (List<ProviderDateOverride>) request.getAttribute("dateOverrides");
                        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E, MMM d");

                        if (weeklyHours != null) {
                            DayOfWeek[] days = DayOfWeek.values();
                            int i = 0;
                            while (i < days.length) {
                                int start = i;
                                ProviderWeeklyHours currentHours = weeklyHours.get(days[start]);
                                while (i + 1 < days.length) {
                                    ProviderWeeklyHours nextHours = weeklyHours.get(days[i+1]);
                                    boolean currentClosed = currentHours == null || currentHours.isClosed();
                                    boolean nextClosed = nextHours == null || nextHours.isClosed();

                                    boolean same = false;
                                    if (currentClosed && nextClosed) {
                                        same = true;
                                    } else if (!currentClosed && !nextClosed) {
                                        same = currentHours.getOpenTime().equals(nextHours.getOpenTime()) &&
                                               currentHours.getCloseTime().equals(nextHours.getCloseTime());
                                    }

                                    if (same) i++;
                                    else break;
                                }

                                String dayRange = (start == i) ?
                                    days[start].getDisplayName(TextStyle.SHORT, Locale.ENGLISH) :
                                    days[start].getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " - " +
                                    days[i].getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

                                String hoursStr = (currentHours == null || currentHours.isClosed()) ?
                                    "<span class='text-danger opacity-50 text-lowercase'>closed</span>" :
                                    "<span class='text-body-emphasis'>" + currentHours.getOpenTime().format(timeFormatter) + " - " +
                                    currentHours.getCloseTime().format(timeFormatter) + "</span>";
                    %>
                        <div class="d-flex justify-content-between py-1">
                            <span class="text-body-secondary"><%= dayRange %></span>
                            <%= hoursStr %>
                        </div>
                    <%
                                i++;
                            }
                        } else {
                    %>
                        <p class="text-muted text-center py-2">Hours unavailable.</p>
                    <% } %>

                    <% if (dateOverrides != null && !dateOverrides.isEmpty()) {
                        LocalDate today = LocalDate.now();
                        List<ProviderDateOverride> upcoming = dateOverrides.stream()
                            .filter(o -> !o.getOverrideDate().isBefore(today))
                            .limit(3)
                            .toList();

                        if (!upcoming.isEmpty()) {
                    %>
                        <div class="d-flex align-items-center justify-content-center mt-4 mb-2 opacity-50">
                            <span class="mx-3 small" style="font-size: 0.75rem;">Special hours</span>
                        </div>
                        <% for (ProviderDateOverride override : upcoming) { %>
                            <div class="d-flex justify-content-between py-1">
                                <span class="text-body-secondary"><%= override.getOverrideDate().format(dateFormatter) %></span>
                                <% if (override.isClosed()) { %>
                                    <span class="text-danger opacity-50 text-lowercase">closed</span>
                                <% } else { %>
                                    <span class="text-body-emphasis small text-lowercase"><%= override.getOpenTime().format(timeFormatter) %> - <%= override.getCloseTime().format(timeFormatter) %></span>
                                <% } %>
                            </div>
                        <% } %>
                    <% }
                    } %>
                </div>
            </div>

            <div class="d-grid gap-3 d-sm-flex justify-content-sm-center">
                <% if (request.getUserPrincipal() == null) { %>
                    <a href="/login" class="btn btn-primary px-4">Login</a>
                    <a href="/register" class="btn btn-outline-secondary px-4">Create Account</a>
                <% } else { %>
                    <a href="/dashboard" class="btn btn-primary px-4">Go to Dashboard</a>
                <% } %>
            </div>
        </div>
    </main>
</body>

</html>
