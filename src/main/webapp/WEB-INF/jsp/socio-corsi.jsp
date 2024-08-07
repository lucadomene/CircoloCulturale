<%--
  Created by IntelliJ IDEA.
  User: sarac
  Date: 02/07/2024
  Time: 20:30
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="it.unife.cavicchidome.CircoloCulturale.models.Socio" %>
<!DOCTYPE html>
<html>
<head>
    <title>Circolo La Sinfonia</title>
    <link href="/static/css/style.css" rel="stylesheet" type="text/css"/>

</head>
<body>
<%@ include file="/static/include/header.jsp" %>
<div id="main-content" class="clearfix">
    <main class="midleft">
        <section class="title">
            <h1>Corsi di ${socioHeader.utente.nome} ${socioHeader.utente.cognome}</h1>
        </section>
        <section class="clearfix content">
        <c:if test="${corsi.size() < 1}"><p id="emptyset">Nessun corso da mostrare</p></c:if>
            <c:forEach items="${corsi}" var="corso">
                <article class="full">
                    <h1><a href="/corso/info?id=${corso.id}">${corso.categoria} ${corso.genere} ${corso.livello}</a></h1>
                    <h2>${corso.descrizione}</h2>
                </article>
            </c:forEach>
        </section>
    </main>
    <%@include file="/static/include/aside.jsp"%>
</div>
<%@include file="/static/include/footer.jsp"%>
</body>
</html>
