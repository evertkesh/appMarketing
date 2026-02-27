package appMarketing.Service;

import java.util.List;
import java.util.Optional;

import appMarketing.dto.ReunionDTO;
import appMarketing.entity.Reunion;

public interface ReunionService {
    List<Reunion> listarTodas();
    List<Reunion> listarPorEquipo(Long equipoId);
    Optional<Reunion> findById(Long id);
    Reunion organizarReunion(Reunion reunion);
    void cancelarReunion(Long id);
    List<ReunionDTO> listarTodasDTO();
}