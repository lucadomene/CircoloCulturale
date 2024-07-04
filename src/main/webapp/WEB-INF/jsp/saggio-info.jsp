<%--
  Created by IntelliJ IDEA.
  User: lucadomeneghetti
  Date: 31/05/2024
  Time: 15:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <title>CircoloCulturale</title>
    <link rel="stylesheet" type="text/css" href="/static/css/style.css"/>
    <script>
        function saggioEnrollButtonAction () {
            let enrollButton = document.getElementById("saggioEnrollButton")
            enrollButton.addEventListener("click", function () {
                window.location.href = ("/saggio/iscrizione?id=" + enrollButton.value)
            })
        }

        window.addEventListener("load", saggioEnrollButtonAction)
    </script>
</head>
<body>
    <%@include file="/static/include/header.jsp"%>
    <div id="main-content">
        <main class="fullsize">
            <section class="title">
                <h1>Informazioni sulla sede</h1>
            </section>
            <section class="content">
                <h1>${saggio.nome}</h1>
                <h2>${saggio.data} dalle ${saggio.orarioInizio} alle ${saggio.orarioFine}</h2>
                <h3>Docenti partecipanti:</h3>
                <ul>
                    <c:forEach items="${saggio.docenti}" var="docente">
                        <li>${docente.socio.utente.nome} ${docente.socio.utente.cognome}</li>
                    </c:forEach>
                </ul>
                <% if ((Integer)request.getAttribute("availableTickets") > 0) { %>
                <p style="color: green">Posti disponibili (${availableTickets})</p>
                <button id="saggioEnrollButton" value="${saggio.id}">Iscriviti</button>
                <% } else { %>
                <p style="color:red">Posti non disponibili</p>
                <button disabled>Iscriviti</button>
                <% } %>
            </section>
        </main>
    </div>
</body>
</html>