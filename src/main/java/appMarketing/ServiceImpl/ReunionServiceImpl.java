package appMarketing.ServiceImpl;

import java.util.List;
import java.util.Optional;
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
    public Optional<Reunion> findById(Long id) {
        return reunionRepository.findById(id);
    }

    @Override
    public Reunion organizarReunion(Reunion reunion) {
        // Validar que no exista una reunión duplicada para el mismo equipo en la misma fecha y hora
        if (reunion.getEquipo() != null && reunion.getFechaHora() != null) {
            Optional<Reunion> reunionExistente = reunionRepository.findByEquipoIdAndFechaHora(
                reunion.getEquipo().getId(), 
                reunion.getFechaHora()
            );
            
            // Si existe y no es la misma reunión (en caso de actualización), lanzar excepción
            if (reunionExistente.isPresent() && 
                (reunion.getId() == null || !reunionExistente.get().getId().equals(reunion.getId()))) {
                throw new IllegalArgumentException("Ya existe una reunión programada para este equipo en la misma fecha y hora");
            }
        }
        
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
        // Verificar que la reunión existe antes de eliminarla
        Optional<Reunion> reunion = reunionRepository.findById(id);
        if (reunion.isPresent()) {
            reunionRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("La reunión con ID " + id + " no existe");
        }
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