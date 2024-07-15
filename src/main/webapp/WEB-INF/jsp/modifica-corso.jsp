<%--
  Created by IntelliJ IDEA.
  User: sarac
  Date: 07/07/2024
  Time: 14:56
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Modifica Corso</title>
    <link href="/static/css/style.css" rel="stylesheet" type="text/css">
    <script>
        function redirectToEditDocentiPage() {
            var courseId = '${corso.id}';
            window.location.href = '/corso/modificaDocenti?idCorso=' + courseId;
        }

        function redirectToEditCalendarioPage() {
            var courseId = '${corso.id}';
            window.location.href = '/corso/modificaCalendario?idCorso=' + courseId;
        }

        var errorDisplayed = false;

        document.addEventListener('DOMContentLoaded', function () {
            initEditCorsoForm();
            handleCancellaCorsoFormSubmission()


        });



        function initEditCorsoForm() {
            var editCorsoForm = document.getElementById('editCorsoForm');
            if (editCorsoForm) {
                editCorsoForm.addEventListener('submit', submitForm);
                var inputs = editCorsoForm.getElementsByTagName('input');
                addFocusListenersToInputs(inputs, 'editCorsoForm');
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
            var charSpaceDashRegex = /^(?=.*[A-Za-z])[A-Za-z\s\'\-]+$/;
            var charDescrizioneRegex = /^(?=.*[A-Za-z])[A-Za-z\s\'\-\(\)\.\,\;\:\!\?\[\]\{\}\"\-]+$/;

            var descrizione = document.getElementById('descrizione').value;
            var genere = document.getElementById('genere').value;
            var livello = document.getElementById('livello').value;
            var categoriaDanza = document.getElementById('danza').checked;
            var categoriaMusica = document.getElementById('musica').checked;


            if (descrizione && !descrizione.match(charDescrizioneRegex)) {
                errorMsg = "Descrizione contiene alcuni caratteri non validi";
                return false;
            }

            if (!genere.match(charSpaceDashRegex) || genere.length > 20 || genere==null || genere=="") {
                erroredField = "genere";
                errorMsg = "Genere contiene alcuni caratteri non validi e deve essere di massimo 20 caratteri. ";
                return false;
            }

            if (!livello.match(charSpaceDashRegex) || livello.length > 20 || livello==null || livello=="") {
                erroredField = "livello";
                errorMsg = "Livello contiene alcuni caratteri non validi e deve essere di massimo 20 caratteri. ";
                return false;
            }

            if (!categoriaDanza && !categoriaMusica) {
                errorMsg = "Selezionare una sola categoria.";
                return false;
            }

            return true;
        }

        function submitForm(event) {
            // Impedisci l'invio del form
            event.preventDefault();

            // Chiama la funzione validateForm
            var validation = validateForm();
            console.log(validation);

            if (!validation) {
                // Ottieni l'elemento h1
                var h1Element = document.getElementsByTagName('h1')[0];
                displayErrorMessages(h1Element);
            } else {  // Se la validazione ha esito positivo, invia il form
                // Usa l'ID del form per inviarlo direttamente
                document.getElementById('editCorsoForm').submit();
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
            if(erroredField != ""){
                var fields = erroredField.split(', ');
                for (var i = 0; i < fields.length; i++) {
                    var fieldElement = document.getElementById(fields[i]);
                    if (fieldElement) {
                        fieldElement.style.border = '1px solid red';
                    }
                }
            }
            if(errorMessageElement){
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
            // Filtra per ottenere solo gli input di tipo 'text'
            var inputs = Array.from(allInputs).filter(function(input) {
                return input.type === 'text';
            });

            // Itera su ogni elemento input
            for (var i = 0; i < inputs.length; i++) {
                // Rimuovi il bordo rosso dal campo
                inputs[i].style.border = '';
            }
        }

        function handleCancellaCorsoFormSubmission() {

            document.getElementById('cancellaCorsoForm').addEventListener('submit', confirmDeletion);
        }

        function confirmDeletion(event) {
            var confirmDeletion = document.getElementById('confirmDeletion');
            if (!confirmDeletion.checked) {
                alert('Per favore, conferma se sei sicuro di voler cancellare il corso.');
                event.preventDefault(); // Prevent form submission
            }
        }

    </script>
</head>
<body>
<%@ include file="/static/include/header.jsp" %>
<div id="main-content">
    <main class="midleft">
        <section class="title">
            <h1>Modifica le informazioni del corso</h1>
        </section>
        <section class="content">
            <% String fail;
                if ((fail = request.getParameter("fail")) != null && fail.equals("true")) {
            %>
            <p>Errore durante la modifica del corso, verificare le informazioni e riprovare</p>
            <%
                }
            %>
            <div>
                <img src="${empty corso.urlFoto ? uploadDir.concat(placeholderImage) : uploadDir.concat(corso.urlFoto)}" alt="Foto Profilo" class="profile-pic"/>
            </div>
            <form id="editCorsoForm" action="modificaBase" method="post">
                <input type="hidden" name="idCorso" value="${corso.id}"/>

                <label for="photo">Seleziona una nuova foto per il corso:</label>
                <input type="file" id="photo" name="photo">

                <label>Descrizione:</label>
                <textarea id="descrizione" name="descrizione" placeholder="Descrizione del corso">${corso.descrizione}</textarea>

                <label>Genere:</label>
                <input type="text" id="genere" name="genere" value="${corso.genere}"/>

                <label>Livello:</label>
                <input type="text" id="livello" name="livello" value="${corso.livello}"/>

                <label>Categoria:</label>
                <input type="radio" id="danza" name="categoria" value="Danza" <c:if test="${corso.categoria == 'Danza'}">checked</c:if> required>
                <label for="danza">Danza</label><br>
                <input type="radio" id="musica" name="categoria" value="Musica" <c:if test="${corso.categoria == 'Musica'}">checked</c:if> required>
                <label for="musica">Musica</label><br>

                <button type="submit">Salva Modifiche</button>
            </form>
        </section>
        <section class="content">
            <button type="button" onclick="redirectToEditDocentiPage()">Modifica Docenti</button>
            <button type="button" onclick="redirectToEditCalendarioPage()">Modifica Calendario e Sala Corso</button>
        </section>
        <section class="content">
            <p>Cancellazione Corso</p>
            <form id="cancellaCorsoForm" action="elimina" method="POST">
                <input type="hidden" name="idCorso" value="${corso.id}" />
                <label for="confirmDeletion">Sei sicuro?</label>
                <input type="checkbox" id="confirmDeletion" name="confirmDeletion" required>
                <button type="submit">Cancella Corso</button>
            </form>
        </section>
    </main>
<%@include file="/static/include/aside.jsp"%>
</div>


</body>
</html>