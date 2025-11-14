<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Error - Central Bank of Belize</title>
</head>
<body>
<h1>An error occurred</h1>
<p><%= request.getAttribute("error") %></p>
</body>
</html>
