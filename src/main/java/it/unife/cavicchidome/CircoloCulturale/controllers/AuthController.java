package it.unife.cavicchidome.CircoloCulturale.controllers;

import it.unife.cavicchidome.CircoloCulturale.models.Socio;
import it.unife.cavicchidome.CircoloCulturale.repositories.SocioRepository;
import it.unife.cavicchidome.CircoloCulturale.services.SocioService;
<<<<<<< Updated upstream
=======
import it.unife.cavicchidome.CircoloCulturale.services.TesseraService;
import it.unife.cavicchidome.CircoloCulturale.services.UtenteService;
import it.unife.cavicchidome.CircoloCulturale.models.Utente;
import it.unife.cavicchidome.CircoloCulturale.models.Tessera;
import it.unife.cavicchidome.CircoloCulturale.services.TesseraService;
import it.unife.cavicchidome.CircoloCulturale.repositories.UtenteRepository;
>>>>>>> Stashed changes
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

<<<<<<< Updated upstream
=======
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.regex.Pattern;

>>>>>>> Stashed changes
@Controller
public class AuthController {

    private final SocioService socioService;
    private final UtenteService utenteService;
    SocioRepository socioRepository;
<<<<<<< Updated upstream

    AuthController(SocioRepository socioRepository, SocioService socioService) {
        this.socioRepository = socioRepository;
        this.socioService = socioService;
=======
    UtenteRepository utenteRepository;
    TesseraService tesseraService;

    AuthController(
            SocioRepository socioRepository,
            SocioService socioService,
            UtenteRepository utenteRepository,
            UtenteService utenteService,
            TesseraService tesseraService
    ){
        this.socioRepository = socioRepository;
        this.socioService = socioService;
        this.utenteRepository = utenteRepository;
        this.utenteService = utenteService;
        this.tesseraService = tesseraService;
>>>>>>> Stashed changes
    }

    @GetMapping("/login")
    public String viewLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String cf,
            @RequestParam String password,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response
    ) {
        Optional<Integer> socioId = socioService.authenticate(cf, password);
        if (socioId.isPresent()) {
            Cookie socioCookie = new Cookie("socio-id", "" + socioId.get());
            response.addCookie(socioCookie);
            return "redirect:/home";
        } else {
            redirectAttributes.addAttribute("failed", "true");
            return "redirect:/login";
        }
    }
<<<<<<< Updated upstream
=======

    @GetMapping("/logout")
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Invalida la sessione
        //request.getSession().invalidate();

        // Rimuove il cookie di autenticazione
        Cookie socioCookie = new Cookie("socio-id", null);
        socioCookie.setMaxAge(0);
        response.addCookie(socioCookie);

        // Reindirizza l'utente alla pagina di login
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String viewSignup() {
        return "signup";
    }



    @PostMapping("/signup")
    public String register(
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam String cf,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam String birthplace,
            @RequestParam String state,
            @RequestParam String province,
            @RequestParam String city,
            @RequestParam String street,
            @RequestParam String houseNumber,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phoneNumber,
            @RequestParam String photoUrl,
            RedirectAttributes redirectAttributes
    ) {
        // Valida i dati del form di registrazione
        if (!(utenteService.validateUserInfo(name, surname, cf, dob, birthplace, state, province, city, street, houseNumber) &&
                socioService.validateSocioInfo(email, password, phoneNumber, photoUrl))) {
            redirectAttributes.addAttribute("failed", "true");
            return "redirect:/signup";
        }

        // Registra utente se non presente nel Database
        if(utenteRepository.findByCf(cf) == null){
            Utente utente = utenteService.createUtente(name, surname, cf, dob, birthplace, state, province, city, street, houseNumber);
            Socio socio = socioService.createSocio(utente, email, password, phoneNumber, photoUrl);
            Tessera tessera = tesseraService.createTessera(socio);

        }
        else{
            redirectAttributes.addAttribute("failed", "true");
            redirectAttributes.addAttribute("alreadyPresent", "true");
            return "redirect:/signup";
        }
        // Reindirizza l'utente alla pagina di login
        return "redirect:/login";


    }



>>>>>>> Stashed changes
}
