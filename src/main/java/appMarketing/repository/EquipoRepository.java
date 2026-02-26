package appMarketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import appMarketing.entity.Equipo;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    
    // Cambia Optional a List
    List<Equipo> findByJefeEquipoId(Long jefeEquipoId);
}