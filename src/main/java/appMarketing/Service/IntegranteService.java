package appMarketing.Service;

import java.util.List;
import java.util.Optional;

import appMarketing.entity.Integrante;

public interface IntegranteService {
    List<Integrante> listarPorEquipo(Long equipoId);
    List<Integrante> listarActivos();
    Optional<Integrante> findById(Long id);
    Integrante guardar(Integrante integrante);
    void eliminar(Long id, String motivo);
    void reasignarEquipo(Long integranteId, Long nuevoEquipoId);
}
