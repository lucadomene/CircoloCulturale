package it.unife.cavicchidome.CircoloCulturale.controllers;

import it.unife.cavicchidome.CircoloCulturale.models.Socio;
import it.unife.cavicchidome.CircoloCulturale.repositories.SocioRepository;
import it.unife.cavicchidome.CircoloCulturale.services.SocioService;
import it.unife.cavicchidome.CircoloCulturale.services.TesseraService;
import it.unife.cavicchidome.CircoloCulturale.services.UtenteService;
import it.unife.cavicchidome.CircoloCulturale.models.Utente;
import it.unife.cavicchidome.CircoloCulturale.models.Tessera;
import it.unife.cavicchidome.CircoloCulturale.services.TesseraService;
import it.unife.cavicchidome.CircoloCulturale.repositories.UtenteRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.regex.Pattern;
import java.time.LocalDate;


@Controller
public class AuthController {

    private final SocioService socioService;
    private final UtenteService utenteService;
    SocioRepository socioRepository;
    UtenteRepository utenteRepository;
    TesseraService tesseraService;

    @Value("${file.upload-dir}")
    private String uploadDir;

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

    }

    @PostMapping("/login")
    public String login(
            @RequestParam String cf,
            @RequestParam String password,
            @RequestParam(defaultValue = "/") String redirectTo,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response
    ) {
        Optional<Integer> socioId = socioService.authenticate(cf, password);
        if (socioId.isPresent()) {
            Cookie socioCookie = new Cookie("socio-id", "" + socioId.get());
            response.addCookie(socioCookie);
            return "redirect:" + redirectTo;
        } else {
            redirectAttributes.addAttribute("authFailed", "true");
            return "redirect:" + redirectTo;
        }
    }


    @PostMapping("/logout")
    public String logout(
            @RequestParam(defaultValue = "/") String redirectTo,
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
        return "redirect:" + redirectTo;
    }

    @GetMapping("/signup")
    public String viewSignup() {
        return "signup";
    }


    //TO DO: Optional e dto
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
            @RequestParam("photo") MultipartFile photo,
            RedirectAttributes redirectAttributes
    ) {
        // Valida i dati del form di registrazione
        if (!(utenteService.validateUserInfo(name, surname, cf, dob, birthplace, state, province, city, street, houseNumber) &&
                socioService.validateSocioInfo(email, password, phoneNumber))) {
            redirectAttributes.addAttribute("failed", "true");
            return "redirect:/signup";
        }

        // Registra utente se non presente nel Database
        if(utenteRepository.findByCf(cf) == null){
            String filename = null;
            if(photo != null && !photo.isEmpty()){
                // Ottieni l'estensione del file
                String originalFilename = photo.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

                // Crea il nome del file utilizzando il codice fiscale del socio e l'estensione del file
                filename = cf  + extension;
                // Salva la fotografia nel server

                try {
                    Path path = Paths.get(uploadDir, filename);
                    photo.transferTo(path);
                } catch (IOException e) {
                    e.printStackTrace();
                    redirectAttributes.addAttribute("failed", "true");
                    return "redirect:/signup";
                }

            }


            Utente utente = utenteService.createUtente(name, surname, cf, dob, birthplace, state, province, city, street, houseNumber);
            Socio socio = socioService.createSocio(utente, email, password, phoneNumber, filename);
            Tessera tessera = tesseraService.createTessera(socio);
            socioService.sendEmail(socio);

        }
        else{
            redirectAttributes.addAttribute("failed", "true");
            redirectAttributes.addAttribute("alreadyPresent", "true");
            return "redirect:/signup";
        }
        // Reindirizza l'utente alla pagina di login
        return "redirect:/login";


    }




}
