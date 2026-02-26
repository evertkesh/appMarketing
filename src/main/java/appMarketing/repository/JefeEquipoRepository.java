package appMarketing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import appMarketing.entity.JefeEquipo;

@Repository
public interface JefeEquipoRepository extends JpaRepository<JefeEquipo, Long> {
    List<JefeEquipo> findByActivoTrue();
}
