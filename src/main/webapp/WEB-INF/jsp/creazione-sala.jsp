<%--
  Created by IntelliJ IDEA.
  User: sarac
  Date: 13/07/2024
  Time: 11:13
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Circolo La Sinfonia</title>
    <link href="/static/css/style.css" rel="stylesheet" type="text/css">
    <script>
        var errorDisplayed = false;

        function redirectToEditSedePage() {
            var idSede = '<%= request.getParameter("idSede") %>';
            window.location.href = '/sede/modifica?idSede=' + idSede;
        }

        document.addEventListener('DOMContentLoaded', function() {
            initCreaSalaForm();
        });

        function initCreaSalaForm() {
            var creaSalaForm = document.getElementById('creaSalaForm');
            if (creaSalaForm) {
                creaSalaForm.addEventListener('submit', submitForm);
                var inputs = creaSalaForm.querySelectorAll('input, select, textarea');
                addFocusListenersToInputs(inputs, 'creaSalaForm');
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
            var descrizione = document.getElementById('descrizione').value;

            var charDescrizioneRegex = /^(?=.*[A-Za-z])[A-Za-z\s\'\-\(\)\.\,\;\:\!\?\[\]\{\}\"\-àèéìòùÀÈÉÌÒÙáéíóúÁÉÍÓÚâêîôûÂÊÎÔÛäëïöüÿÄËÏÖÜŸ]+$/;
            /* almeno un carattere alfabetico (maiuscolo o minuscolo) e possono includere spazi, apostrofi, trattini e, nel caso di charDescrizioneRegex,
             anche parentesi, punti, virgole, punto e virgola, due punti, punti esclamativi, punti interrogativi, parentesi quadre, parentesi graffe, e virgolette.
             Anche lettere accentate
             */

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

            // Optional fields validation
            if (descrizione && !descrizione.match(charDescrizioneRegex)) {
                errorMsg = "Descrizione deve contenere solo lettere, spazi o trattini";
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

                displayErrorMessages();
            } else {  // Se la validazione ha esito positivo, invia il form
                // Usa l'ID del form per inviarlo direttamente
                document.getElementById('creaSalaForm').submit();
            }
        }

        function displayErrorMessages() {
            formElement=document.getElementById('creaSalaForm');
            errorDisplayed = true;
            // Controlla se il messaggio di errore esiste già
            var errorMessageElement = document.getElementById('error-message');
            var specificErrorElement = document.getElementById('specific-error');

            // Se il messaggio di errore non esiste, crealo
            if (!errorMessageElement) {
                errorMessageElement = document.createElement('p');
                errorMessageElement.id = 'error-message';
                errorMessageElement.textContent = "Errore durante l'inserimento, si prega di correggere le informazioni errate.";
                document.querySelector('.content').insertBefore(errorMessageElement, formElement);
            }

            // Se il messaggio di errore specifico non esiste, crealo
            if (!specificErrorElement) {
                specificErrorElement = document.createElement('p');
                specificErrorElement.id = 'specific-error';
                specificErrorElement.textContent = errorMsg;
                document.querySelector('.content').insertBefore(specificErrorElement, formElement);

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
<%@ include file="/static/include/header.jsp" %>
<div id="main-content" class="clearfix">
    <main class="midleft">
        <section class="title">
            <h1>Crea una nuova Sala</h1>
        </section>
        <section class="content">
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
        <form id="creaSalaForm" action="/sede/sala/crea" method="post">
            <fieldset>
            <legend>Dettagli Sala</legend>
            <input type="hidden" name="idSede" value="<%= request.getParameter("idSede") %>">

            <label for="numero">Numero Sala:</label>
            <input type="text" id="numero" name="numero" required pattern="\d+" title="Solo numeri sono permessi."><br>

            <label for="capienza">Capienza:</label>
            <input type="number" id="capienza" name="capienza" required><br>

            <label for="descrizione">Descrizione (Opzionale):</label>
            <textarea id="descrizione" name="descrizione"></textarea><br>

            <label for="prenotabile">Prenotabile:</label>
            <input type="checkbox" id="prenotabile" name="prenotabile">

            <button type="submit">Crea Sala</button>
            </fieldset>
        </form>
        </section>
        <section class="content">
            <button type="button" onclick="redirectToEditSedePage()">Modifica Sede</button>
        </section>
</main>
<%@include file="/static/include/aside.jsp"%>
</div>
<%@include file="/static/include/footer.jsp"%>
</body>
</html>