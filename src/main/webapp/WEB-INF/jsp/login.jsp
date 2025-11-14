<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Central Bank of Belize - Login</title>
</head>
<body>
<h1>Central Bank of Belize - Secure Login</h1>
<% String error = (String) request.getAttribute("error");
   if (error != null) { %>
    <div style="color:red"><%= error %></div>
<% } %>
<form method="post" action="<%= request.getContextPath() %>/login">
    <label>Username: <input type="text" name="username" required></label><br>
    <label>Password: <input type="password" name="password" required></label><br>
    <button type="submit">Login</button>
</form>
</body>
</html>
