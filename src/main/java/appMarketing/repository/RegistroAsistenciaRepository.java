package appMarketing.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import appMarketing.entity.RegistroAsistencia;

@Repository
public interface RegistroAsistenciaRepository extends JpaRepository<RegistroAsistencia, Long> {
    List<RegistroAsistencia> findByFecha(LocalDate fecha);
    List<RegistroAsistencia> findByIntegranteIdAndFechaBetween(Long integranteId, LocalDate inicio, LocalDate fin);
    
    // Buscar registro de hoy para un integrante específico
    @Query("SELECT r FROM RegistroAsistencia r WHERE r.integrante.id = :integranteId AND r.fecha = :fecha")
    Optional<RegistroAsistencia> findRegistroPorIntegranteYFecha(@Param("integranteId") Long integranteId, @Param("fecha") LocalDate fecha);
    
    // Contar personal presente (con entrada pero sin salida)
    @Query("SELECT COUNT(r) FROM RegistroAsistencia r WHERE r.fecha = :fecha AND r.horaEntrada IS NOT NULL AND r.horaSalida IS NULL")
    Long contarPersonalPresente(@Param("fecha") LocalDate fecha);
    
    // Contar total de registros del día
    @Query("SELECT COUNT(r) FROM RegistroAsistencia r WHERE r.fecha = :fecha")
    Long contarTotalRegistrosHoy(@Param("fecha") LocalDate fecha);
    
    // Obtener historial de hoy con información del integrante
    @Query("SELECT r FROM RegistroAsistencia r JOIN FETCH r.integrante WHERE r.fecha = :fecha ORDER BY r.horaEntrada DESC")
    List<RegistroAsistencia> obtenerHistorialHoy(@Param("fecha") LocalDate fecha);
}
