package appMarketing.ServiceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import appMarketing.Service.EquipoService;
import appMarketing.entity.Equipo;
import appMarketing.repository.EquipoRepository;

@Service
public class EquipoServiceImpl implements EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Override
    public List<Equipo> listarTodos() {
        return equipoRepository.findAll();
    }

    @Override
    public Optional<Equipo> findById(Long id) {
        return equipoRepository.findById(id);
    }

    @Override
    public Equipo guardar(Equipo equipo) {
        return equipoRepository.save(equipo);
    }

    @Override
    public void eliminar(Long id) {
        equipoRepository.deleteById(id);
    }


    @Override
    public List<Equipo> findByJefeEquipoId(Long jefeEquipoId) {
        return equipoRepository.findByJefeEquipoId(jefeEquipoId);
    }
}