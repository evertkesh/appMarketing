package appMarketing.ServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import appMarketing.Service.IntegranteService;
import appMarketing.entity.Equipo;
import appMarketing.entity.Integrante;
import appMarketing.repository.EquipoRepository;
import appMarketing.repository.IntegranteRepository;

@Service
public class IntegranteServiceImpl implements IntegranteService {

    @Autowired
    private IntegranteRepository integranteRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Override
    public List<Integrante> listarPorEquipo(Long equipoId) {
        return integranteRepository.findByEquipoIdAndActivoTrue(equipoId);
    }

    @Override
    public List<Integrante> listarActivos() {
        return integranteRepository.findByActivoTrue();
    }

    @Override
    public Optional<Integrante> findById(Long id) {
        return integranteRepository.findById(id);
    }

    @Override
    public Integrante guardar(Integrante integrante) {
        if (integrante.getFechaIngreso() == null) {
            integrante.setFechaIngreso(LocalDate.now());
        }
        return integranteRepository.save(integrante);
    }

    @Override
    public void eliminar(Long id, String motivo) {
        Optional<Integrante> integrante = integranteRepository.findById(id);
        integrante.ifPresent(i -> {
            i.setActivo(false);
            i.setFechaSalida(LocalDate.now());
            i.setMotivoSalida(motivo);
            integranteRepository.save(i);
        });
    }

    @Override
    public void reasignarEquipo(Long integranteId, Long nuevoEquipoId) {
        Optional<Integrante> integrante = integranteRepository.findById(integranteId);
        Optional<Equipo> equipo = equipoRepository.findById(nuevoEquipoId);

        if (integrante.isPresent() && equipo.isPresent()) {
            integrante.get().setEquipo(equipo.get());
            integranteRepository.save(integrante.get());
        }
    }
}