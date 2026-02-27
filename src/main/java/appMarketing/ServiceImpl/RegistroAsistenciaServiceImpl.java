package appMarketing.ServiceImpl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import appMarketing.Service.RegistroAsistenciaService;
import appMarketing.dto.RegistroAsistenciaDTO;
import appMarketing.entity.Integrante;
import appMarketing.entity.RegistroAsistencia;
import appMarketing.repository.IntegranteRepository;
import appMarketing.repository.RegistroAsistenciaRepository;

@Service
@Transactional
public class RegistroAsistenciaServiceImpl implements RegistroAsistenciaService {

    private static final ZoneId PERU_ZONE = ZoneId.of("America/Lima");

    @Autowired
    private RegistroAsistenciaRepository registroRepository;

    @Autowired
    private IntegranteRepository integranteRepository;

    @Override
    public void registrarEntrada(Long integranteId) {
        if (integranteId == null) {
            throw new IllegalArgumentException("integranteId no puede ser null");
        }
        
        // Verificar si el integrante existe
        Integrante integrante = integranteRepository.findById(integranteId)
            .orElseThrow(() -> new IllegalArgumentException("Integrante no encontrado"));
        
        // Usar fecha y hora de Perú
        ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
        LocalDate hoy = ahoraEnPeru.toLocalDate();
        LocalTime ahora = ahoraEnPeru.toLocalTime();
        
        // Verificar si ya existe registro para hoy
        Optional<RegistroAsistencia> registroExistente = registroRepository
            .findRegistroPorIntegranteYFecha(integranteId, hoy);
        
        if (registroExistente.isPresent()) {
            RegistroAsistencia registro = registroExistente.get();
            if (registro.getHoraEntrada() != null) {
                throw new IllegalStateException("Ya registró entrada hoy a las " + registro.getHoraEntrada());
            }
        }
        
        // Crear nuevo registro
        RegistroAsistencia registro = new RegistroAsistencia();
        registro.setIntegrante(integrante);
        registro.setFecha(hoy);
        registro.setHoraEntrada(ahora);
        
        // Para trabajo asincrónico, siempre marcar como PRESENTE
        // Sin importar la hora de entrada (pueden conectarse a cualquier hora)
        registro.setEstado(RegistroAsistencia.Estado.PRESENTE);
        
        registroRepository.save(registro);
    }

    @Override
    public void registrarSalida(Long integranteId) {
        if (integranteId == null) {
            throw new IllegalArgumentException("integranteId no puede ser null");
        }
        
        // Usar fecha y hora de Perú
        ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
        LocalDate hoy = ahoraEnPeru.toLocalDate();
        LocalTime horaSalida = ahoraEnPeru.toLocalTime();
        
        // Buscar registro de hoy
        Optional<RegistroAsistencia> registroOpt = registroRepository
            .findRegistroPorIntegranteYFecha(integranteId, hoy);
        
        if (!registroOpt.isPresent()) {
            throw new IllegalStateException("No existe registro de entrada para hoy");
        }
        
        RegistroAsistencia registro = registroOpt.get();
        
        if (registro.getHoraSalida() != null) {
            throw new IllegalStateException("Ya registró salida hoy a las " + registro.getHoraSalida());
        }
        
        // Registrar salida
        registro.setHoraSalida(horaSalida);
        
        // El estado (PRESENTE/TARDANZA) ya se definió al registrar la entrada, no se cambia aquí.
        
        registroRepository.save(registro);
    }

    @Override
    public List<RegistroAsistencia> listarPorFecha(LocalDate fecha) {
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        return registroRepository.findByFecha(fecha);
    }

    @Override
    public List<RegistroAsistencia> listarPorIntegranteYPeriodo(Long integranteId, LocalDate inicio, LocalDate fin) {
        if (integranteId == null || inicio == null || fin == null) {
            throw new IllegalArgumentException("Los parámetros no pueden ser null");
        }
        return registroRepository.findByIntegranteIdAndFechaBetween(integranteId, inicio, fin);
    }

    @Override
    public List<RegistroAsistenciaDTO> obtenerHistorialHoy() {
        // Usar fecha de Perú
        ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
        LocalDate hoy = ahoraEnPeru.toLocalDate();
        
        List<RegistroAsistencia> registros = registroRepository.obtenerHistorialHoy(hoy);
        
        return registros.stream()
            .map(registro -> {
                RegistroAsistenciaDTO dto = new RegistroAsistenciaDTO();
                dto.setId(registro.getId());
                dto.setIntegranteName(registro.getIntegrante() != null ? 
                    registro.getIntegrante().getNombreCompleto() : "N/A");
                dto.setHoraEntrada(registro.getHoraEntrada());
                dto.setHoraSalida(registro.getHoraSalida());
                dto.setEstado(registro.getEstado() != null ? registro.getEstado().toString() : "N/A");
                
                // Formato descriptivo
                if (registro.getEstado() != null) {
                    switch (registro.getEstado()) {
                        case PRESENTE:
                            dto.setEstadoFormatted("Presente");
                            break;
                        case TARDANZA:
                            dto.setEstadoFormatted("Tardanza");
                            break;
                        case AUSENTE:
                            dto.setEstadoFormatted("Ausente");
                            break;
                        default:
                            dto.setEstadoFormatted("N/A");
                    }
                }
                
                // Calcular tiempo activo
                if (registro.getHoraEntrada() != null) {
                    LocalTime fin = registro.getHoraSalida() != null ? registro.getHoraSalida() : LocalTime.now();
                    long minutos = Duration.between(registro.getHoraEntrada(), fin).toMinutes();
                    if (minutos < 0) minutos = 0;
                    long horas = minutos / 60;
                    long mins = minutos % 60;
                    if (horas > 0) {
                        dto.setTiempoActivo(horas + "h " + mins + "min");
                    } else {
                        dto.setTiempoActivo(mins + " min");
                    }
                } else {
                    dto.setTiempoActivo("--");
                }
                
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Long contarPersonalPresente() {
        ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
        LocalDate hoy = ahoraEnPeru.toLocalDate();
        return registroRepository.contarPersonalPresente(hoy);
    }

    @Override
    public Long contarTotalRegistrosHoy() {
        ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
        LocalDate hoy = ahoraEnPeru.toLocalDate();
        return registroRepository.contarTotalRegistrosHoy(hoy);
    }
    
    @Override
    public List<RegistroAsistenciaDTO> obtenerHistorialPorFecha(LocalDate fecha) {
        if (fecha == null) {
            ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
            fecha = ahoraEnPeru.toLocalDate();
        }
        
        List<RegistroAsistencia> registros = registroRepository.obtenerHistorialHoy(fecha);
        
        return registros.stream()
            .map(registro -> {
                RegistroAsistenciaDTO dto = new RegistroAsistenciaDTO();
                dto.setId(registro.getId());
                dto.setIntegranteName(registro.getIntegrante() != null ? 
                    registro.getIntegrante().getNombreCompleto() : "N/A");
                dto.setHoraEntrada(registro.getHoraEntrada());
                dto.setHoraSalida(registro.getHoraSalida());
                dto.setEstado(registro.getEstado() != null ? registro.getEstado().toString() : "N/A");
                
                // Formato descriptivo
                if (registro.getEstado() != null) {
                    switch (registro.getEstado()) {
                        case PRESENTE:
                            dto.setEstadoFormatted("Presente");
                            break;
                        case TARDANZA:
                            dto.setEstadoFormatted("Tardanza");
                            break;
                        case AUSENTE:
                            dto.setEstadoFormatted("Ausente");
                            break;
                        default:
                            dto.setEstadoFormatted("N/A");
                    }
                }
                
                // Calcular tiempo activo
                if (registro.getHoraEntrada() != null) {
                    LocalTime fin = registro.getHoraSalida() != null ? registro.getHoraSalida() : LocalTime.now();
                    long minutos = Duration.between(registro.getHoraEntrada(), fin).toMinutes();
                    if (minutos < 0) minutos = 0;
                    long horas = minutos / 60;
                    long mins = minutos % 60;
                    if (horas > 0) {
                        dto.setTiempoActivo(horas + "h " + mins + "min");
                    } else {
                        dto.setTiempoActivo(mins + " min");
                    }
                } else {
                    dto.setTiempoActivo("--");
                }
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Long contarPersonalPresentePorFecha(LocalDate fecha) {
        if (fecha == null) {
            ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
            fecha = ahoraEnPeru.toLocalDate();
        }
        return registroRepository.contarPersonalPresente(fecha);
    }
    
    @Override
    public Long contarTotalRegistrosPorFecha(LocalDate fecha) {
        if (fecha == null) {
            ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
            fecha = ahoraEnPeru.toLocalDate();
        }
        return registroRepository.contarTotalRegistrosHoy(fecha);
    }
}
