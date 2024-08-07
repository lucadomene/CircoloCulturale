package it.unife.cavicchidome.CircoloCulturale.services;

import it.unife.cavicchidome.CircoloCulturale.models.*;
import it.unife.cavicchidome.CircoloCulturale.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CorsoService {

    private final UtenteRepository utenteRepository;
    private final OrarioSedeService orarioSedeService;
    private final CorsoRepository corsoRepository;
    private final SalaRepository salaRepository;
    private final SocioRepository socioRepository;
    private final DocenteRepository docenteRepository;
    private final CalendarioCorsoRepository calendarioCorsoRepository;
    private final SocioService socioService;
    private final SedeRepository sedeRepository;

    @Value("${file.corso.upload-dir}")
    String uploadCorsoDir;


    public CorsoService(
            CorsoRepository corsoRepository,
            SalaRepository salaRepository,
            SocioRepository socioRepository,
            UtenteRepository utenteRepository,
            DocenteRepository docenteRepository,
            CalendarioCorsoRepository calendarioCorsoRepository,
            OrarioSedeService orarioSedeService,
            SedeRepository sedeRepository,
            SocioService socioService
    ) {
        this.corsoRepository = corsoRepository;
        this.salaRepository = salaRepository;
        this.socioRepository = socioRepository;
        this.utenteRepository = utenteRepository;
        this.docenteRepository = docenteRepository;
        this.calendarioCorsoRepository = calendarioCorsoRepository;
        this.orarioSedeService = orarioSedeService;
        this.socioService = socioService;
        this.sedeRepository = sedeRepository;
    }

    public boolean validateBasicInfo(String descrizione, String genere, String livello, String categoria) {
        String nomeIndirizzoPattern = "^(?=.*[A-Za-z])[A-Za-z\\s\\'\\-àèéìòùÀÈÉÌÒÙáéíóúÁÉÍÓÚâêîôûÂÊÎÔÛäëïöüÿÄËÏÖÜŸ]+$";
        String descrizionePattern = "^(?=.*[A-Za-z])[A-Za-z\\s\\'\\-\\(\\)\\.\\,\\;\\:\\!\\?\\[\\]\\{\\}\"\\-àèéìòùÀÈÉÌÒÙáéíóúÁÉÍÓÚâêîôûÂÊÎÔÛäëïöüÿÄËÏÖÜŸ]+$";
        /* almeno un carattere alfabetico (maiuscolo o minuscolo) e possono includere spazi, apostrofi, trattini e, nel caso di charDescrizioneRegex,
             anche parentesi, punti, virgole, punto e virgola, due punti, punti esclamativi, punti interrogativi, parentesi quadre, parentesi graffe, e virgolette.
             Anche lettere accentate
             */
        if( !descrizione.isEmpty() && descrizione != null && !descrizione.matches(descrizionePattern)){
            return false;
        }
        if(genere == null || genere.isEmpty() || livello == null || livello.isEmpty() || categoria == null || categoria.isEmpty()){
            return false;
        }
        if(!genere.matches(nomeIndirizzoPattern) || !livello.matches(nomeIndirizzoPattern)){
            return false;
        }

        return true;
    }

    public boolean validateDocentiAndStipendi(List<String> docentiCf, List<Integer> stipendi) {
        if (docentiCf == null || docentiCf.isEmpty() || !validateStipendi(stipendi)) return false;
        return true;
    }

    private boolean validateStipendi(List<Integer> stipendi){
        if (stipendi == null || stipendi.isEmpty()) return false;
        for (Integer stipendio : stipendi) {
            if (stipendio == null || stipendio < 10000 || stipendio > 100000) return false;
        }
        return true;
    }

    public boolean validateCalendarioAndSala(List<Integer> giorni, List<LocalTime> orarioInizio, List<LocalTime> orarioFine, Integer idSala) {
        if (idSala == null || giorni == null || giorni.isEmpty() || orarioInizio == null || orarioInizio.isEmpty() || orarioFine == null || orarioFine.isEmpty()) return false;
        if (orarioInizio.size() != orarioFine.size()) return false;
        Optional<Sala> salaOpt = salaRepository.findById(idSala);
        if (!salaOpt.isPresent()) {
            return false; // Sala not found
        }
        for (int i = 0; i < giorni.size(); i++) {
            Integer giorno = giorni.get(i);
            //List<LocalTime> orariAperturaChiusura = orarioSedeService.findOrarioAperturaChiusuraByIdSedeAndGiornoSettimana(sedeService.findSedeByIdSalaActive(idSala).get().getId(), Weekday.values()[giorno]);
            //LocalTime orarioApertura = orariAperturaChiusura.get(0);
            //LocalTime orarioChiusura = orariAperturaChiusura.get(1);
            LocalTime inizio = orarioInizio.get(giorno-1);
            LocalTime fine = orarioFine.get(giorno-1);
            /*if (inizio == null || fine == null || !inizio.isBefore(fine) || inizio.isBefore(orarioApertura) || fine.isAfter(orarioChiusura)) {
                return false;
            }
            */
            if(outsideOpeningHours(idSala, Weekday.fromDayNumber(giorno), inizio, fine)){ //TODO: verificare se funziona uguale
                return false;
            }
        }
        return true;
    }

    public boolean validateCourseData(
            String descrizione,
            String genere,
            String livello,
            String categoria,
            Integer idSala,
            List<String> docentiCf,
            List<Integer> stipendi,
            List<Integer> giorni,
            List<LocalTime> orarioInizio,
            List<LocalTime> orarioFine) {

        return validateBasicInfo(descrizione, genere, livello, categoria) &&
                validateDocentiAndStipendi(docentiCf, stipendi) &&
                validateCalendarioAndSala(giorni, orarioInizio, orarioFine, idSala);
    }


    @Transactional(readOnly = true) //solo active
    public Corso findCorsoByCategoriaGenereLivello(String categoria, String genere, String livello) {
        return corsoRepository.findByCategoriaAndGenereAndLivello(categoria, genere, livello).orElse(null);
    }

    @Transactional(readOnly = true) //anche non active
    public Corso findCorsoByCategoriaGenereLivelloAll(String categoria, String genere, String livello) {
        return corsoRepository.findByCategoriaAndGenereAndLivello(categoria, genere, livello).orElse(null);
    }

    @Transactional
    public Corso saveCourseInformation(
            String descrizione,
            String genere,
            String livello,
            String categoria,
            Integer idSala,
            List<String> docentiCf,
            List<Integer> stipendi,
            List<Integer> giorni,
            List<LocalTime> orarioInizio,
            List<LocalTime> orarioFine,
            MultipartFile photo
    ) throws IllegalArgumentException {

       if(!validateCourseData(descrizione, genere, livello, categoria, idSala, docentiCf, stipendi, giorni, orarioInizio, orarioFine))
            throw new IllegalArgumentException("Invalid course data");

        boolean reactivated = false;

        Optional<Corso> corsoIdentical = corsoRepository.findByCategoriaAndGenereAndLivelloAll(categoria, genere, livello);
        if(corsoIdentical.isPresent() ) {
            if(corsoIdentical.get().getActive() == true)
                throw new IllegalArgumentException("Course already exists"); //Corso Attivo già esistente
            else {
                corsoIdentical.get().setActive(true);
                reactivated = true;
            }
        }
        System.out.println("reactivated: " + reactivated);


        Sala sala = salaRepository.findByIdActive(idSala).orElse(null);
        if (sala == null) {
            throw new IllegalArgumentException("Sala not found");
        }
        Set<Docente> docenti = new HashSet<>();
        for (int i = 0; i < docentiCf.size(); i++) {
            String cf = docentiCf.get(i);

            Optional<Utente> utenteOpt = utenteRepository.findByCfNotDeleted(cf);
            if (!utenteOpt.isPresent()) {
                throw new IllegalArgumentException("utente not found");
            }
            Utente utente = utenteOpt.get();


            Optional<Socio> socioOpt = socioRepository.findById(utente.getId()); //solo soci attivi
            if (!socioOpt.isPresent()) {
                throw new IllegalArgumentException("Socio not found");
            }
            Socio socio = socioOpt.get();


            if (socio.getDocente() == null) {
                Docente docente = new Docente();
                docente.setActive(true);
                docente.setSocio(socio);
                docente.setStipendio(BigDecimal.valueOf(stipendi.get(i)));
                docenteRepository.save(docente);
                docenti.add(docente);
            }
            else{ //aggiorna stipendio solo se superiore al precedente
                if (BigDecimal.valueOf(stipendi.get(i)).compareTo(socio.getDocente().getStipendio()) > 0){
                    socio.getDocente().setStipendio(BigDecimal.valueOf(stipendi.get(i)));
                    socio.getDocente().setActive(true);
                    docenteRepository.save(socio.getDocente());
                }
                docenti.add(socio.getDocente());
            }

        }

        if(checkScheduleOverlap(Optional.empty(), giorni, orarioInizio, orarioFine, idSala) == false){
            throw new IllegalArgumentException("Schedule overlap");
        }


        Corso corso;
        if(!reactivated){
            corso = new Corso();
            corso.setGenere(genere);
            corso.setLivello(livello);
            corso.setCategoria(categoria);
        }
        else{
            corso=corsoIdentical.get();
            corso.setDescrizione(null);
            corso.setUrlFoto(null);
        }
        if(descrizione != null && !descrizione.isEmpty()){
            corso.setDescrizione(descrizione);
        }
        corso.setIdSala(sala);
        corso.setDocenti(docenti);
        corso.setActive(true);
        corsoRepository.save(corso);

        if(photo != null){
            String filename = saveCorsoPicture(photo, categoria, corso.getId()); //TODO: controllare che funzioni alla creazione di un corso
            corso.setUrlFoto(filename);
        }

        Set<CalendarioCorso> nuovoCalendarioCorso = new HashSet<>();
        for (Integer giorno: giorni) {
            Weekday weekday;
            try{
                weekday = Weekday.fromDayNumber(giorno);
            }catch (IllegalArgumentException e){
                throw new IllegalArgumentException("Giorno non valido");
            }

            if(reactivated) {
                Optional<CalendarioCorso> existingEntryOpt = calendarioCorsoRepository.findByCorsoAndGiornoSettimanaId(corso.getId(), weekday);

                if (existingEntryOpt.isPresent()) {
                    System.out.println(existingEntryOpt.get().getGiornoSettimana());
                    CalendarioCorso existingEntry = existingEntryOpt.get();
                    System.out.println(orarioInizio.get(giorno - 1));
                    existingEntry.setOrarioInizio(orarioInizio.get(giorno - 1));
                    existingEntry.setOrarioFine(orarioFine.get(giorno - 1));
                    existingEntry.setActive(true);
                    calendarioCorsoRepository.save(existingEntry);
                    nuovoCalendarioCorso.add(existingEntry);
                } else {
                    // Create new entry
                    CalendarioCorso newCalendario = new CalendarioCorso();
                    CalendarioCorsoId calendarioCorsoId = new CalendarioCorsoId();
                    calendarioCorsoId.setGiornoSettimana(weekday);
                    calendarioCorsoId.setIdCorso(corso.getId());
                    newCalendario.setId(calendarioCorsoId);
                    newCalendario.setIdCorso(corso);
                    newCalendario.setActive(true);
                    newCalendario.setOrarioInizio(orarioInizio.get(giorno - 1));
                    newCalendario.setOrarioFine(orarioFine.get(giorno - 1));
                    calendarioCorsoRepository.save(newCalendario);
                    nuovoCalendarioCorso.add(newCalendario);
                }
            }else{
                CalendarioCorso newCalendario = new CalendarioCorso();
                CalendarioCorsoId calendarioCorsoId = new CalendarioCorsoId();
                ;
                calendarioCorsoId.setGiornoSettimana(weekday);
                calendarioCorsoId.setIdCorso(corso.getId());
                newCalendario.setId(calendarioCorsoId);
                newCalendario.setIdCorso(corso);
                newCalendario.setActive(true);
                newCalendario.setOrarioInizio(orarioInizio.get(giorno - 1));
                newCalendario.setOrarioFine(orarioFine.get(giorno - 1));
                calendarioCorsoRepository.save(newCalendario);
                nuovoCalendarioCorso.add(newCalendario);
            }

        }


        if (!corso.getIdSala().equals(idSala)) {
            Sala newSala = salaRepository.findById(idSala).orElseThrow(() -> new IllegalStateException("Sala not found"));
            corso.setIdSala(newSala);

        }

        corso.setCalendarioCorso(nuovoCalendarioCorso);
        return corsoRepository.save(corso);

    }


    //Controlla sovrapposizione oraria corsi nella stessa sala
    public boolean checkScheduleOverlap(
            Optional<Corso> corso,
            List<Integer> giorni,
            List<LocalTime> orarioInizio,
            List<LocalTime> orarioFine,
            Integer idSala
    ) {
        //Controllo sovrapposizione oraria di corsi che si tengono nella stessa sala
        boolean sovrapposizione = false;
        for (int i = 0; i < giorni.size(); i++) {
            Integer giorno = giorni.get(i);
            LocalTime inizio = orarioInizio.get(giorno - 1);
            LocalTime fine = orarioFine.get(giorno - 1);

            // Trova corsi nel CalendarioCorso che si sovrappongono per orario
            List<CalendarioCorso> corsiSovrapposti = calendarioCorsoRepository.findCorsiSovrapposti(Weekday.fromDayNumber(giorno), inizio, fine, idSala);
            for (CalendarioCorso calendarioCorso : corsiSovrapposti) {
                System.out.println(calendarioCorso.getIdCorso().getDescrizione());
                Corso corsoSovrapposto = calendarioCorso.getIdCorso();
                if (calendarioCorso.getIdCorso().getActive() == true && corsoSovrapposto.getActive() == true) {
                    if (corso.isPresent() && corso.get().getId().equals(corsoSovrapposto.getId())) {
                        continue; //si esclude corso stesso dal controllo di sovrapposizione
                    }
                    sovrapposizione = true;
                    break;
                }
            }
            if (sovrapposizione) break;
        }

        if (sovrapposizione) {
            // Gestisci la sovrapposizione (es. restituendo false o lanciando un'eccezione)
            return false;
        } else return true;
    }

    /*public boolean checkTimeOverlap(Optional<Corso> corso, List<Integer> giorni, List<LocalTime> orarioInizio, List<LocalTime> orarioFine) {
        //Controllo sovrapposizione oraria di corsi che si tengono nella stessa sala
        boolean sovrapposizione = false;
        for (int i = 0; i < giorni.size(); i++) {
            Integer giorno = giorni.get(i);
            LocalTime inizio = orarioInizio.get(giorno-1);
            LocalTime fine = orarioFine.get(giorno-1);

            // Trova corsi nel CalendarioCorso che si sovrappongono per orario
            Optional<List<CalendarioCorso>> corsiSovrappostiOpt = calendarioCorsoRepository.findCorsiContemporanei(Weekday.fromDayNumber(giorno) , inizio, fine);
            if(corsiSovrappostiOpt.isPresent()) {
                List<CalendarioCorso> corsiSovrapposti = corsiSovrappostiOpt.get();
                for (CalendarioCorso calendarioCorso : corsiSovrapposti) {
                    System.out.println(calendarioCorso.getIdCorso().getDescrizione());
                    Corso corsoSovrapposto = calendarioCorso.getIdCorso();
                    if (calendarioCorso.getIdCorso().getActive() == true && corsoSovrapposto.getActive() == true) {
                        if (corso.isPresent() && corso.get().getId().equals(corsoSovrapposto.getId())) {
                            continue; //si esclude corso stesso dal controllo di sovrapposizione
                        }
                        sovrapposizione = true;
                        break;
                    }
                }
            }
            if (sovrapposizione) break;
        }

        if (sovrapposizione) {
            // Gestisci la sovrapposizione (es. restituendo false o lanciando un'eccezione)
            return false;
        }
        else return true;
    }

     */

    //Controlla sovrapposizione oraria corsi nella stessa sala
    public boolean checkScheduleOverlap(
            Optional<Corso> corso,
            List<CalendarioCorso> calendarioCorso,
            Integer idSala
    ) {
        //Controllo sovrapposizione oraria di corsi che si tengono nella stessa sala
        boolean sovrapposizione = false;
        for (int i = 0; i < calendarioCorso.size(); i++) {
            Integer giorno = calendarioCorso.get(i).getId().getGiornoSettimana().getDayNumber();
            LocalTime inizio = calendarioCorso.get(i).getOrarioInizio();
            LocalTime fine = calendarioCorso.get(i).getOrarioFine();

            // Trova corsi nel CalendarioCorso che si sovrappongono per orario
            List<CalendarioCorso> corsiSovrapposti = calendarioCorsoRepository.findCorsiSovrapposti(Weekday.fromDayNumber(giorno), inizio, fine, idSala);
            for (CalendarioCorso calendario : corsiSovrapposti) {
                System.out.println(calendario.getIdCorso().getDescrizione());
                Corso corsoSovrapposto = calendario.getIdCorso();
                if (calendario.getIdCorso().getActive() == true && corsoSovrapposto.getActive() == true) {
                    if (corso.isPresent() && corso.get().getId().equals(corsoSovrapposto.getId())) {
                        continue; //si esclude corso stesso dal controllo di sovrapposizione
                    }
                    sovrapposizione = true;
                    break;
                }
            }
            if (sovrapposizione) break;
        }

        if (sovrapposizione) {
            // Gestisci la sovrapposizione (es. restituendo false o lanciando un'eccezione)
            return false;
        } else return true;
    }

    //Controlla sovrapposizione oraria corsi senza controllare la sala
    //TODO: se non usato eliminare
    public boolean checkTimeOverlap(
            Corso corso,
            List<CalendarioCorso> calendarioCorso
    ) {
        //Controllo sovrapposizione oraria di corsi
        boolean sovrapposizione = false;
        for (int i = 0; i < calendarioCorso.size(); i++) {
            Integer giorno = calendarioCorso.get(i).getId().getGiornoSettimana().getDayNumber();
            LocalTime inizio = calendarioCorso.get(i).getOrarioInizio();
            LocalTime fine = calendarioCorso.get(i).getOrarioFine();

            // Trova corsi nel CalendarioCorso che si sovrappongono per orario
            List<CalendarioCorso> calendariSovrapposti = calendarioCorsoRepository.findCorsiContemporanei(Weekday.fromDayNumber(giorno), inizio, fine); //solo corsi e calendari attivi
            if (!calendariSovrapposti.isEmpty()) {
                for (CalendarioCorso calendario : calendariSovrapposti) {
                    System.out.println(calendario.getIdCorso().getId());
                    Corso corsoSovrapposto = calendario.getIdCorso();
                    if (corso.getId().equals(corsoSovrapposto.getId())) {
                        continue; //si esclude corso stesso dal controllo di sovrapposizione
                    }
                    sovrapposizione = true;
                    break;

                }
                if (sovrapposizione) break;
            }
        }

        if (sovrapposizione) {
            // Gestisci la sovrapposizione (es. restituendo false o lanciando un'eccezione)
            return false;
        } else return true;
    }

    //per la modifica dei docenti
    @Transactional
    public boolean checkDocentiScheduleOverlap(Integer idCorso, List<String> docentiCf) {
        boolean sovrapposizione = false;
        for (String cf : docentiCf) {
            System.out.println("docente: "+cf);
            Optional<Docente> docente = docenteRepository.findByCf(cf);
            if (!docente.isPresent()) {
                return false; //TODO: gestire caso in cui docente non esiste: eccezione
            }
            Integer idDocente = docente.get().getId();
            List<Corso> corsiInsegnati = corsoRepository.findCorsiByDocenteId(idDocente); //solo corsi e docenti active
            if (!corsiInsegnati.isEmpty()) {
                List<Integer> corsiInsegnatiIds = corsiInsegnati.stream()
                        .filter(corso -> !corso.getId().equals(idCorso)) // Exclude the course with id=idCorso
                        .map(Corso::getId)
                        .collect(Collectors.toList());
                System.out.println(corsiInsegnatiIds.size());
                List<CalendarioCorso> calendariSovrapposti = calendarioCorsoRepository.existsSovrapposizioneCorsiInsegnati(idCorso, corsiInsegnatiIds);
                System.out.println(calendariSovrapposti.size());
                if(!calendariSovrapposti.isEmpty())
                    sovrapposizione=true;

            }

        }
        System.out.println(sovrapposizione);
        return !sovrapposizione;
    }

    //Verifica sovrapposizione oraria di un corso (dato dall'id) con la tupla giorno, orarioInizio, orarioFine di un altro
    public boolean checkScheduleConflict(Integer idCorso, Integer giorno, LocalTime inizio, LocalTime fine) {
        Optional<Corso> corso = corsoRepository.findByIdActive(idCorso);
        if (!corso.isPresent()) {
            return false; //TODO: gestire caso in cui corso non esiste: eccezione
        } else {
            List<CalendarioCorso> calendarioCorso = new ArrayList<>(corso.get().getCalendarioCorso());
            for (CalendarioCorso calendario : calendarioCorso) {
                Optional<CalendarioCorso> calendarioContemporaneo = calendarioCorsoRepository.findSeCorsoContemporaneo(Weekday.fromDayNumber(giorno), idCorso, inizio, fine); //solo calendari e corsi active
                if (calendarioContemporaneo.isPresent() ) {
                    System.out.println(calendarioContemporaneo.get().getIdCorso().getId());
                    return false;
                    //&& calendarioContemporaneo.get().getIdCorso().getId() != idCorso
                }
            }
        }
        return true;
    }


    //Per la creazione di un corso
    //Se c'è sovrapposizione restituisce false
    @Transactional
    public boolean checkDocentiScheduleOverlap(
            List<String> docentiCf,
            List<Integer> giorni,
            List<LocalTime> orariInizio,
            List<LocalTime> orariFine
    ) {
        boolean sovrapposizione = false;
        for (String cf : docentiCf) {
            Optional<Docente> docente = docenteRepository.findByCf(cf);
            if (!docente.isPresent()) {
                continue;
            } else {
                Integer idDocente = docente.get().getId();
                List<Corso> corsiInsegnati = corsoRepository.findCorsiByDocenteId(idDocente);
                if (!corsiInsegnati.isEmpty()) {
                    for (Corso corso : corsiInsegnati) {
                        for (int i = 0; i < giorni.size(); i++) {
                            Integer giorno = giorni.get(i);
                            LocalTime inizio = orariInizio.get(giorno - 1);
                            LocalTime fine = orariFine.get(giorno - 1);
                            if (checkScheduleConflict(corso.getId(), giorno, inizio, fine) == false) {
                                sovrapposizione = true;
                                break;
                            }
                        }

                    }
                }
            }
        }
        if (sovrapposizione)
            return false;
        return true;

    }

    //Per la modifica del calendario
    @Transactional
    public boolean checkDocentiScheduleOverlap(
            Integer idCorso,
            List<Integer> giorni,
            List<LocalTime> orariInizio,
            List<LocalTime> orariFine
    ) {
        boolean sovrapposizione = false;
        Optional<Corso> corsoOpt = corsoRepository.findById(idCorso);
        if (!corsoOpt.isPresent()) {
            return false; //TODO: gestire caso in cui corso non esiste: eccezione
        }
        Set<Docente> docenti = corsoOpt.get().getDocenti(); //gia solo corsi attivi
        List<Docente> docentiList = new ArrayList<>(docenti);
        for (Docente docente : docentiList) {
            Integer idDocente = docente.getId();
            List<Corso> corsiInsegnati = corsoRepository.findCorsiByDocenteId(idDocente);

            if (!corsiInsegnati.isEmpty()) {
                for (Corso corso : corsiInsegnati) {
                    System.out.println(corso.getDescrizione());
                    if (corso.getActive() && corso.getId() != idCorso) { //non stesso corso
                        for (int i = 0; i < giorni.size(); i++) {
                            Integer giorno = giorni.get(i);
                            LocalTime inizio = orariInizio.get(giorno - 1);
                            LocalTime fine = orariFine.get(giorno - 1);
                            if (checkScheduleConflict(corso.getId(), giorno, inizio, fine) == false) {
                                sovrapposizione = true;
                                break;
                            }
                        }

                    }
                }
            }

        }
        if (sovrapposizione) return false;
        return true;

    }

    public boolean corsoOverlap(Integer salaId, LocalDate date, LocalTime start, LocalTime end) {
        return corsoRepository.findOverlapCorso(salaId, Weekday.fromDayNumber(date.getDayOfWeek().getValue()), start, end).isPresent();
    }

    String saveCorsoPicture(MultipartFile picture, String categoria, Integer idCorso) {
        System.out.println("Prova Salvataggio foto");
        if (picture == null || picture.isEmpty()) {
            return null;
        }

        String originalFilename = picture.getOriginalFilename();
        if (originalFilename == null) {
            return null;
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = categoria + idCorso + extension;
        //String filename = filenameConSpazi.replace(" ", "");

        try {
            // Percorso relativo alla directory resources/static del progetto
            String relativePath = "static/images/corsoPhotos";
            // Costruisce il percorso completo utilizzando il percorso del progetto
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "src/main/resources", relativePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path picturePath = uploadPath.resolve(filename);
            System.out.println("Tentativo di salvataggio in: " + picturePath.toAbsolutePath());

            picture.transferTo(picturePath);

            if (Files.exists(picturePath)) {
                System.out.println("File salvato correttamente in" + picturePath.toAbsolutePath());
            } else {
                System.out.println("Il file non è stato salvato.");
                return null;
            }

            // Restituisce il percorso relativo per l'accesso via URL
            //return Paths.get(relativePath, filename).toString().replace("\\", "/");
            return filename;
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            exc.printStackTrace();
            return null;
        }
    }

    @Transactional
    public boolean outsideOpeningHours(Integer idSala, Weekday dow, LocalTime startTime, LocalTime endTime) {
        Sede sede = sedeRepository.getReferenceById(salaRepository.getReferenceById(idSala).getIdSede().getId());
        OrarioSede orarioSede = sedeRepository.findOrarioSede(sede.getId(), dow);
        if (startTime.isBefore(orarioSede.getOrarioApertura()) || endTime.isAfter(orarioSede.getOrarioChiusura())) {
            return true;
        } else {
            return false;
        }
    }


    @Transactional
    public List<Corso> filterCorsiDocente(Optional<String> category,
                                          Optional<String> genre,
                                          Optional<String> level,
                                          Integer docenteId) {
            List<Corso> corsi = filterCorsi(category, genre, level, Optional.empty());
            List<Corso> docenteFilteredCorsi = new ArrayList<Corso>();
            for (Corso c : corsi) {
                for (Docente d : c.getDocenti()) {
                    if (d.getId().equals(docenteId)) {
                        docenteFilteredCorsi.add(c);
                    }
                }
            }
            return docenteFilteredCorsi;
    }

    @Transactional
    public List<Corso> filterCorsi(Optional<String> category,
                                   Optional<String> genre,
                                   Optional<String> level,
                                   Optional<Boolean> active) { //TODO: DOME Ho corretto la linea perche  si vedevano anche i corsi cancellati quando non dovevano
       List<Corso> corsi = corsoRepository.findAllActive(active.orElse(false));

        if (category.isPresent() && !category.get().isEmpty()) {
            List<Corso> categoryFilteredCorsi = new ArrayList<>();
            for (Corso c : corsi) {
                if (c.getCategoria().equals(category.get())) {
                    categoryFilteredCorsi.add(c);
                }
            }
            corsi = categoryFilteredCorsi;
        }

        if (genre.isPresent() && !genre.get().isEmpty()) {
            List<Corso> genreFilteredCorsi = new ArrayList<>();
            for (Corso c : corsi) {
                if (c.getGenere().equals(genre.get())) {
                    genreFilteredCorsi.add(c);
                }
            }
            corsi = genreFilteredCorsi;
        }

        if (level.isPresent() && !level.get().isEmpty()) {
            List<Corso> levelFilteredCorsi = new ArrayList<>();
            for (Corso c : corsi) {
                if (c.getLivello().equals(level.get())) {
                    levelFilteredCorsi.add(c);
                }
            }
            corsi = levelFilteredCorsi;
        }

        return corsi;
    }

    @Transactional
    public Boolean isEnrolled(Corso corso, Socio socio) {
        return corso.getSoci().contains(socio);
    }

    @Transactional
    public Boolean isAvailable(Corso corso) {
        return (corso.getIdSala().getCapienza() > corso.getSoci().size());
    }


    @Transactional
    public List<String> getCategorie() {
        return corsoRepository.findDistinctCategoria();
    }

    @Transactional
    public List<String> getGeneri() {
        return corsoRepository.findDistinctGenere();
    }

    @Transactional
    public List<String> getLivelli() {
        return corsoRepository.findDistinctLivello();
    }
    @Transactional
    public List<String> getCategorieActive() {
        return corsoRepository.findDistinctCategoriaActive();
    }

    @Transactional
    public List<String> getGeneriActive() {
        return corsoRepository.findDistinctGenereActive();
    }

    @Transactional
    public List<String> getLivelliActive() {
        return corsoRepository.findDistinctLivelloActive();
    }

    @Transactional
    public Optional<Corso> findCorsoById(Integer idCorso) {
        return corsoRepository.findByIdActive(idCorso);
    }

    @Transactional
    public void enroll(Integer socioId, Integer corsoId) throws EntityNotFoundException {
        Corso corso = corsoRepository.getReferenceById(corsoId);
        if (corso == null) {
            throw new EntityNotFoundException("Corso not found");
        }
        Socio socio = socioRepository.getReferenceById(socioId);
        if (socio == null) {
            throw new EntityNotFoundException("Socio not found");
        }
        corso.getSoci().add(socio);
        corsoRepository.save(corso);
    }

    @Transactional
    public void unenroll(Integer socioId, Integer corsoId) throws EntityNotFoundException {
        Corso corso = corsoRepository.getReferenceById(corsoId);
        if (corso == null) {
            throw new EntityNotFoundException("Corso not found");
        }
        Socio socio = socioRepository.getReferenceById(socioId);
        if (socio == null) {
            throw new EntityNotFoundException("Socio not found");
        }

        corso.getSoci().remove(socio);
        corsoRepository.save(corso);
    }

    @Transactional
    public boolean updateBasicCourseInfo(Integer idCorso, String descrizione, String genere, String livello, String categoria, MultipartFile photo) {
        // Validate course data first
        boolean isValid = validateBasicInfo(descrizione, genere, livello, categoria);
        if (!isValid) {
            return false;
        }

        Optional<Corso> corsoOpt = corsoRepository.findById(idCorso);
        Optional<Corso> corsoIdenticalOpt = corsoRepository.findByCategoriaAndGenereAndLivello(descrizione, genere, livello);

        if (!corsoOpt.isPresent() || corsoIdenticalOpt.isPresent()) { //Se id non esiste o corso con stesse caratteristiche esiste già
            return false;
        }

        Corso corso = corsoOpt.get();
        if(photo != null){
            String filename = saveCorsoPicture(photo, categoria, idCorso); //TODO: controllare che funzioni alla creazione di un corso
            corso.setUrlFoto(filename);
        }

        // Update course data
        corso.setDescrizione(descrizione);
        corso.setGenere(genere);
        corso.setLivello(livello);
        corso.setCategoria(categoria);

        corsoRepository.save(corso);
        return true;
    }

    @Transactional
    public boolean updateCourseSchedule(Integer idCorso, List<Integer> giorni, List<LocalTime> orarioInizio, List<LocalTime> orarioFine, Integer idSala) throws EntityNotFoundException, IllegalStateException {
        Optional<Corso> corsoOpt = corsoRepository.findById(idCorso);
        if (!corsoOpt.isPresent()) {
            return false; // Course not found
        }
        Corso corso = corsoOpt.get();
        if(validateCalendarioAndSala(giorni, orarioInizio, orarioFine, idSala) == false){
            return false;
        }

        if (checkScheduleOverlap(corsoOpt, giorni, orarioInizio, orarioFine, idSala) == false) {
            return false;
        }

        // Delete existing CalendarioCorso entries for this course
        List<CalendarioCorso> existingEntries = calendarioCorsoRepository.findByCorsoId(idCorso);
        existingEntries.forEach(calendarioCorso -> calendarioCorso.setActive(false));

        // Update existing entries or create new ones and collect IDs of days to keep

        Set<CalendarioCorso> nuovoCalendarioCorso = new HashSet<>();
        for (Integer giorno : giorni) {
            Weekday weekday;
            try {
                weekday = Weekday.fromDayNumber(giorno);
                System.out.println(weekday);
            } catch (IllegalArgumentException e) {
                return false;
            }

            Optional<CalendarioCorso> existingEntryOpt = calendarioCorsoRepository.findByCorsoAndGiornoSettimanaId(idCorso, weekday);


            if (existingEntryOpt.isPresent()) {
                System.out.println(existingEntryOpt.get().getGiornoSettimana());
                CalendarioCorso existingEntry = existingEntryOpt.get();
                System.out.println(orarioInizio.get(giorno - 1));
                existingEntry.setOrarioInizio(orarioInizio.get(giorno - 1));
                existingEntry.setOrarioFine(orarioFine.get(giorno - 1));
                existingEntry.setActive(true);
                calendarioCorsoRepository.save(existingEntry);
                nuovoCalendarioCorso.add(existingEntry);
            } else {
                // Create new entry
                CalendarioCorso newCalendario = new CalendarioCorso();
                CalendarioCorsoId calendarioCorsoId = new CalendarioCorsoId();
                ;
                calendarioCorsoId.setGiornoSettimana(weekday);
                calendarioCorsoId.setIdCorso(idCorso);
                newCalendario.setId(calendarioCorsoId);
                newCalendario.setIdCorso(corso);
                newCalendario.setActive(true);
                newCalendario.setOrarioInizio(orarioInizio.get(giorno - 1));
                newCalendario.setOrarioFine(orarioFine.get(giorno - 1));
                calendarioCorsoRepository.save(newCalendario);
                nuovoCalendarioCorso.add(newCalendario);

            }
        }


        if (!corso.getIdSala().equals(idSala)) {
            if (checkRoomCapacityForCourse(idCorso, idSala) == false) {
                return false;
            }
            Sala newSala = salaRepository.findById(idSala).orElseThrow(() -> new IllegalStateException("Sala not found"));
            corso.setIdSala(newSala);

        }

        corso.setCalendarioCorso(nuovoCalendarioCorso);
        corsoRepository.save(corso);


        return true; // Successfully updated the course schedule/
    }


    @Transactional
    public Optional<Corso> findById(Integer idCorso) {
        return corsoRepository.findByIdActive(idCorso);
    }



    @Transactional(readOnly = true) //da eliminare
    public List<CalendarioCorso> findCalendarioByCorsoId(Integer corsoId) {
        return calendarioCorsoRepository.findByCorsoId(corsoId);
    }

    @Transactional
    public List<Corso> findAll() {
        return corsoRepository.findAll();
    }


    @Transactional
    public boolean updateDocenti(
            Integer idCorso,
            Optional<List<Integer>> deletedDocentiId,
            Optional<List<String>> docentiCf,
            Optional<List<Integer>> stipendiAttuali,
            Optional<List<Integer>> stipendi
    ) {
        Optional<Corso> corsoOpt = corsoRepository.findByIdActive(idCorso);
        if (!corsoOpt.isPresent()) {
            return false; // Corso non trovato
        }
        Corso corso = corsoOpt.get();

        Set<Docente> docenti = new HashSet<>();

        if(stipendiAttuali.isPresent()) {
            List<Integer> stipendiAttualiList = stipendiAttuali.get();
            if (stipendiAttualiList.size() != corso.getDocenti().size() || !validateStipendi(stipendiAttualiList)) {
                return false;
            }

            Collections.sort(stipendiAttualiList);
            List<Docente> docentiList = new ArrayList<>(corso.getDocenti());
            Collections.sort(docentiList, Comparator.comparingInt(Docente::getId)); //ordino docenti in base a id crescente
            for (int i = 0; i < docentiList.size(); i++) {
                Integer stipendio = stipendiAttualiList.get(i);
                Docente docente = docentiList.get(i);
                boolean eliminato = false;
                if (!deletedDocentiId.isEmpty()) {
                    for (Integer id : deletedDocentiId.get()) {
                        if (docente.getId() == id) {
                            eliminato = true;
                            break;
                        }
                    }
                }
                if (!eliminato) {
                    if (BigDecimal.valueOf(stipendio).compareTo(docente.getStipendio()) > 0) {
                        docente.setStipendio(BigDecimal.valueOf(stipendio));
                        docenteRepository.save(docente);
                    }
                    docenti.add(docente);
                }

            }
        }

        if (!docentiCf.isEmpty() && !stipendi.isEmpty()) {
            if (!validateDocentiAndStipendi(docentiCf.get(), stipendi.get())) {
                return false;
            }

            List<String> docentiCfList = docentiCf.get();
            List<Integer> stipendiList = stipendi.get();
            for (int i = 0; i < docentiCfList.size(); i++) {
                String cf = docentiCfList.get(i);
                // Step 3: Find User by CF
                Optional<Utente> utenteOpt = utenteRepository.findByCfNotDeleted(cf);
                if (!utenteOpt.isPresent()) {
                    return false; // Utente does not exist
                }
                Utente utente = utenteOpt.get();

                //Controlla se utente è socio
                Optional<Socio> socioOpt = socioRepository.findById(utente.getId()); //non soci cancellati
                if (!socioOpt.isPresent()) {
                    return false; // Socio does not exist
                }
                Socio socio = socioOpt.get();

                //Check if Socio is not a Docente
                if (socio.getDocente() == null) {
                    // Socio is not a Docente, proceed to save Docente information
                    Docente docente = new Docente();
                    docente.setSocio(socio);
                    docente.setStipendio(BigDecimal.valueOf(stipendiList.get(i)));
                    docente.setActive(true);
                    docenteRepository.save(docente);
                    docenti.add(docente);
                } else {
                    if (BigDecimal.valueOf(stipendiList.get(i)).compareTo(socio.getDocente().getStipendio()) > 0) {
                        Docente docente = socio.getDocente();
                        docente.setStipendio(BigDecimal.valueOf(stipendiList.get(i)));
                        docenteRepository.save(docente);
                    }
                    socio.getDocente().setActive(true);
                    docenti.add(socio.getDocente());
                }
            }

        }
        // Salvataggio delle modifiche al corso
        corsoRepository.save(corso);
        corso.setDocenti(docenti);
        if (!deletedDocentiId.isEmpty()) {
            for (Integer docenteId : deletedDocentiId.get()) {
                List<Corso> corsiInsegnati = corsoRepository.findAltriCorsiInsegnatiByDocenteId(docenteId, idCorso);
                if (corsiInsegnati.isEmpty()) {
                    Docente docente = docenteRepository.findById(docenteId).orElseThrow(() -> new IllegalStateException("Docente not found"));
                    docente.setActive(false);
                    docente.setStipendio(BigDecimal.ZERO);
                    docenteRepository.save(docente);
                }
            }
        }

        return true; // Operazione completata con successo
    }

    @Transactional
    public boolean deleteCourse(Integer idCorso){
        Optional<Corso> corsoOpt = corsoRepository.findById(idCorso);
        if (!corsoOpt.isPresent()) {
            return false; // Corso non trovato
        }
        Corso corso = corsoOpt.get();
        try{
            deletePhoto(idCorso);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        corso.setActive(false);
        corsoRepository.save(corso);
        List<Docente> docenti = new ArrayList<>(corso.getDocenti());
        for (Docente docente : docenti) {
            List<Corso> corsiInsegnati = corsoRepository.findAltriCorsiInsegnatiByDocenteId(docente.getId(), idCorso);
            if (corsiInsegnati.isEmpty()) {
                docente.setStipendio(BigDecimal.ZERO);
                docente.setActive(false);
                docenteRepository.save(docente);
            }
        }
        corso.setDocenti(null);
        corso.setSoci(null);
        List<CalendarioCorso> calendarioCorso = calendarioCorsoRepository.findByCorsoId(idCorso);
        for (CalendarioCorso calendario : calendarioCorso) {
            calendario.setActive(false);
        }
        corsoRepository.save(corso);
        return true;
    }


    public boolean checkRoomCapacityForCourse(Integer idCorso, Integer idSala) {
        // Trova il corso tramite l'id fornito
        Optional<Corso> corsoOpt = corsoRepository.findById(idCorso);
        if (!corsoOpt.isPresent()) {
            throw new EntityNotFoundException("Corso non trovato con l'ID: " + idCorso);
        }
        Corso corso = corsoOpt.get();

        // Trova la sala tramite l'id fornito
        Optional<Sala> salaOpt = salaRepository.findById(idSala);
        if (!salaOpt.isPresent()) {
            throw new EntityNotFoundException("Sala non trovata con l'ID: " + idSala);
        }
        Sala sala = salaOpt.get();

        // Calcola il numero totale di soci che frequentano il corso
        int totalParticipants = corso.getSoci().size();

        // Verifica se la capienza della sala è sufficiente per il numero di partecipanti
        return totalParticipants <= sala.getCapienza();
    }



    @Transactional
        //anche corsi non attivi
    public Optional<Corso> findByIdAll(Integer idCorso) {
        return corsoRepository.findByIdAll(idCorso);
    }

    @Transactional
        //solo corsi attivi
    public List<Corso> findCorsiByDocenteId(Integer docenteId) {
        return corsoRepository.findCorsiByDocenteId(docenteId);
    }

    @Transactional
    public boolean aggiungiCorsiBaseRuolo(HttpServletRequest request, HttpServletResponse response, Model model) {
        Optional<Socio> socioOpt = socioService.setSocioFromCookie(request, response, model);
        if (!socioOpt.isPresent()) {
            // Gestire il caso in cui il socio non è trovato
            return false;
        }
        Socio socio = socioOpt.get();
        List<Corso> corsi;
        if (socio.getDocente() != null) {
            // Il socio è un docente, quindi recupera i corsi insegnati da lui
            corsi = corsoRepository.findCorsiByDocenteId(socio.getDocente().getId()); //solo attivi
        } else if (socio.getSegretario() != null) {
            // Il socio è un segretario, quindi recupera tutti i corsi
            corsi = corsoRepository.findAllIfActiveTrue();
            System.out.println("Segretario");
        } else {
            // Gestire il caso in cui il socio non è né un docente né un segretario
            System.out.println("Errore identificativo socio");
            return false;
        }

        model.addAttribute("corsi", corsi);

        return true;
    }

    @Transactional
    List<Corso> findAllIfActiveTrue(){
        return corsoRepository.findAllIfActiveTrue();
    }


    @Transactional
    public List<Corso> findBySalaId(Integer idSala){
        return corsoRepository.findBySalaId(idSala);
    }

    @Transactional
    public List<Corso> findBySedeId(Integer idSede){
        return corsoRepository.findBySedeId(idSede);
    }

    @Transactional
    public void deletePhoto(Integer corsoId) throws Exception {
        Optional<Corso> corsoOptional = corsoRepository.findByIdActive(corsoId); //solo attivi
        if (corsoOptional.isPresent()) {
            Corso corso = corsoOptional.get();
            String photoFilename = corso.getUrlFoto();
            corso.setUrlFoto(null);
            if (photoFilename != null && !photoFilename.isEmpty()) {
                Path fileStorageLocation = Paths.get(System.getProperty("user.dir") + "/src/main/resources/static/images/corsoPhotos/"+photoFilename);
                System.out.println("Tentativo di eliminazione in: " + fileStorageLocation);
                Files.deleteIfExists(fileStorageLocation);
                System.out.println("File eliminato correttamente in" + fileStorageLocation);
            }
        } else {
            throw new Exception("Corso not found with ID: " + corsoId);
        }
    }

}



