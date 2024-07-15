<%--
  Created by IntelliJ IDEA.
  User: lucadomeneghetti
  Date: 07/07/2024
  Time: 08:22
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Circolo Culturale</title>
    <link href="/static/css/style.css" rel="stylesheet" type="text/css">
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            var docentiOverlap = "${param.docentiOverlap}";
            if (docentiOverlap == 'true') {
                warningDocentiOverlap();
            }

        });

        function warningDocentiOverlap() {
            alert("Attenzione: le informazioni sono state salvate ma sono stati rilevati problemi di sovrapposizione oraria nell'orario dei docenti.");
        }
    </script>
</head>
<body>
    <%@include file="/static/include/header.jsp"%>
    <div id="main-content">
        <main class="midleft">
            <section class="title">
                <h1>Corsi del circolo</h1>
            </section>
            <section class="filter">
                <form action="/segretario/corsi" method="get">
                    <label for="categoria">Categoria</label>
                    <select name="categoria" id="categoria">
                        <option label="---"></option>
                        <c:forEach items="${categorie}" var="categoria">
                            <option value="${categoria}" <c:if test="${param.categoria == categoria}">selected</c:if> >${categoria}</option>
                        </c:forEach>
                    </select>
                    <label for="genere">Genere</label>
                    <select name="genere" id="genere">
                        <option label="---"></option>
                        <c:forEach items="${generi}" var="genere">
                            <option value="${genere}" <c:if test="${param.genere == genere}">selected</c:if> >${genere}</option>
                        </c:forEach>
                    </select>
                    <label for="livello">Livello</label>
                    <select name="livello" id="livello">
                        <option label="---"></option>
                        <c:forEach items="${livelli}" var="livello">
                            <option value="${livello}" <c:if test="${param.livello == livello}">selected</c:if>>${livello}</option>
                        </c:forEach>
                    </select>
                    <label for="active">Mostra corsi cancellati</label>
                    <input type="checkbox" name="active" id="active" value="true" <c:if test="${param.active == true}">checked</c:if>>
                    <input type="submit" value="Filtra">
                </form>
            </section>
            <section class="content clearfix">
                <c:forEach items="${corsi}" var="corso">
                    <article>
                        <h1><a href="/corso/modificaBase?idCorso=${corso.id}">${corso.categoria} ${corso.genere} ${corso.livello}</a></h1>
                        <h2>${corso.descrizione}</h2>
                    </article>
                </c:forEach>
            </section>
        </main>
        <%@include file="/static/include/aside.jsp"%>
    </div>
</body>
</html>