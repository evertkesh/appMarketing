package appMarketing.Service;

import java.util.List;

import appMarketing.dto.ReunionDTO;
import appMarketing.entity.Reunion;

public interface ReunionService {
    List<Reunion> listarTodas();
    List<Reunion> listarPorEquipo(Long equipoId);
    Reunion organizarReunion(Reunion reunion);
    void cancelarReunion(Long id);
    List<ReunionDTO> listarTodasDTO();
}