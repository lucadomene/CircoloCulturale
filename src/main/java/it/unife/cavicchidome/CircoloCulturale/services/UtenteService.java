package it.unife.cavicchidome.CircoloCulturale.services;

import it.unife.cavicchidome.CircoloCulturale.exceptions.EntityAlreadyPresentException;
import it.unife.cavicchidome.CircoloCulturale.exceptions.ValidationException;
import it.unife.cavicchidome.CircoloCulturale.models.Utente;
import it.unife.cavicchidome.CircoloCulturale.repositories.UtenteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UtenteService {
    UtenteRepository utenteRepository;

    UtenteService(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    Utente validateUtente(Utente utente) throws ValidationException {
        String[] indirizzoSplit = utente.getIndirizzo().split(", ");
        return validateAndParseUtente(utente.getNome(), utente.getCognome(), utente.getCf(), utente.getDataNascita(), utente.getLuogoNascita(), indirizzoSplit[0], indirizzoSplit[1], indirizzoSplit[2], indirizzoSplit[3], indirizzoSplit[4]);
    }

    Utente validateAndParseUtente(String name,
                                  String surname,
                                  String cf,
                                  LocalDate dob,
                                  String birthplace,
                                  String country,
                                  String province,
                                  String city,
                                  String street,
                                  String houseNumber) throws ValidationException {

        // Aggiungi controlli per stringhe vuote
        if (name == null || name.isEmpty()
                || surname == null || surname.isEmpty()
                || cf == null || cf.isEmpty()
                || dob == null || birthplace == null || birthplace.isEmpty()
                || country == null || country.isEmpty()
                || province == null || province.isEmpty()
                || city == null || city.isEmpty()
                || street == null || street.isEmpty()
                || houseNumber == null || houseNumber.isEmpty()) {
            throw new ValidationException("Campi obbligatori non compilati");
        }

        // Controlla che nome e cognome abbiano al massimo 20 caratteri
        if (name.length() > 20 || surname.length() > 20) {
            throw new ValidationException("Nome o cognome troppo lunghi");
        }

        // Controlla che il codice fiscale abbia esattamente 16 caratteri
        if (cf.length() != 16) {
            throw new ValidationException("Il codice fiscale deve essere composto da 16 caratteri");
        }

        // Controlla che la data di nascita sia odierna o antecedente
        if (dob.isAfter(LocalDate.now())) {
            throw new ValidationException("Data di nascita successiva alla data attuale");
        }

        // Controlla che il luogo di nascita abbia al massimo 20 caratteri
        if (birthplace.length() > 20) {
            throw new ValidationException("Luogo di nascita troppo lungo");
        }

        // Controlla che l'indirizzo abbia al massimo 80 caratteri
        if ((country.length() + province.length() + city.length() + street.length() + houseNumber.length() ) > 80) {
            throw new ValidationException("Indirizzo troppo lungo");
        }

        String regex = "^(?=.*[A-Za-z])[A-Za-z\\s\\'\\-àèéìòùÀÈÉÌÒÙáéíóúÁÉÍÓÚâêîôûÂÊÎÔÛäëïöüÿÄËÏÖÜŸ]+$";
        /* almeno un carattere alfabetico (maiuscolo o minuscolo) e possono includere spazi, apostrofi, trattini.
             Anche lettere accentate
             */
        if (!name.matches(regex) || !surname.matches(regex) || !birthplace.matches(regex) || !country.matches(regex) || !province.matches(regex) || !city.matches(regex) || !street.matches(regex)) {
            throw new ValidationException("Campi non validi");
        }


        String houseNumberRegex = "^(?=.*[0-9])[0-9a-zA-Z/]+$";;
        if (houseNumber.isEmpty() || houseNumber== null || !houseNumber.matches(houseNumberRegex)) {
            throw new ValidationException("Numero civico non valido");
        }

        // Controlla che il codice fiscale sia composto sia di numeri che di lettere
        String cfRegex = "^[0-9a-zA-Z]+$";
        if (!cf.matches(cfRegex)) {
            throw new ValidationException("Codice fiscale non valido");
        }

        return new Utente(cf, dob, birthplace, name, surname, country + ", " + province + ", " + city + ", " + street + ", " + houseNumber, false);
    }

    @Transactional
    public Utente editUtente(Integer utenteId,
                             Optional<String> name,
                             Optional<String> surname,
                             Optional<String> cf,
                             Optional<LocalDate> dob,
                             Optional<String> birthplace,
                             Optional<String> country,
                             Optional<String> province,
                             Optional<String> city,
                             Optional<String> street,
                             Optional<String> houseNumber) throws ValidationException, EntityNotFoundException {
        Utente utente = utenteRepository.getReferenceById(utenteId);
        name.ifPresent(utente::setNome);
        surname.ifPresent(utente::setCognome);
        cf.ifPresent(utente::setCf);
        dob.ifPresent(utente::setDataNascita);
        birthplace.ifPresent(utente::setLuogoNascita);
        if (country.isPresent() && province.isPresent() && city.isPresent() && street.isPresent() && houseNumber.isPresent()) {
            utente.setIndirizzo(country.get() + ", " + province.get() + ", " + city.get() + ", " + street.get() + ", " + houseNumber.get());
        }
        validateUtente(utente);
        return utenteRepository.save(utente);
    }

    @Transactional
    public Utente newUtente(String name,
                            String surname,
                            String cf,
                            LocalDate dob,
                            String birthplace,
                            String country,
                            String province,
                            String city,
                            String street,
                            String houseNumber) throws ValidationException, EntityAlreadyPresentException {
        Optional<Utente> alreadyPresent = utenteRepository.findByCf(cf);
        if (alreadyPresent.isPresent()) {
            throw new EntityAlreadyPresentException(alreadyPresent.get());
        }

        Utente utente = validateAndParseUtente(name, surname, cf, dob, birthplace, country, province, city, street, houseNumber);
        utente.setDeleted(false);

        return utenteRepository.save(utente);
    }

    @Transactional
    public Optional<Utente> findById(Integer utenteId) {
        return utenteRepository.findById(utenteId);
    }

    @Transactional
    public Optional<Utente> findByCf(String cf) {
        return utenteRepository.findByCf(cf);
    }


}
