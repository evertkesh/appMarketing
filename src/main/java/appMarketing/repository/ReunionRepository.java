package appMarketing.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import appMarketing.entity.Reunion;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {
    List<Reunion> findByEquipoId(Long equipoId);
    
    @Query("SELECT r FROM Reunion r WHERE r.equipo.id = :equipoId AND r.fechaHora = :fechaHora")
    Optional<Reunion> findByEquipoIdAndFechaHora(@Param("equipoId") Long equipoId, @Param("fechaHora") LocalDateTime fechaHora);
}
