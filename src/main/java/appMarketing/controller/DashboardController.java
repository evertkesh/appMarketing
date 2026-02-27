package appMarketing.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import appMarketing.Service.EquipoService;
import appMarketing.Service.RegistroAsistenciaService;
import appMarketing.Service.ReporteDIarioService;
import appMarketing.Service.ServicioService;
import appMarketing.entity.Integrante;
import appMarketing.entity.RegistroAsistencia;
import appMarketing.entity.Servicio;
import appMarketing.entity.Usuario;
import appMarketing.repository.IntegranteRepository;
import appMarketing.repository.RegistroAsistenciaRepository;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    private static final ZoneId PERU_ZONE = ZoneId.of("America/Lima");

    @Autowired 
    private EquipoService equipoService;
    
    @Autowired 
    private ServicioService servicioService;
    
    @Autowired 
    private RegistroAsistenciaService asistenciaService;
    
    @Autowired 
    private ReporteDIarioService reporteService;

    @Autowired
    private IntegranteRepository integranteRepository;

    @Autowired
    private RegistroAsistenciaRepository registroAsistenciaRepository;

 
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            model.addAttribute("active", "dashboard"); 
            model.addAttribute("equipos", equipoService.listarTodos() != null ? equipoService.listarTodos() : java.util.Collections.emptyList());
            model.addAttribute("servicios", servicioService.listarTodos() != null ? servicioService.listarTodos() : java.util.Collections.emptyList());
            model.addAttribute("asistenciasHoy", asistenciaService.listarPorFecha(LocalDate.now()) != null ? asistenciaService.listarPorFecha(LocalDate.now()) : java.util.Collections.emptyList());
            model.addAttribute("totalGanancias",servicioService.calcularGananciasTotales());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
        }
        return "admin/menu-principal";
    }
    
    @GetMapping("/menu-principal")
    public String menuPrincipal(Model model) {
        try {
            model.addAttribute("active", "dashboard"); 
            model.addAttribute("equipos", equipoService.listarTodos() != null ? equipoService.listarTodos() : java.util.Collections.emptyList());
            model.addAttribute("servicios", servicioService.listarTodos() != null ? servicioService.listarTodos() : java.util.Collections.emptyList());
            model.addAttribute("asistenciasHoy", asistenciaService.listarPorFecha(LocalDate.now()) != null ? asistenciaService.listarPorFecha(LocalDate.now()) : java.util.Collections.emptyList());
            model.addAttribute("totalGanancias", servicioService.calcularGananciasTotales());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el menú principal: " + e.getMessage());
        }
        return "admin/menu-principal";
    }

    @GetMapping("/seguimiento")
    public String seguimiento(Model model) {
        try {
            model.addAttribute("active", "seguimiento");
            model.addAttribute("asistencias", asistenciaService.listarPorFecha(LocalDate.now()) != null ? asistenciaService.listarPorFecha(LocalDate.now()) : java.util.Collections.emptyList());
            model.addAttribute("servicios", servicioService.listarTodos() != null ? servicioService.listarTodos() : java.util.Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el seguimiento: " + e.getMessage());
        }
        return "admin/seguimiento";
    }
    

    @GetMapping("/control-personal")
    public String controlPersonal(Model model, HttpSession session,
                                 @org.springframework.web.bind.annotation.RequestParam(required = false) String fecha) {
        try {
            // Usar fecha de Perú o fecha filtrada
            ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
            LocalDate fechaFiltro;
            
            if (fecha != null && !fecha.isEmpty()) {
                try {
                    fechaFiltro = LocalDate.parse(fecha);
                } catch (Exception e) {
                    fechaFiltro = ahoraEnPeru.toLocalDate();
                }
            } else {
                fechaFiltro = ahoraEnPeru.toLocalDate();
            }
            
            LocalDate hoy = ahoraEnPeru.toLocalDate();
            
            model.addAttribute("active", "control-personal");
            model.addAttribute("fechaSeleccionada", fechaFiltro.toString());
            model.addAttribute("historialHoy", asistenciaService.obtenerHistorialPorFecha(fechaFiltro));
            model.addAttribute("personalPresente", asistenciaService.contarPersonalPresentePorFecha(fechaFiltro));
            model.addAttribute("totalRegistrosHoy", asistenciaService.contarTotalRegistrosPorFecha(fechaFiltro));
            model.addAttribute("asistenciasHoy", asistenciaService.listarPorFecha(fechaFiltro));
            
           
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario != null) {
                Optional<Integrante> integranteOpt = integranteRepository.findByEmail(usuario.getEmail());
                if (integranteOpt.isPresent()) {
                    Optional<RegistroAsistencia> registroHoy = registroAsistenciaRepository
                        .findRegistroPorIntegranteYFecha(integranteOpt.get().getId(), hoy);
                    if (registroHoy.isPresent() && registroHoy.get().getHoraEntrada() != null 
                            && registroHoy.get().getHoraSalida() == null) {
                        model.addAttribute("asistenciaActiva", registroHoy.get());
                    }
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar control de personal: " + e.getMessage());
            model.addAttribute("historialHoy", java.util.Collections.emptyList());
            model.addAttribute("personalPresente", 0L);
            model.addAttribute("totalRegistrosHoy", 0L);
        }
        return "admin/control-personal";
    }
    
    @PostMapping("/servicios/limpiar-todo")
    public String limpiarServiciosTodo() {
        try {
            servicioService.eliminarTodos();
        } catch (Exception e) {
            System.out.println("Error al limpiar: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
    
    @GetMapping("/ganancias-hoy")
    @ResponseBody
    public java.util.Map<String, Object> gananciasHoy() {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        try {
            java.math.BigDecimal total = servicioService.calcularGananciasTotales();
            long completados = servicioService.listarTodos().stream()
                    .filter(s -> s.getEstado() == Servicio.EstadoServicio.COMPLETADO)
                    .count();
            resp.put("total", total);
            resp.put("serviciosCompletados", completados);
        } catch (Exception e) {
            resp.put("error", e.getMessage());
        }
        return resp;
    }
}
