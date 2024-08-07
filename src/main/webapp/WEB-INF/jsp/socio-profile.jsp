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

    <script>
        var errorDisplayed = false;

        function divideIndirizzo(utente) {
            const parti = utente.indirizzo.split(', ');
            const risultato = {
                state: parti[0],
                province: parti[1],
                city: parti[2],
                street: parti[3],
                houseNumber: parti[4]
            };
            console.log(risultato)
            return risultato;
        }

        document.addEventListener('DOMContentLoaded', function () {
            initializeAddressFields();
            initProfileForm();
            var socioDeleted = '${socio.deleted}';
            var profileForm = 'profileForm';
            var deleteForm = 'deletePhotoForm';
            var modificaPasswordForm = 'modificaPassword';
            if(socioDeleted == false){
                //handleEliminaSocioFormSubmission();
                disableForm(profileForm);
                disableForm(deleteForm);
                disableForm(modificaPasswordForm)
            }else{
                enableEditCheckbox(profileForm);
                enableEditCheckbox(deleteForm);
                enableEditCheckbox(modificaPasswordForm)
            }

            var urlParams = new URLSearchParams(window.location.search);
            var failed = urlParams.get('failed');
            if (failed === "true") {
                scrollToErrorMsg();
            }

        });

        function disableForm(formElement) {
            var inputs = document.getElementById(formElement).getElementsByTagName('input');

            for (var i = 0; i < inputs.length; i++) {
                inputs[i].setAttribute('disabled', 'true');
            }

            var enableEdit = document.getElementById('enableEdit');
            enableEdit.removeAttribute('disabled');

        }
        function enableEditCheckbox(formElement){
            //var submitButton = document.getElementById('profileForm').getElementsByTagName('button')[0];
            //submitButton.setAttribute('disabled', 'true');
            var inputs = document.getElementById(formElement).getElementsByTagName('input');

            for (var i = 0; i < inputs.length; i++) {
                inputs[i].setAttribute('disabled', 'true');
            }
            var enableEdit = document.getElementById('enableEdit');
            enableEdit.removeAttribute('disabled');
            enableEdit.addEventListener('change', function () {
                if (enableEdit.checked) {
                    for (var i = 0; i < inputs.length; i++) {
                        inputs[i].removeAttribute('disabled');
                    }
                } else {
                    for (var i = 0; i < inputs.length; i++) {
                        inputs[i].setAttribute('disabled', 'true');
                    }
                    enableEdit.removeAttribute('disabled');
                    //submitButton.setAttribute('disabled', 'true');
                }
            });
        }

        function initializeAddressFields() {
            var utente = {
                indirizzo: "${socio.utente.indirizzo}"
            };
            var risultato = divideIndirizzo(utente);
            document.getElementById('state').value = risultato.state;
            document.getElementById('province').value = risultato.province;
            document.getElementById('city').value = risultato.city;
            document.getElementById('street').value = risultato.street;
            document.getElementById('houseNumber').value = risultato.houseNumber;
        }

        /*function handleEliminaSocioFormSubmission() {
            document.getElementById('deleteForm').addEventListener('submit', confirmDisiscrizione);
        }*/

        function scrollToErrorMsg() {
            var ErrorMsgElement = document.getElementById('ErroreMsg');
            if (ErrorMsgElement) {
                ErrorMsgElement.scrollIntoView({behavior: "smooth"});
            }
        }
        /*
        function confirmDisiscrizione(event) {
            var confirmUnsubscribe = document.getElementById('confirmUnsubscribe');
            if (confirmUnsubscribe != null && !confirmUnsubscribe.checked) {
                alert('Per favore, conferma se sei sicuro di volerti disiscrivere.');
                event.preventDefault(); // Prevent form submission
            }
        }*/


        function initProfileForm() {
            var profileForm = document.getElementById('profileForm');
            if (profileForm) {
                profileForm.addEventListener('submit', submitForm);
                var inputs = profileForm.getElementsByTagName('input');
                addFocusListenersToInputs(inputs, 'profileForm');
            }
        }

        function addFocusListenersToInputs(inputs, formName) {
            for (var i = 0; i < inputs.length; i++) {
                inputs[i].addEventListener('focus', function () {
                    removeError(formName);
                });
            }
        }

        //campo/i che ha generato l'errore
        var erroredField = "";
        var errorMsg = "";

        function validatePassword() {
            var form = document.modificaPassword;
            if (form.getElementById("old-password") != null) {
                var oldPassword = form.getElementById("old-password").value;
                if (oldPassword == null) {
                    errorMsg = "Inserisci la vecchia password.";
                    erroredField = "old-password";
                    return false;
                }
            }
            var newPassword = form.getElementById("new-password").value;
            if (newPassword == null) {
                errorMsg = "Inserisci una nuova password.";
                erroredField = "new-password";
                return false;
            }
            if (newPassword.length < 8 || newPassword.length > 50) {
                errorMsg = "La password deve essere lunga tra 8 e 50 caratteri.";
                erroredField = "new-password";
                return false;
            }
            var regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,50}$/;
            if (newPassword.match(regex) == null) {
                errorMsg = "La password deve contenere almeno una lettera maiuscola, una lettera minuscola e un numero.";
                erroredField = "new-password";
                return false;
            }
            if (form.getElementById("old-password") != null) {
                var oldPassword = form.getElementById("old-password").value;
                if (oldPassword == newPassword) {
                    errorMsg = "La nuova password non può essere uguale alla vecchia password.";
                    erroredField = "new-password";
                    return false;
                }
            }
        }

        function validateForm() {
            var form = document.profileForm;
            var cf = form.cf.value;
            var name = form.name.value;
            var surname = form.surname.value;
            var dob = form.dob.value;
            var birthplace = form.birthplace.value;
            var state = form.state.value;
            var email = form.email.value;
            var province = form.province.value;
            var city = form.city.value;
            var street = form.street.value;
            var houseNumber = form.houseNumber.value;
            var phoneNumber = form.phoneNumber.value;


            var regex = /^(?=.*[A-Za-z])[A-Za-z\s\'\-àèéìòùÀÈÉÌÒÙáéíóúÁÉÍÓÚâêîôûÂÊÎÔÛäëïöüÿÄËÏÖÜŸ]+$/;
            /* almeno un carattere alfabetico (maiuscolo o minuscolo) e possono includere spazi, apostrofi, trattini.
             Anche lettere accentate
             */
            if (!regex.test(name) || !regex.test(surname) || !regex.test(birthplace) || !regex.test(state) || !regex.test(province) || !regex.test(city) || !regex.test(street)) {
                errorMsg = "I campi nome, cognome, luogo di nascita, stato, provincia, città, via devono contenere solo caratteri e non numeri.";
                erroredField = "name, surname, birthplace, state, province, city, street";
                return false;
            }

            // Controlla se la data di nascita è una data o del giorno corrente o antecedente
            var today = new Date();
            today.setHours(0, 0, 0, 0);
            var inputDate = new Date(dob);
            if (inputDate > today) {
                errorMsg = "La data di nascita deve essere odierna o antecedente.";
                erroredField = "dob";
                return false;
            }

            // Controlla se la somma dei caratteri di stato, provincia, città, via e numero civico non supera gli 80 caratteri
            if ((state.length + province.length + city.length + street.length + houseNumber.length) > 80) {
                errorMsg = "La somma dei caratteri di stato, provincia, città, via e numero civico non deve superare gli 80 caratteri.";
                erroredField = "state, province, city, street, houseNumber";
                return false;
            }

            // Controlla CF
            if (!validateCF(cf)) {
                erroredField = "cf";
                return false;
            }

            // Controlla se l'email è un'email valida
            var emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
            if (!emailRegex.test(email)) {
                errorMsg = "Inserisci un'email valida.";
                errorField = "email";
                return false;
            }


            // Controlla se il numero di telefono contiene solo numeri
            var phoneRegex = /^[0-9]{10}$/;
            if (phoneNumber && phoneNumber!="" && phoneNumber!=null && !phoneRegex.test(phoneNumber)) {
                errorMsg = "Il numero di telefono deve contenere solo 10 numeri.";
                erroredField = "phoneNumber";
                return false;
            }


            // Se tutti i controlli passano, restituisce true per permettere l'invio del form
            return true;
        }

        function validateCF(cf) {
            // Controlla se il codice fiscale è composto sia da numeri che da lettere
            var cfRegex = /^[0-9a-zA-Z]{16}$/;
            if (!cfRegex.test(cf)) {
                errorMsg = "Il codice fiscale deve essere di 16 caratteri e composto sia da numeri che da lettere.";
                return false;
            }
            return true;
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
                document.getElementById('profileForm').submit();
            }
        }

        function displayErrorMessages() {
            var formElement = document.getElementById('profileForm');
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
            var fields = erroredField.split(', ');
            for (var i = 0; i < fields.length; i++) {
                var fieldElement = document.getElementById(fields[i]);
                if (fieldElement) {
                    fieldElement.style.border = '1px solid red';
                }
            }
        }

        function removeError(formName) {
            if (!errorDisplayed) {
                return;
            }
            errorDisplayed = false;
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
            var inputs = document.getElementById(formName).getElementsByTagName('input');

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
            <h1>Profilo di ${socio.utente.nome} ${socio.utente.cognome}</h1>
        </section>
        <section class="content">
            <img class="profile-image" src="${empty socio.urlFoto ? uploadDir.concat(placeholderImage) : uploadDir.concat(socio.urlFoto)}" alt="Foto Profilo" class="profile-pic"/>

            <form id="tesseraform" name="tesseraform" action="/socio/tessera" method="POST">
                <input type="hidden" name="socio-id" value="${socio.id}"/>
                <fieldset>
                    <legend>Informazioni tessera</legend>
                    <label for="tessera-id">Tessera ID:</label>
                    <input type="text" id="tessera-id" name="tessera-id" value="${socio.tessera.id}" readonly>
                    <label for="emissione">Emissione:</label>
                    <input type="date" id="emissione" name="emissione" value="${socio.tessera.dataEmissione}" readonly>
                    <c:if test="${socioHeader.segretario ne null}">
                    <label for="confermato">Confermata</label>
                    <input type="radio" id="confermato" name="confermato" value="true" <%= ((it.unife.cavicchidome.CircoloCulturale.models.Socio)request.getAttribute("socio")).getTessera().getStatoPagamento().equals('c') ? "checked" : ""%>/>
                    <label for="pending">In sospeso</label>
                    <input type="radio" id="pending" name="confermato" value="false" <%= ((it.unife.cavicchidome.CircoloCulturale.models.Socio)request.getAttribute("socio")).getTessera().getStatoPagamento().equals('p') ? "checked" : ""%>/>
                    <input type="submit" value="Conferma"/>
                    </c:if>
                </fieldset>
            </form>

            <form id="deletePhotoForm" action="/socio/deletePhoto" method="POST">
                <input type="hidden" name="socio-id" value="${socio.id}" />
                <input type="submit" value="Cancella Foto Profilo">
            </form>

            <form id="profileForm" name="profileForm" action="/socio/profile" method="POST" enctype="multipart/form-data">
                <input type="hidden" name="socio-id" value="${socio.id}"/>
                <c:if test="${not socio.deleted}">
                <label for="enableEdit">Abilita modifiche</label>
                <input type="checkbox" id="enableEdit" name="enableEdit"/>
                </c:if>
                <fieldset>
                    <legend>Informazioni personali</legend>
                    <label for="name">Nome:</label>
                    <input type="text" id="name" name="name" value="${socio.utente.nome}" placeholder="Nome"/>

                    <label for="surname">Cognome:</label>
                    <input type="text" id="surname" name="surname" value="${socio.utente.cognome}" placeholder="Cognome"/>

                    <label for="email">Email:</label>
                    <input type="email" id="email" name="email" value="${socio.email}" placeholder="Email"/>

                    <label for="phoneNumber">Numero di Telefono:</label>
                    <input type="tel" id="phoneNumber" name="phoneNumber" value="${socio.telefono}"
                           placeholder="Numero di Telefono"/>

                    <label for="cf">Codice Fiscale:</label>
                    <input type="text" id="cf" name="cf" value="${socio.utente.cf}" placeholder="Codice Fiscale"/>

                    <label for="dob">Data di nascita:</label>
                    <input type="date" id="dob" name="dob" value="${socio.utente.dataNascita}" placeholder="Data di nascita"/>

                    <label for="birthplace">Luogo di nascita (città):</label>
                    <input type="text" id="birthplace" name="birthplace" value="${socio.utente.luogoNascita}"
                           placeholder="Luogo di nascita (città)"/>
                </fieldset>

                <fieldset>
                    <legend>Indirizzo</legend>
                    <label for="state">Stato:</label>
                    <input type="text" id="state" name="state" placeholder="Stato"/>

                    <label for="province">Provincia:</label>
                    <input type="text" id="province" name="province" placeholder="Provincia"/>

                    <label for="city">Città:</label>
                    <input type="text" id="city" name="city" placeholder="Città"/>

                    <label for="street">Via:</label>
                    <input type="text" id="street" name="street" placeholder="Via"/>

                    <label for="houseNumber">Numero Civico:</label>
                    <input type="text" id="houseNumber" name="houseNumber" placeholder="Numero Civico"/>
                </fieldset>

                <fieldset>
                    <legend>Altri Dettagli</legend>
                    <label for="photo">Seleziona una nuova Foto Profilo:</label>
                    <input type="file" id="photo" name="photo">
                </fieldset>

                <input type="submit" value="Aggiorna">
            </form>
        </section>

        <c:if test="${socio.docente ne null and socio.docente.active == true and socioHeader.segretario ne null}">
        <section class="content">
            <div class="custom-fieldset">
                <h1 class="custom-legend">Corsi insegnati</h1>
                <ul>
                    <c:forEach items="${socio.docente.corsi}" var="corso">
                        <c:if test="${corso.active == true}">
                            <li><a href="/corso/modificaBase?idCorso=${corso.id}">${corso.genere} ${corso.categoria} ${corso.livello}</a></li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>
        </section>
        </c:if>

        <c:if test="${socio.segretario ne null and socio.segretario.active == true and socioHeader.segretario ne null}">
        <section class="content">
            <div class="custom-fieldset">
                <h1 class="custom-legend">Sede amministrata</h1>
            <a href="/sede/modifica?idSede=${socio.segretario.sedeAmministrata.id}">${socio.segretario.sedeAmministrata.nome}</a>
            </div>
        </section>
        </c:if>

        <!-- TODO: cambia gestione inattivo e attivo -->
        <!-- TODO: aggiungere possibilita` di confermare o meno tessera -->
        <c:if test="${not socio.deleted}">
            <section class="content">
                <form name="modificaPassword" id="modificaPassword" action="modificaPassword" method="POST">
                    <input type="hidden" name="socio-id" value="${socio.id}"/>
                    <% if (request.getAttribute("socioHeader") != null && ((Socio)request.getAttribute("socioHeader")).getSegretario() == null) { %>
                    <label for="old-password">Vecchia password</label><input type="password" id="old-password" name="old-password" required/> <% } %>
                    <label for="new-password">Nuova password</label><input type="password" id="new-password" name="new-password" required/>
                    <input type="submit" value="Modifica Password">
                </form>
            </section>
        </c:if>
            <section class="content">
                <div class="custom-fieldset">
                    <h1 class="custom-legend">Disiscrizione dal Circolo Culturale</h1>
                    <form name="deleteForm" id="deleteForm" action="elimina" method="POST">
                        <input type="hidden" name="socio-id" value="${socio.id}"/>
                        <% if (((Socio)request.getAttribute("socioHeader")).getSegretario() == null) { %>
                        <label for="confirmUnsubscribe">Confermi la disiscrizione?</label>
                        <input type="checkbox" id="confirmUnsubscribe" name="confirmUnsubscribe" required>
                        <button type="submit">Disiscriviti</button>
                        <% } else { %>
                        <label for="not-deleted">Attivo</label><input type="radio" id="not-deleted" name="delete" value="false" <c:if test="${socio.deleted == false}">checked</c:if>>
                        <label for="deleted">Inattivo</label><input type="radio" id="deleted" name="delete" value="true" <c:if test="${socio.deleted == true}">checked</c:if>>
                        <button type="submit">Conferma</button>
                        <% } %>
                    </form>
                </div>
            </section>
    </main>
    <%@include file="/static/include/aside.jsp"%>
</div>
<%@include file="/static/include/footer.jsp"%>
</body>
</html>
