package appMarketing.ServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import appMarketing.Service.JefeEquipoService;
import appMarketing.Service.ReunionService;
import appMarketing.dto.ReunionDTO;
import appMarketing.entity.Reunion;
import appMarketing.repository.ReunionRepository;

@Service
public class ReunionServiceImpl implements ReunionService {

    @Autowired
    private ReunionRepository reunionRepository;

    @Autowired
    private JefeEquipoService jefeEquipoService;

    @Override
    public List<Reunion> listarTodas() {
        return reunionRepository.findAll();
    }

    @Override
    public List<Reunion> listarPorEquipo(Long equipoId) {
        return reunionRepository.findByEquipoId(equipoId);
    }

    @Override
    public Reunion organizarReunion(Reunion reunion) {
        Reunion saved = reunionRepository.save(reunion);

        if (reunion.isNotificarJefe() && reunion.getEquipo() != null
            && reunion.getEquipo().getJefeEquipo() != null) {
            String mensaje = "Se ha organizado una reunión: " + reunion.getTitulo()
                           + " para el " + reunion.getFechaHora();
            jefeEquipoService.enviarAviso(reunion.getEquipo().getJefeEquipo().getId(), mensaje);
        }

        return saved;
    }

    @Override
    public void cancelarReunion(Long id) {
        reunionRepository.deleteById(id);
    }

    @Override
    public List<ReunionDTO> listarTodasDTO() {
        return reunionRepository.findAll().stream()
            .map(reunion -> new ReunionDTO(
                reunion.getId(),
                reunion.getTitulo(),
                reunion.getFechaHora(),
                reunion.getDescripcion(),
                reunion.getLugar(),
                reunion.getEquipo() != null ? reunion.getEquipo().getNombre() : "Sin equipo",
                reunion.getOrganizador() != null ? reunion.getOrganizador().getNombreCompleto() : "Sin organizador",
                reunion.isNotificarJefe()
            ))
            .collect(Collectors.toList());
    }
}