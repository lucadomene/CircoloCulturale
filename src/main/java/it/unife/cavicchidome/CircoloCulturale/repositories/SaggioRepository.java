package it.unife.cavicchidome.CircoloCulturale.repositories;

import it.unife.cavicchidome.CircoloCulturale.models.Saggio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaggioRepository extends JpaRepository<Saggio, Integer> {
    @Query("""
       SELECT s FROM Saggio s WHERE s.data <= ?1
    """)
    public List<Saggio> getNextSaggi(LocalDate untilDate);

    @Query("SELECT s FROM Saggio s WHERE s.data = ?1")
    public Optional<Saggio> getSaggioByData(LocalDate data);
}