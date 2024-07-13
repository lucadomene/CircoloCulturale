<%--
  Created by IntelliJ IDEA.
  User: sarac
  Date: 13/07/2024
  Time: 12:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Modifica Sale</title>
    <script>
        var errorDisplayed = false;

        document.addEventListener('DOMContentLoaded', function() {
            initModificaSalaForm();
        });

        function initModificaSalaForm() {
            var modificaSalaForm = document.getElementById('modificaSalaForm');
            if (modificaSalaForm) {
                modificaSalaForm.addEventListener('submit', submitForm);
                var inputs = modificaSalaForm.getElementsByTagName('input');
                addFocusListenersToInputs(inputs, 'modificaSalaForm');
            }
        }
        function addFocusListenersToInputs(inputs, formName) {
            for (var i = 0; i < inputs.length; i++) {
                inputs[i].addEventListener('focus', function() {
                    removeError(formName);
                });
            }
        }

        //campo/i che ha generato l'errore
        var erroredField = "";
        var errorMsg = "";

        function validateForm() {
            var numeroSala = document.getElementById('numero').value;
            var capienza = document.getElementById('capienza').value;

            // Controlla che il numero della sala sia presente e contenga solo numeri
            if (!numeroSala || !/^\d+$/.test(numeroSala)) {
                alert("Il numero della sala deve essere presente e può contenere solo numeri.");
                return false;
            }

            // Controlla che la capienza sia presente e sia un intero positivo
            if (!capienza || !/^\d+$/.test(capienza) || parseInt(capienza, 10) <= 0) {
                alert("La capienza deve essere un numero intero positivo.");
                return false;
            }

            return true; // La validazione è passata
        }

        function submitForm(event) {
            // Impedisci l'invio del form
            event.preventDefault();

            // Chiama la funzione validateForm
            var validation = validateForm();

            if (!validation) {
                // Ottieni l'elemento h1
                var h2Element = document.getElementsByTagName('h2')[0];
                displayErrorMessages(h2Element);
            } else {  // Se la validazione ha esito positivo, invia il form
                // Usa l'ID del form per inviarlo direttamente
                document.getElementById('modificaSalaForm').submit();
            }
        }

        function displayErrorMessages(Element) {

            errorDisplayed = true;
            // Controlla se il messaggio di errore esiste già
            var errorMessageElement = document.getElementById('error-message');
            var specificErrorElement = document.getElementById('specific-error');

            // Se il messaggio di errore non esiste, crealo
            if (!errorMessageElement) {
                errorMessageElement = document.createElement('p');
                errorMessageElement.id = 'error-message';
                errorMessageElement.textContent = "Errore durante l'inserimento, si prega di correggere le informazioni errate.";
                Element.appendChild(errorMessageElement);
            }

            // Se il messaggio di errore specifico non esiste, crealo
            if (!specificErrorElement) {
                specificErrorElement = document.createElement('p');
                specificErrorElement.id = 'specific-error';
                specificErrorElement.textContent = errorMsg;
                Element.appendChild(specificErrorElement);

            }

            // Colora il bordo del campo o dei campi che hanno dato errore
            if(erroredField != "") {
                var fields = erroredField.split(', ');
                for (var i = 0; i < fields.length; i++) {
                    var fieldElement = document.getElementById(fields[i]);
                    if (fieldElement) {
                        fieldElement.style.border = '1px solid red';
                    }
                }
            }
            if(errorMessageElement) {
                scrollToErrorMsg();
            }

        }
        function scrollToErrorMsg() {
            var ErrorMsgElement = document.getElementById('error-message');
            if (ErrorMsgElement) {
                ErrorMsgElement.scrollIntoView({behavior: "smooth"});
            }
        }

        function removeError(formName) {
            if(!errorDisplayed){
                return;
            }
            errorDisplayed=false;
            erroredField="";
            // Rimuovi il messaggio di errore
            var errorMessageElement = document.getElementById('error-message');
            if (errorMessageElement) {
                errorMessageElement.remove();
            }

            // Rimuovi il messaggio di errore specifico
            var specificErrorElement = document.getElementById('specific-error');
            if (specificErrorElement) {
                specificErrorElement.remove();
            }

            // Ottieni tutti gli elementi input del form
            var allInputs = document.getElementById(formName).getElementsByTagName('input');
            // Filtra per ottenere solo gli input di tipo 'text' o number
            var inputs = Array.from(allInputs).filter(function(input) {
                return input.type === 'text' || input.type === 'number';
            });

            // Itera su ogni elemento input
            for (var i = 0; i < inputs.length; i++) {
                // Rimuovi il bordo rosso dal campo
                inputs[i].style.border = '';
            }
        }
    </script>
</head>
<body>
<h2>Modifica Sale</h2>
<% String alreadyPresent;
    if ((alreadyPresent = request.getParameter("alreadyPresent")) != null && alreadyPresent.equals("true")) {
%>
<p>Errore, esiste già una sala con lo stesso numero nella sede con id <%= request.getParameter("idSede") %></p>
<%
    }
%>
<% String fail;
    if ((fail = request.getParameter("fail")) != null && fail.equals("true")) {
%>
<p id="fail">Errore durante la creazione del saggio, verificare le informazioni e riprovare</p>
<script>
    var errorPresentElement = document.getElementById("fail");
    errorPresentElement.scrollIntoView({behavior: "smooth"});
</script>
<%} %>
<c:forEach items="${sale}" var="sala">
    <form id="modificaSaleForm" name="modificaSaleForm" action="/sede/sala/modifica" method="post">
        <input type="hidden" name="idSala" value="${sala.id}" />
        <input type="hidden" name="idSede" value="${sala.idSede.id}" />

        <label for="numeroSala_${sala.id}">Numero Sala:</label>
        <input type="text" id="numeroSala_${sala.id}" name="numero" value="${sala.numeroSala}" required />

        <label for="descrizione_${sala.id}">Descrizione:</label>
        <textarea id="descrizione_${sala.id}" name="descrizione">${sala.descrizione}</textarea>

        <label for="prenotabile_${sala.id}">Prenotabile:</label>
        <select id="prenotabile_${sala.id}" name="prenotabile">
            <option value="true" ${sala.prenotabile ? 'selected' : ''}>Sì</option>
            <option value="false" ${!sala.prenotabile ? 'selected' : ''}>No</option>
        </select>

        <label>Capienza: ${sala.capienza}</label>

        <button type="submit">Salva Modifiche per la sala "${sala.numeroSala}</button>
    </form>
    <hr/>
</c:forEach>
</body>
</html>
