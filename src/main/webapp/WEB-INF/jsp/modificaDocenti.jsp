<%--
  Created by IntelliJ IDEA.
  User: sarac
  Date: 07/07/2024
  Time: 15:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Modifica Docenti</title>
    <script>
        var errorDisplayed = false;
        var errorMsg = "";
        function redirectToEditCorsoPage() {
            var courseId = '${corso.id}';
            window.location.href = '/corso/modificaBase?idCorso=' + courseId;
        }
        function redirectToEditCalendarioPage() {
            var courseId = '${corso.id}';
            window.location.href = '/corso/modificaCalendario?idCorso=' + courseId;
        }

        document.addEventListener('DOMContentLoaded', function() {
            initForm();
            gestisciStipendiDocenti();
            var fail = "${param.fail}";
            if (fail == 'true') {
                scrollToErrorMsg();
            }
            var docentiOverlap = "${param.docentiOverlap}";
            if (docentiOverlap == 'true') {
                warningDocentiOverlap();
            }
        });

        function warningDocentiOverlap() {
            alert("Attenzione: sono stati rilevati problemi di sovrapposizione oraria nell'orario dei docenti. Se è previsto e non un errore, si prega di selezionare nuovamente i dati e confermare.");
        }

        function scrollToErrorMsg() {
            var ErrorMsgElement = document.getElementById('ErroreMsg');
            if (ErrorMsgElement) {
                ErrorMsgElement.scrollIntoView({behavior: "smooth"});
            }
        }

        function gestisciStipendiDocenti() {
            var selectDocenti = document.getElementsByName('nuoviDocenti')[0];
            var docentiStipendiContainer = document.getElementById('containerStipendi');
            docentiStipendiContainer.innerHTML = ''; // Clears the current list

            Array.from(selectDocenti.selectedOptions).forEach(function(option) {
                var docenteDiv = document.createElement('div');
                var docenteLabel = document.createElement('label');
                docenteLabel.textContent = 'Stipendio per: ' + option.text;

                var docenteStipendio = document.createElement('input');
                docenteStipendio.setAttribute('type', 'number');
                docenteStipendio.setAttribute('title', 'Per favore inserire un importo corretto senza cifre decimali.');
                docenteStipendio.setAttribute('name', 'stipendi');
                docenteStipendio.setAttribute('required', '');
                docenteStipendio.setAttribute('min', '10000');
                docenteStipendio.setAttribute('max', '100000');

                docenteDiv.appendChild(docenteLabel);
                docenteDiv.appendChild(docenteStipendio);
                docentiStipendiContainer.appendChild(docenteDiv);
            });
        }


        function initForm() {
            var Form = document.getElementById('modificaDocentiForm');
            if (Form) {
                Form.addEventListener('submit', submitForm);
                var inputs = Form.getElementsByTagName('input');
                addFocusListenersToInputs(inputs, 'modificaDocentiForm');
            }
        }
        function addFocusListenersToInputs(inputs, formName) {
            for (var i = 0; i < inputs.length; i++) {
                inputs[i].addEventListener('focus', function() {
                    removeError(formName);
                });
            }
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
                document.getElementById('modificaDocentiForm').submit();
            }
        }

        function validateForm(){
            /*if(!validateInstructorSelection()){
                return false;
            }*/
            var stipendiInputs = document.querySelectorAll('#containerStipendi input[type="number"]');
            if(!validateSalaryRange(stipendiInputs)){
                return false;
            }
            stipendiInputs = document.getElementsByName('stipendiAttuali');
            if(!validateSalaryRange(stipendiInputs)){
                return false;
            }

            return true;
        }
        function validateSalaryRange(stipendiInputs) {
            for (var i = 0; i < stipendiInputs.length; i++) {
                var stipendio = parseInt(stipendiInputs[i].value, 10);
                if (isNaN(stipendio) || stipendio < 10000 || stipendio > 100000) {
                    errorMsg = "Lo stipendio deve essere tra 10000 e 100000.";
                    return false;
                }
            }
            return true;
        }

        /*function validateInstructorSelection() {
            var selectDocenti = document.getElementsByName('nuoviDocenti')[0];

            if (selectDocenti.selectedOptions.length === 0) {
                errorMsg="Selezionare almeno un docente";
                return false;
            }
            return true;
        }
         */

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


        }
    </script>
</head>
<body>
<h2>Docenti Correnti</h2>
<% String fail;
    if ((fail = request.getParameter("fail")) != null && fail.equals("true")) {
%>
<p>Errore durante la modifica del corso, verificare le informazioni e riprovare</p>
<%
    }
%>
<form id="modificaDocentiForm" action="modificaDocenti" method="post">
    <input type="hidden" name="idCorso" value="${corso.id}"/>
    <input type="hidden" name="docentiOverlap" value="<%= request.getParameter("docentiOverlap") != null ? request.getParameter("docentiOverlap") : "null" %>">
    <p>Seleziona i docenti da eliminare<p>
    <c:forEach var="docente" items="${docentiCorso}">
        <div>
            <input type="checkbox" id="docentiDaEliminare" name="docentiDaEliminare" value="${docente.id}" />
            <span>${docente.socio.utente.nome} ${docente.socio.utente.cognome} (${docente.socio.utente.cf}): </span>
    <input type="number" name="stipendiAttuali" value="${docente.stipendio}" title="Per favore inserire un importo corretto senza cifre decimali" required/>
        </div>
    </c:forEach>
    <p>Attenzione, gli stipendi dei docenti verranno aggiornati solamente nel caso l'importo risulti superiore a quello attualmente percepito</p>
    <h2>Aggiungi Nuovi Docenti</h2>
    <select name="nuoviDocenti" multiple="multiple" onchange="gestisciStipendiDocenti()">
        <c:forEach var="socio" items="${sociInfo}">
            <option value="${socio[0]}">${socio[1]} ${socio[2]} (${socio[0]})</option>
        </c:forEach>
    </select>
    <div id="containerStipendi"></div>
    <!-- <label for="stipendiNuoviDocenti">Inserisci gli stipendi per i nuovi docenti (separati da virgola, nell'ordine di selezione):</label>
    <input type="text" id="stipendiNuoviDocenti" name="stipendiNuoviDocenti" placeholder="Esempio: 1500,1200,1300" /> -->

    <button type="submit">Salva Modifiche</button>
</form>
<button type="button" onclick="redirectToEditCalendarioPage()">Modifica Calendario e Sala Corso</button>
<button type="button" onclick="redirectToEditCorsoPage()">Torna alla pagina modifica corso</button>

</body>
</html>
