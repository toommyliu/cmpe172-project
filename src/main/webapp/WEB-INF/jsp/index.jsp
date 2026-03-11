<!DOCTYPE html>
<html>
<head>
    <title>Home page</title>
</head>
<body>
    <h1><%= request.getAttribute("message") %></h1>
    <p>This is a JSP coming from <b>src/main/webapp/WEB-INF/jsp/index.jsp</b>.</p>
    <p>Time on server: <%= new java.util.Date() %></p>
</body>
</html>
