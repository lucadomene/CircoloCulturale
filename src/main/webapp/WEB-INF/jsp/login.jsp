<%--
  Created by IntelliJ IDEA.
  User: lucadomeneghetti
  Date: 22/06/2024
  Time: 12:11
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Login page</title>
</head>
<body>
    <h1>Please insert your credentials</h1>
    <form name="authForm" method="post" action="login">
        <input type="text" id="cf" name="cf" maxlength="16">
        <label for="cf">Codice fiscale</label>
        <input type="password" id="password" name="password">
        <label for="password">Password</label>
        <input type="submit" name="login" value="Login">
        <% String failed;
        if ((failed = request.getParameter("failed")) != null) {
            if (failed.equals("true")) {%>
        <h2 style="color:red">Codice fiscale o password sbagliati</h2>
        <%  }
        } %>
    </form>
</body>
</html>
