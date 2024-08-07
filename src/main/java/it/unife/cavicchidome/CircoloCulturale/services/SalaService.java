package it.unife.cavicchidome.CircoloCulturale.services;

import it.unife.cavicchidome.CircoloCulturale.exceptions.EntityAlreadyPresentException;
import it.unife.cavicchidome.CircoloCulturale.models.*;
import it.unife.cavicchidome.CircoloCulturale.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SalaService {
    private final SalaRepository salaRepository;
    private final SedeRepository sedeRepository;
    private final PrenotazioneSalaRepository prenotazioneSalaRepository;
    private final CorsoRepository corsoRepository;
    private final CorsoService corsoService;
    private final SocioRepository socioRepository;

    public SalaService(SalaRepository salaRepository, SedeRepository sedeRepository, PrenotazioneSalaRepository prenotazioneSalaRepository, CorsoRepository corsoRepository, CorsoService corsoService, SocioRepository socioRepository) {
        this.salaRepository = salaRepository;
        this.sedeRepository = sedeRepository;
        this.prenotazioneSalaRepository = prenotazioneSalaRepository;
        this.corsoRepository = corsoRepository;
        this.corsoService = corsoService;
        this.socioRepository = socioRepository;
    }

    @Transactional(readOnly = true)
    public List<Sala> findAll() {
        return salaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Sala> findAllPrenotabili() {
        return salaRepository.findAllPrenotabili();
    }

    @Transactional(readOnly = true)
    public Optional<Sala> findById(Integer idSala) {
        return salaRepository.findById(idSala);
    }

    public boolean validateSalaInfo(String numeroSala, Integer capienza, String descrizione) {
        String descrizionePattern = "^(?=.*[A-Za-z])[A-Za-z\\s\\'\\-\\(\\)\\.\\,\\;\\:\\!\\?\\[\\]\\{\\}\"\\-àèéìòùÀÈÉÌÒÙáéíóúÁÉÍÓÚâêîôûÂÊÎÔÛäëïöüÿÄËÏÖÜŸ]+$";
        /* almeno un carattere alfabetico (maiuscolo o minuscolo) e possono includere spazi, apostrofi, trattini e, nel caso di charDescrizioneRegex,
             anche parentesi, punti, virgole, punto e virgola, due punti, punti esclamativi, punti interrogativi, parentesi quadre, parentesi graffe, e virgolette.
             Anche lettere accentate
             */

        // Controlla se numeroSala o capienza sono nulli
        if (numeroSala == null || capienza == null) {
            return false;
        }
        if (!descrizione.isEmpty() && descrizione != null && !descrizione.matches(descrizionePattern)) {
            System.out.println("descrizione: " + descrizione);
            return false;
        }

        // Controlla se numeroSala è un numero valido (non negativo)
        if (Integer.parseInt(numeroSala) < 0) {
            return false;
        }

        // Controlla se capienza è un intero positivo
        if (capienza <= 0) {
            return false;
        }

        // Se tutte le condizioni sono soddisfatte, ritorna true
        return true;
    }

    public boolean newSala(
            String numeroSala,
            Integer capienza,
            String descrizione,
            Boolean prenotabile,
            Integer idSede
    ) throws IllegalArgumentException {
        if(!validateSalaInfo(numeroSala, capienza, descrizione)) {
            return false;
        }

        Optional<Sede> sedeOptional = sedeRepository.findById(idSede);
        if (!sedeOptional.isPresent()) {
            throw new IllegalArgumentException("Sede with the provided ID does not exist.");
        }
        Optional<Sala> salaOptional = salaRepository.findByNumeroSalaAndIdSedeEvenIfNotActive(Integer.parseInt(numeroSala), sedeOptional.get());
        Sala newSala;
        if(salaOptional.isPresent()){
            if(salaOptional.get().getActive() == false){
                newSala = salaOptional.get();
                newSala.setActive(true);
            }
            else{
                throw new IllegalArgumentException("Sala già presente");
            }
        }else{
            newSala = new Sala();
        }

        newSala.setNumeroSala(Integer.parseInt(numeroSala));
        newSala.setCapienza(capienza);
        if(descrizione != null && !descrizione.isEmpty()) {
            newSala.setDescrizione(descrizione);
        }else{
            newSala.setDescrizione(null);
        }
        newSala.setPrenotabile(prenotabile);
        newSala.setActive(true);
        newSala.setIdSede(sedeOptional.get());

        // Save the Sala instance to the repository
        Sala savedSala = salaRepository.save(newSala);


        return true;
    }
    @Transactional(readOnly = true)
    public Optional<Sala> findByNumeroSalaAndIdSedeActive(Integer numeroSala, Sede idSede){
        return salaRepository.findByNumeroSalaAndIdSedeActive(numeroSala, idSede);
    }

    @Transactional(readOnly = true)
    public Optional<Sala> findByNumeroSalaAndIdSedeEvenIfNotActive(Integer numeroSala, Sede idSede){
        return salaRepository.findByNumeroSalaAndIdSedeEvenIfNotActive(numeroSala, idSede);
    }

    @Transactional(readOnly = true)
    public List<Sala> findAllBySedeId(Integer idSede) { return salaRepository.findAllActiveBySedeId(idSede); }

    public boolean updateSala(Integer idSala, String numeroSala, String descrizione, Boolean prenotabile) {
        try {
            Optional<Sala> salaOpt = salaRepository.findById(idSala);
            if (!salaOpt.isPresent()) {
                return false;
            }
            if(!validateSalaInfo(numeroSala, salaOpt.get().getCapienza(), descrizione)){
                return false;
            }
            Optional<Sala> salaOptNumero = salaRepository.findByNumeroSalaAndIdSedeActive(Integer.parseInt(numeroSala), salaOpt.get().getIdSede());
            if (salaOptNumero.isPresent() && !salaOptNumero.get().getId().equals(idSala)) {
                throw new RuntimeException("Sala già presente");
            }
            Sala sala = salaOpt.get();
            sala.setNumeroSala(Integer.parseInt(numeroSala));
            sala.setActive(true);
            if(descrizione != null && !descrizione.isEmpty())
                sala.setDescrizione(descrizione);
            if(prenotabile == false){
                List<PrenotazioneSala> prenotazioni = prenotazioneSalaRepository.findBySala(idSala);
                if (!prenotazioni.isEmpty()) {
                    for (PrenotazioneSala prenotazione : prenotazioni) {
                        prenotazione.setDeleted(true);
                        prenotazioneSalaRepository.save(prenotazione);
                    }
                }
            }
            sala.setPrenotabile(prenotabile);
            salaRepository.save(sala);
            return true;
        } catch (Exception e) {
            // Log the exception
            System.out.println("Error updating Sala: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean deleteSala(Integer idSala) {
        try {
            Optional<Sala> salaOpt = salaRepository.findById(idSala);
            if (!salaOpt.isPresent()) {
                return false;
            }
            Sala sala = salaOpt.get();
            List<Corso> corsi = corsoRepository.findBySalaId(idSala);
            if(!corsi.isEmpty()){
                for(Corso corso : corsi){
                    corsoService.deleteCourse(corso.getId());
                }
            }
            List<PrenotazioneSala> prenotazioni = prenotazioneSalaRepository.findBySala(idSala); //solo prenotazioni e sale attive
            if (!prenotazioni.isEmpty()) {
                for (PrenotazioneSala prenotazione : prenotazioni) {
                    prenotazione.setDeleted(true);
                    prenotazioneSalaRepository.save(prenotazione);
                }
            }
            sala.setActive(false);
            sala.setPrenotabile(false);
            salaRepository.save(sala);
            return true;
        } catch (Exception e) {
            // Log eccezione
            System.out.println("Error deleting Sala: "  + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<Sala> findAllIfActive() {
        return salaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Sede> findDistinctSedi() { return salaRepository.findDistinctSedi(); }


}
