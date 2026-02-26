package appMarketing.Service;


import java.util.List;
import java.util.Optional;

import appMarketing.entity.Equipo;

public interface EquipoService {
    List<Equipo> listarTodos();
    Optional<Equipo> findById(Long id);
    Equipo guardar(Equipo equipo);
    void eliminar(Long id);
    
    // Cambia a List
    List<Equipo> findByJefeEquipoId(Long jefeEquipoId);
}