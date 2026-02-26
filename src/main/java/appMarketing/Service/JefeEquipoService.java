package appMarketing.Service;

import java.util.List;
import java.util.Optional;

import appMarketing.entity.Equipo;
import appMarketing.entity.JefeEquipo;

public interface JefeEquipoService {
    List<JefeEquipo> listarTodos();
    Optional<JefeEquipo> findById(Long id);
    JefeEquipo guardar(JefeEquipo jefe);
    void eliminar(Long id);
    void enviarCorreo(Long jefeEquipoId, String asunto, String mensaje);
    void enviarAviso(Long jefeEquipoId, String aviso);
}