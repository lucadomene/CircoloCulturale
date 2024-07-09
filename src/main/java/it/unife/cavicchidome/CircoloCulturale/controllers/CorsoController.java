package it.unife.cavicchidome.CircoloCulturale.controllers;
import it.unife.cavicchidome.CircoloCulturale.models.CalendarioCorso;
import it.unife.cavicchidome.CircoloCulturale.models.Corso;
import it.unife.cavicchidome.CircoloCulturale.models.Docente;
import it.unife.cavicchidome.CircoloCulturale.models.Socio;
import it.unife.cavicchidome.CircoloCulturale.repositories.CorsoRepository;
import it.unife.cavicchidome.CircoloCulturale.services.SocioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import it.unife.cavicchidome.CircoloCulturale.services.SalaService;
import it.unife.cavicchidome.CircoloCulturale.services.DocenteService;
import it.unife.cavicchidome.CircoloCulturale.services.CorsoService;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final CorsoRepository corsoRepository;

    @Value("${file.corso.upload-dir}")
    private String uploadDir;

    public CorsoController(
            CorsoService corsoService,
            SalaService salaService,
            DocenteService docenteService,
            SocioService socioService,
            CorsoRepository corsoRepository
    ) {
        this.salaService = salaService;
        this.docenteService = docenteService;
        this.socioService = socioService;
        this.corsoService = corsoService;
        this.corsoRepository = corsoRepository;
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
                            @RequestParam("photo") MultipartFile foto,
                            @RequestParam("docenti") List<String> docenti,
                            @RequestParam("giorni") List<Integer> giorni,
                            @RequestParam("orariInizio") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) List<LocalTime> orarioInizio,
                            @RequestParam("orariFine") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) List<LocalTime> orarioFine,
                            @RequestParam ("stipendi")List<Integer> stipendi,
                            @RequestParam("docentiOverlap") String docentiOverlap,
                            Model model,
                            RedirectAttributes redirectAttributes
    ) {

        // Validate course data first
        boolean isValid = corsoService.validateCourseData(descrizione, genere, livello, categoria, idSala, docenti, stipendi, giorni, orarioInizio, orarioFine);
        if (!isValid) {
            // Handle validation failure (e.g., log the error, return "errorView", throw an exception)
            redirectAttributes.addAttribute("fail", "true");
            return "redirect:/corso/creazione-corso"; // Adjust "errorView" to your actual error view name
        }
        boolean checkDocentiSchedule = corsoService.checkDocentiScheduleOverlap(docenti, giorni, orarioInizio, orarioFine);
        if (!checkDocentiSchedule && docentiOverlap.equals("null")) {
            redirectAttributes.addAttribute("docentiOverlap", "true");
            return "redirect:/corso/crea";
        }

        // If validation passes, proceed to save course information
        boolean saveSuccess = corsoService.saveCourseInformation(descrizione, genere, livello, categoria, idSala, docenti,stipendi, giorni, orarioInizio, orarioFine, foto);
        if (!saveSuccess) {
            redirectAttributes.addAttribute("fail", "true");
            return "redirect:/corso/creazione-corso";
        }

        return "redirect:/corso/corsi"; //TODO: Adjust "successView" to your actual success view name
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

        Optional<Socio> socio = socioService.setSocioFromCookie(request, response, model);

        if (corsoId.isPresent()) {
            Optional<Corso> corso = corsoService.findCorsoById(corsoId.get());
            if (corso.isPresent()) {
                model.addAttribute("corso", corso.get());
                model.addAttribute("availability", corsoService.isAvailable(corso.get()));
                if (socio.isPresent()) {
                    model.addAttribute("isEnrolled", corsoService.isEnrolled(corso.get(), socio.get()));
                }
                return "corso/info?id=" + corsoId;
            }
        }

        model.addAttribute("categorie", corsoService.getCategorie());
        model.addAttribute("generi", corsoService.getGeneri());
        model.addAttribute("livelli", corsoService.getLivelli());
        model.addAttribute("corsi", corsoService.filterCorsi(courseCategory, courseGenre, courseLevel, socioId));
        return "corsi";
    }

    @PostMapping("/iscrizione")
    public String enrollCorso (@RequestParam(name = "socio-id") Integer socioId,
                               @RequestParam(name = "corso-id") Integer corsoId,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        try {
            corsoService.enroll(socioId, corsoId);
            redirectAttributes.addAttribute("success", "true");
            return "redirect:/corso/info?id=" + corsoId;
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/corso/info?id=" + corsoId;
        }
    }

    @GetMapping("/modificaBase")
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

        model.addAttribute("uploadDir", uploadDir);
        model.addAttribute("placeholderImage", "profilo.jpg");

        return "modifica-corso"; // Nome della JSP da visualizzare
    }

    @PostMapping("/modificaBase")
    public String updateCorso(
            @RequestParam("idCorso") Integer idCorso,
            @RequestParam("descrizione") String descrizione,
            @RequestParam("genere") String genere,
            @RequestParam("livello") String livello,
            @RequestParam("categoria") String categoria,
            @RequestParam("photo") Optional<MultipartFile> photo,
            RedirectAttributes redirectAttributes
    ) {
        // Validate course data first
        boolean isValid = corsoService.updateBasicCourseInfo(idCorso, descrizione, genere, livello, categoria);
        if (!isValid) {
            // Handle validation failure (e.g., log the error, return "errorView", throw an exception)
            redirectAttributes.addAttribute("fail", "true");
            return "redirect:/corso/modificaBase"; //TODO: vedere come gestire meglio
        }
        redirectAttributes.addAttribute("successMessage", "Corso aggiornato con successo.");

        return "redirect:/corso/info"; //pagina visualizzazione corsi

    }

    @GetMapping("/modificaDocenti")
    public String modificaDocenti(
            @RequestParam("idCorso") Integer idCorso,
            Model model
    ) {
        Optional<Corso> corsoOpt = corsoService.findById(idCorso);
        if (!corsoOpt.isPresent()) {
            // Gestisci il caso in cui il corso non viene trovato
            return "redirect:/paginaErrore";
        }
        Corso corso = corsoOpt.get();

        // Aggiungi il corso al modello
        model.addAttribute("corso", corso);

        // Aggiungi l'elenco dei docenti correnti al modello
        List<Docente> docentiCorso = new ArrayList<>(corso.getDocenti());
        Collections.sort(docentiCorso, Comparator.comparingInt(Docente::getId));
        model.addAttribute("docentiCorso", docentiCorso);

        // Ottieni e aggiungi l'elenco di tutti i docenti disponibili al modello
        List<Docente> tuttiIDocenti = docenteService.findAll();
        model.addAttribute("tuttiIDocenti", tuttiIDocenti);

        List<Object[]> sociInfo = socioService.findSociNotDocentiAndNotSegretariByIdCorso(idCorso);
        model.addAttribute("sociInfo", sociInfo);

        return "modificaDocenti"; // Nome della JSP da visualizzare
    }

    @PostMapping("/modificaDocenti")
    public String updateDocenti(
            @RequestParam("idCorso") Integer idCorso,
            @RequestParam("docentiDaEliminare") Optional<List<Integer>> deletedDocentiId,
            @RequestParam("stipendiAttuali") List<Integer> stipendiAttuali,
            @RequestParam("nuoviDocenti") Optional<List<String>> docentiCf,
            @RequestParam("stipendi") Optional<List<Integer>> stipendi,
            @RequestParam("docentiOverlap")String docentiOverlap,
            RedirectAttributes redirectAttributes
    ) {
        if(docentiOverlap.equals("null") && docentiCf.isPresent() && !corsoService.checkDocentiScheduleOverlap(docentiCf.get())){
            System.out.println(corsoService.checkDocentiScheduleOverlap(docentiCf.get()));
            redirectAttributes.addAttribute("docentiOverlap", "true");
            return "redirect:/corso/modificaDocenti?idCorso=" + idCorso;
        }

        boolean updateSuccess = corsoService.updateDocenti(idCorso, deletedDocentiId, docentiCf, stipendiAttuali, stipendi);

        if (!updateSuccess) {
            redirectAttributes.addAttribute("fail", "true");
            return "redirect:/corso/modificaDocenti?idCorso=" + idCorso ; //TODO: vedere come gestire meglio
        }




        redirectAttributes.addAttribute("successMessage", "Docenti aggiornati con successo.");
        return "redirect:/corso/info";
    }



    @GetMapping("/modificaCalendario")
    public String viewModificaCalendario(
            @RequestParam("idCorso") Integer idCorso,
            Model model
    ) {
        Optional<Corso> corsoOpt = corsoService.findById(idCorso);
        if (!corsoOpt.isPresent()) {
            // Gestisci il caso in cui il corso non viene trovato
            return "redirect:/paginaErrore";
        }
        Corso corso = corsoOpt.get();

        // Aggiungi il corso al modello
        model.addAttribute("corso", corso);
        Set<CalendarioCorso> calendarioCorso = corso.getCalendarioCorso();
        Set<CalendarioCorso> calendariAttivi = calendarioCorso.stream()
                .filter(CalendarioCorso::getActive)
                .collect(Collectors.toSet());
        model.addAttribute("calendarioCorso", calendariAttivi);

        // Aggiungi al modello le informazioni aggiuntive necessarie per la pagina di modifica, come le sale disponibili
        model.addAttribute("sale", salaService.findAll());

        // Restituisci il nome della JSP da visualizzare
        return "modificaCalendario"; // Nome della JSP da visualizzare
    }

    @PostMapping("/modificaCalendario")
    public String updateCalendario(
            @RequestParam("idCorso") Integer idCorso,
            @RequestParam("giorni") List<Integer> giorni,
            @RequestParam("orariInizio") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) List<LocalTime> orariInizio,
            @RequestParam("orariFine") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) List<LocalTime> orariFine,
            @RequestParam("idSala") Integer idSala,
            @RequestParam("docentiOverlap") String docentiOverlap,
            RedirectAttributes redirectAttributes
    ) {
        boolean checkDocentiSchedule = corsoService.checkDocentiScheduleOverlap(idCorso, giorni, orariInizio, orariFine);
        System.out.println(checkDocentiSchedule);
        if (!checkDocentiSchedule && docentiOverlap.equals("null")) {
            redirectAttributes.addAttribute("docentiOverlap", "true");
            return "redirect:/corso/modificaCalendario?idCorso=" + idCorso;
        }
        boolean updateSuccess = corsoService.updateCourseSchedule(idCorso, giorni, orariInizio, orariFine, idSala);

        if (!updateSuccess) {
            redirectAttributes.addAttribute("fail", "true");
            return "redirect:/corso/modificaCalendario?idCorso=" + idCorso ; //TODO: vedere come gestire meglio
        }



        redirectAttributes.addAttribute("successMessage", "Calendario aggiornato con successo.");
        return "redirect:/corso/info";
    }

    @PostMapping("/elimina")
    public String eliminaCorso(
            @RequestParam("idCorso") Integer idCorso,
            RedirectAttributes redirectAttributes) {
        boolean deleteSuccess = corsoService.deleteCourse(idCorso);

        if (!deleteSuccess) {
            redirectAttributes.addAttribute("fail", "true");
            return "redirect:/corso/info?id=" + idCorso;
        }

        redirectAttributes.addAttribute("successMessage", "Corso eliminato con successo.");
        return "redirect:/"; //TODO: vedere come gestire meglio
    }
}
