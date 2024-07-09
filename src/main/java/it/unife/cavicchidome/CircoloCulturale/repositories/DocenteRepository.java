package it.unife.cavicchidome.CircoloCulturale.repositories;

import it.unife.cavicchidome.CircoloCulturale.models.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocenteRepository extends JpaRepository<Docente, Integer> {
    @Query("SELECT d FROM Docente d JOIN d.corsi c WHERE c.id = :corsoId")
    Optional<List<Docente>> findDocentiByCorsoId(Integer corsoId); //List<Object[]> findDocentiByCorsoId(Integer corsoId);

    @Query("SELECT d FROM Docente d JOIN d.socio s JOIN s.utente u WHERE u.cf = :cf")
    Optional<Docente> findByCf(String cf);


}