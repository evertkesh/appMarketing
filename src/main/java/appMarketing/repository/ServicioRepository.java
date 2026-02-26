package appMarketing.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import appMarketing.entity.Servicio;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByEstado(Servicio.EstadoServicio estado);

    @Query("SELECT COALESCE(SUM(s.precioBase),0) " +
    	       "FROM Servicio s " +
    	       "WHERE s.estado = 'COMPLETADO'")
    	BigDecimal sumarGananciasCompletadas();
}