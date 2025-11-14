<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bz.gov.centralbank.models.Account" %>
<%@ page import="bz.gov.centralbank.models.Transaction" %>
<!DOCTYPE html>
<html>
<head>
    <title>Central Bank of Belize - Dashboard</title>
</head>
<body>
<h1>Welcome to the Central Bank of Belize</h1>
<% String message = (String) request.getAttribute("message");
   String error = (String) request.getAttribute("error");
   if (message != null) { %>
    <div style="color:green"><%= message %></div>
<% } else if (error != null) { %>
    <div style="color:red"><%= error %></div>
<% } %>

<h2>Your Accounts</h2>
<%
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    if (accounts != null && !accounts.isEmpty()) {
%>
<table border="1" cellpadding="4" cellspacing="0">
    <tr>
        <th>ID</th>
        <th>Account Number</th>
        <th>Balance</th>
        <th>Currency</th>
    </tr>
    <% for (Account a : accounts) { %>
        <tr>
            <td><%= a.getId() %></td>
            <td><%= a.getAccountNumber() %></td>
            <td><%= a.getBalance() %></td>
            <td><%= a.getCurrency() %></td>
        </tr>
    <% } %>
</table>
<% } else { %>
<p>No accounts found.</p>
<% } %>

<h2>Transfer Funds</h2>
<form method="post" action="<%= request.getContextPath() %>/transfer">
    <label>From Account ID: <input type="number" name="fromAccountId" required></label><br>
    <label>To Account ID: <input type="number" name="toAccountId" required></label><br>
    <label>Amount: <input type="number" name="amount" step="0.01" min="0.01" required></label><br>
    <label>Description: <input type="text" name="description"></label><br>
    <button type="submit">Submit Transfer</button>
</form>

<h2>Recent Transactions</h2>
<%
    List<Transaction> txs = (List<Transaction>) request.getAttribute("transactions");
    if (txs != null && !txs.isEmpty()) {
%>
<table border="1" cellpadding="4" cellspacing="0">
    <tr>
        <th>ID</th>
        <th>Account ID</th>
        <th>Amount</th>
        <th>Type</th>
        <th>When</th>
        <th>Description</th>
    </tr>
    <% for (Transaction t : txs) { %>
        <tr>
            <td><%= t.getId() %></td>
            <td><%= t.getAccountId() %></td>
            <td><%= t.getAmount() %></td>
            <td><%= t.getType() %></td>
            <td><%= t.getCreatedAt() %></td>
            <td><%= t.getDescription() %></td>
        </tr>
    <% } %>
</table>
<% } else { %>
<p>No transactions found.</p>
<% } %>

<form method="post" action="<%= request.getContextPath() %>/logout">
    <button type="submit">Logout</button>
</form>
</body>
</html>
