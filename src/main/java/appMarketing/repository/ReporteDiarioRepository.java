package appMarketing.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import appMarketing.entity.ReporteDiario;

@Repository
public interface ReporteDiarioRepository extends JpaRepository<ReporteDiario, Long> {
    List<ReporteDiario> findByFecha(LocalDate fecha);

    @Query("SELECT SUM(r.gananciaDia) FROM ReporteDiario r WHERE r.fecha = ?1")
    BigDecimal sumGananciasByFecha(LocalDate fecha);
}