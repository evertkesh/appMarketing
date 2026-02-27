package appMarketing.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import appMarketing.Service.RegistroAsistenciaService;
import appMarketing.entity.Integrante;
import appMarketing.entity.RegistroAsistencia;
import appMarketing.entity.Usuario;
import appMarketing.repository.IntegranteRepository;
import appMarketing.repository.RegistroAsistenciaRepository;
import appMarketing.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/asistencia")
public class AsistenciaController {

    private static final ZoneId PERU_ZONE = ZoneId.of("America/Lima");

    @Autowired
    private RegistroAsistenciaService asistenciaService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private IntegranteRepository integranteRepository;
    
    @Autowired
    private RegistroAsistenciaRepository registroAsistenciaRepository;

    // Endpoint para obtener integrantes agrupados por sección (rol)
    @GetMapping("/integrantes-por-seccion")
    @ResponseBody
    public ResponseEntity<Map<String, List<Map<String, Object>>>> obtenerIntegrantesPorSeccion() {
        try {
            List<Integrante> integrantes = integranteRepository.findByActivoTrue();
            
            // Agrupar por función/rol
            Map<String, List<Map<String, Object>>> agrupado = integrantes.stream()
                .collect(Collectors.groupingBy(
                    i -> i.getFuncion() != null ? i.getFuncion() : "Sin Función",
                    Collectors.mapping(i -> {
                        Map<String, Object> mapa = new HashMap<>();
                        mapa.put("id", i.getId());
                        mapa.put("nombre", i.getNombreCompleto());
                        mapa.put("email", i.getEmail());
                        mapa.put("funcion", i.getFuncion());
                        return mapa;
                    }, Collectors.toList())
                ));
            
            return ResponseEntity.ok(agrupado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint para obtener integrantes que ya tienen entrada hoy (presentes en turno)
    @GetMapping("/presentes-en-turno")
    @ResponseBody
    public ResponseEntity<Map<String, List<Map<String, Object>>>> obtenerPresentesEnTurno() {
        try {
            ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
            LocalDate hoy = ahoraEnPeru.toLocalDate();
            List<RegistroAsistencia> registros = registroAsistenciaRepository.obtenerHistorialHoy(hoy);
            
            // Filtrar solo los que tienen entrada pero no salida (presentes en turno)
            Map<String, List<Map<String, Object>>> agrupado = registros.stream()
                .filter(r -> r.getHoraEntrada() != null && r.getHoraSalida() == null)
                .collect(Collectors.groupingBy(
                    r -> r.getIntegrante().getFuncion() != null ? r.getIntegrante().getFuncion() : "Sin Función",
                    Collectors.mapping(r -> {
                        Map<String, Object> mapa = new HashMap<>();
                        mapa.put("id", r.getIntegrante().getId());
                        mapa.put("nombre", r.getIntegrante().getNombreCompleto());
                        mapa.put("horaEntrada", r.getHoraEntrada().toString());
                        return mapa;
                    }, Collectors.toList())
                ));
            
            return ResponseEntity.ok(agrupado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Registrar entrada para un integrante específico
    @PostMapping("/entrada-participante")
    public String registrarEntradaParticipante(Long integranteId,
                                               RedirectAttributes redirectAttributes) {
        try {
            if (integranteId == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Integrante no especificado");
                return "redirect:/admin/control-personal";
            }
            
            Integrante integrante = integranteRepository.findById(integranteId)
                .orElseThrow(() -> new IllegalArgumentException("Integrante no encontrado"));
            
            asistenciaService.registrarEntrada(integranteId);
            redirectAttributes.addFlashAttribute("success", "✓ Entrada registrada para " + integrante.getNombreCompleto());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error interno: " + e.getMessage());
        }
        return "redirect:/admin/control-personal";
    }

    // Registrar salida para un integrante específico
    @PostMapping("/salida-participante")
    public String registrarSalidaParticipante(Long integranteId,
                                              RedirectAttributes redirectAttributes) {
        try {
            if (integranteId == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Integrante no especificado");
                return "redirect:/admin/control-personal";
            }
            
            Integrante integrante = integranteRepository.findById(integranteId)
                .orElseThrow(() -> new IllegalArgumentException("Integrante no encontrado"));
            
            asistenciaService.registrarSalida(integranteId);
            redirectAttributes.addFlashAttribute("success", "✓ Salida registrada para " + integrante.getNombreCompleto());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error interno: " + e.getMessage());
        }
        return "redirect:/admin/control-personal";
    }

    @PostMapping("/entrada")
    public String registrarEntrada(HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            Usuario usuarioEnSesion = (Usuario) session.getAttribute("usuario");
            
            if (usuarioEnSesion == null) {
                redirectAttributes.addFlashAttribute("error", "No hay usuario en sesión");
                return "redirect:/admin/control-personal";
            }
            
            Integrante integrante = obtenerOCrearIntegrante(usuarioEnSesion);
            asistenciaService.registrarEntrada(integrante.getId());
            redirectAttributes.addFlashAttribute("success", "✓ Entrada registrada para " + integrante.getNombreCompleto());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error interno: " + e.getMessage());
        }
        return "redirect:/admin/control-personal";
    }

    @PostMapping("/salida")
    public String registrarSalida(HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            Usuario usuarioEnSesion = (Usuario) session.getAttribute("usuario");
            
            if (usuarioEnSesion == null) {
                redirectAttributes.addFlashAttribute("error", "No hay usuario en sesión");
                return "redirect:/admin/control-personal";
            }
            
            Integrante integrante = obtenerOCrearIntegrante(usuarioEnSesion);
            asistenciaService.registrarSalida(integrante.getId());
            redirectAttributes.addFlashAttribute("success", "✓ Salida registrada para " + integrante.getNombreCompleto());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error interno: " + e.getMessage());
        }
        return "redirect:/admin/control-personal";
    }

    /**
     * Busca un Integrante por email del usuario logueado.
     * Si no existe, lo crea automáticamente con los datos del usuario.
     */
    private Integrante obtenerOCrearIntegrante(Usuario usuario) {
        Optional<Integrante> integranteOpt = integranteRepository.findByEmail(usuario.getEmail());
        
        if (integranteOpt.isPresent()) {
            return integranteOpt.get();
        }
        
        // Crear integrante automáticamente con datos del usuario
        Integrante nuevo = new Integrante();
        nuevo.setNombreCompleto(usuario.getNombreCompleto());
        nuevo.setEmail(usuario.getEmail());
        nuevo.setFuncion(usuario.getRol() != null ? usuario.getRol().name() : "EMPLEADO");
        nuevo.setFechaIngreso(java.time.LocalDate.now());
        nuevo.setActivo(true);
        
        return integranteRepository.save(nuevo);
    }
}

