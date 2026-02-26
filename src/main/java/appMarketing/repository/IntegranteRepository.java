package appMarketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import appMarketing.entity.Integrante;

@Repository
public interface IntegranteRepository extends JpaRepository<Integrante, Long> {
    List<Integrante> findByEquipoIdAndActivoTrue(Long equipoId);
    List<Integrante> findByActivoTrue();
    Optional<Integrante> findByEmail(String email);
}
