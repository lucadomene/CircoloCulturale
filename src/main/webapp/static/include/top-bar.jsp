<script>
    function setRedirectValue() {
        let redirectField = document.getElementById("redirectField")
        redirectField.value = window.location.pathname + window.location.search
    }

    function signupButtonAction() {
        let signupButton = document.getElementById("signupButton")
        if (signupButton != null) {
            signupButton.addEventListener("click", function () {
                window.location.href = "/signup"
            })
        }
    }

    window.addEventListener("load", setRedirectValue)
    window.addEventListener("load", signupButtonAction)
</script>
<div id="top-bar">
    <section id="accountInfo">
        <% if (request.getAttribute("socioHeader") != null) { %>
        <p>
            <a href="/socio/profile">${socioHeader.utente.nome} ${socioHeader.utente.cognome}</a>
        </p>
        <form class="logInOut" action="/logout" method="POST">
            <input type="submit" value="Esci">
            <input type="hidden" id="redirectField" name="redirectTo" value="">
        </form>
        <% } else { %>
        <form class="logInOut" action="/login" method="POST">
            <input name="cf" type="text" placeholder="Codice fiscale">
            <input name="password" type="password" placeholder="Password">
            <input type="submit" value="accedi">
            <input type="button" id="signupButton" value="registrati">
            <input type="hidden" id="redirectField" name="redirectTo" value="">
        </form>
        <% } %>
    </section>
</div>