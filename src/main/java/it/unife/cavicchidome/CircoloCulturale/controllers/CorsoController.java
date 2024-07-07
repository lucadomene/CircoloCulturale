package it.unife.cavicchidome.CircoloCulturale.controllers;
import it.unife.cavicchidome.CircoloCulturale.models.CalendarioCorso;
import it.unife.cavicchidome.CircoloCulturale.models.Corso;
import it.unife.cavicchidome.CircoloCulturale.models.Docente;
import it.unife.cavicchidome.CircoloCulturale.services.SocioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import it.unife.cavicchidome.CircoloCulturale.services.SalaService;
import it.unife.cavicchidome.CircoloCulturale.services.DocenteService;
import it.unife.cavicchidome.CircoloCulturale.services.CorsoService;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/corso")
public class CorsoController {

    private final SalaService salaService;
    private final DocenteService docenteService;
    private final SocioService socioService;
    private final CorsoService corsoService;

    public CorsoController(
            CorsoService corsoService,
            SalaService salaService,
            DocenteService docenteService,
            SocioService socioService
    ) {
        this.salaService = salaService;
        this.docenteService = docenteService;
        this.socioService = socioService;
        this.corsoService = corsoService;
    }

    @GetMapping("/crea")
    public String creaCorso(Model model) {

        //TODO: Aggiungi controllo per verificare che si tratta di ADMIN

        // Ottenere le sale dal servizio e aggiungerle al model
        model.addAttribute("sale", salaService.findAll());

        // Ottenere i soci dal servizio e aggiungerli al model
        List<Object[]> sociInfo = socioService.findSociNotSegretari();
        model.addAttribute("sociInfo", sociInfo);

        // Aggiungere i giorni della settimana al model come interi
        List<Integer> giorniSettimana = Arrays.asList(1, 2, 3, 4, 5); // Assuming 1 = Monday, 2 = Tuesday, etc.
        model.addAttribute("giorniSettimana", giorniSettimana);

        return "creazione-corso";
    }

    @PostMapping("/crea")
    public String creaCorso(@RequestParam("descrizione") String descrizione,
                            @RequestParam("genere") String genere,
                            @RequestParam("livello") String livello,
                            @RequestParam("categoria") String categoria,
                            @RequestParam("idSala") Integer idSala,
                            @RequestParam("foto") MultipartFile foto,
                            @RequestParam("docenti") List<String> docenti,
                            @RequestParam("giorni") List<Integer> giorni,
                            @RequestParam("orariInizio") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) List<LocalTime> orarioInizio,
                            @RequestParam("orariFine") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) List<LocalTime> orarioFine,
                            @RequestParam ("stipendi")List<Integer> stipendi,
                            Model model,
                            RedirectAttributes redirectAttributes
    ) {

        // Validate course data first
        boolean isValid = corsoService.validateCourseData(descrizione, genere, livello, categoria, idSala, docenti, stipendi, giorni, orarioInizio, orarioFine);
        if (!isValid) {
            // Handle validation failure (e.g., log the error, return "errorView", throw an exception)
            redirectAttributes.addAttribute("fail", "true");
            return "redirect:/creazioneCorso"; // Adjust "errorView" to your actual error view name
        }

        // If validation passes, proceed to save course information
        boolean saveSuccess = corsoService.saveCourseInformation(descrizione, genere, livello, categoria, idSala, docenti,stipendi, giorni, orarioInizio, orarioFine, foto);
        if (!saveSuccess) {
            redirectAttributes.addAttribute("fail", "true");
            return "redirect:/creazioneCorso";
        }

        return "redirect:/"; //TODO: Adjust "successView" to your actual success view name
    }

    @GetMapping("/modifica")
    public String viewEdit(
            @RequestParam("idCorso") Integer idCorso,
            Model model
    ) {

        // Recupera le informazioni del corso tramite il suo ID
        Optional<Corso> corso = corsoService.findById(idCorso);
        if (!corso.isPresent()) {
            // TODO:Gestisci il caso in cui il corso non viene trovato (es. reindirizzamento a una pagina di errore)
            return "redirect:/paginaErrore";
        }

        // Aggiungi il corso al modello per poterlo visualizzare nella pagina JSP
        model.addAttribute("corso", corso.get());

        // Recupera e aggiungi al modello la lista dei docenti che insegnano il corso
        Set<Docente> docenti = corso.get().getDocenti();
        model.addAttribute("docenti", docenti);

        // Recupera e aggiungi al modello il calendario di svolgimento del corso
        Set<CalendarioCorso> calendario = corso.get().getCalendarioCorso();
        model.addAttribute("calendario", calendario);

        // Aggiungi al modello le informazioni aggiuntive necessarie per la pagina di modifica, come le sale disponibili
        model.addAttribute("sale", salaService.findAll());

        // Aggiungi al modello le informazioni sui soci (nome, cognome, cf) dei non segretari
        List<Object[]> sociInfo = socioService.findSociNotSegretari();
        model.addAttribute("sociInfo", sociInfo);



        return "modifica-corso"; // Nome della JSP da visualizzare
    }

    @GetMapping("/info")
    public String viewCorsi(@RequestParam(name = "id") Optional<Integer> corsoId,
                            @RequestParam(name = "categoria") Optional<String> courseCategory,
                            @RequestParam(name = "genere") Optional<String> courseGenre,
                            @RequestParam(name = "livello") Optional<String> courseLevel,
                            @RequestParam(name = "id-socio") Optional<Integer> socioId,
                            Model model,
                            HttpServletRequest request,
                            HttpServletResponse response) {

        socioService.setSocioFromCookie(request, response, model);

        if (corsoId.isPresent()) {
            Optional<Corso> corso = corsoService.findById(corsoId.get());
            if (corso.isPresent()) {
                model.addAttribute("corso", corso.get());
                return "corso-info";
            }
        }

        model.addAttribute("categorie", corsoService.getCategorie());
        model.addAttribute("generi", corsoService.getGeneri());
        model.addAttribute("livelli", corsoService.getLivelli());
        model.addAttribute("corsi", corsoService.filterCorsi(courseCategory, courseGenre, courseLevel, socioId));
        return "corsi";
    }
}
