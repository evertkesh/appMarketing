package appMarketing.Service;

import java.time.LocalDate;
import java.util.List;

import appMarketing.dto.RegistroAsistenciaDTO;
import appMarketing.entity.RegistroAsistencia;

public interface RegistroAsistenciaService {
    
    void registrarEntrada(Long integranteId);
    
    void registrarSalida(Long integranteId);
    
    List<RegistroAsistencia> listarPorFecha(LocalDate fecha);
    
    List<RegistroAsistencia> listarPorIntegranteYPeriodo(Long integranteId, LocalDate inicio, LocalDate fin);
    
    // Nuevos métodos según requerimientos
    List<RegistroAsistenciaDTO> obtenerHistorialHoy();
    
    Long contarPersonalPresente();
    
    Long contarTotalRegistrosHoy();
}
