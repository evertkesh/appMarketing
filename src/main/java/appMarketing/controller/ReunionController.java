package appMarketing.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import appMarketing.Service.EquipoService;
import appMarketing.Service.ReunionService;
import appMarketing.Service.UsuarioService;
import appMarketing.dto.ReunionDTO;
import appMarketing.entity.Equipo;
import appMarketing.entity.Reunion;
import appMarketing.entity.Usuario;

@Controller
@RequestMapping("/admin/reuniones")
public class ReunionController {

    @Autowired
    private ReunionService reunionService;

    @Autowired
    private EquipoService equipoService;

    @Autowired(required = false)
    private UsuarioService usuarioService;

    @GetMapping
    public String listarReuniones(Model model) {
        try {
            model.addAttribute("reuniones", reunionService.listarTodas() != null ? reunionService.listarTodas() : java.util.Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar reuniones: " + e.getMessage());
            model.addAttribute("reuniones", java.util.Collections.emptyList());
        }
        return "admin/reuniones/lista";
    }

    @GetMapping("/nueva")
    public String nuevaReunionForm(Model model) {
        try {
            model.addAttribute("reunion", new Reunion());
            model.addAttribute("equipos", equipoService.listarTodos() != null ? equipoService.listarTodos() : java.util.Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar formulario: " + e.getMessage());
        }
        return "admin/reuniones/form";
    }

    @PostMapping("/organizar")
    public String organizarReunion(@ModelAttribute Reunion reunion,
                                  @RequestParam Long equipoId,
                                  @RequestParam Long organizadorId) {
        try {
            // Validar que los parámetros sean válidos
            if (equipoId == null || equipoId <= 0) {
                return "redirect:/admin/reuniones/nueva?error=equipo";
            }
            
            if (organizadorId == null || organizadorId <= 0) {
                return "redirect:/admin/reuniones/nueva?error=organizador";
            }
            
            if (reunion.getTitulo() == null || reunion.getTitulo().trim().isEmpty()) {
                return "redirect:/admin/reuniones/nueva?error=titulo";
            }
            
            if (reunion.getFechaHora() == null) {
                return "redirect:/admin/reuniones/nueva?error=fecha";
            }
            
            // Asignar equipo
            Optional<Equipo> equipoOpt = equipoService.findById(equipoId);
            if (equipoOpt.isPresent()) {
                reunion.setEquipo(equipoOpt.get());
            } else {
                return "redirect:/admin/reuniones/nueva?error=equipo_no_existe";
            }
            
            // Asignar organizador
            if (usuarioService != null) {
                Optional<Usuario> usuarioOpt = usuarioService.findById(organizadorId);
                if (usuarioOpt.isPresent()) {
                    reunion.setOrganizador(usuarioOpt.get());
                } else {
                    return "redirect:/admin/reuniones/nueva?error=usuario_no_existe";
                }
            }
            
            // Guardar la reunión (aquí se validará si existe duplicado)
            reunionService.organizarReunion(reunion);
            
            return "redirect:/admin/reuniones?success=creada";
            
        } catch (IllegalArgumentException e) {
            // Capturar la excepción de reunión duplicada
            return "redirect:/admin/reuniones/nueva?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/reuniones/nueva?error=" + java.net.URLEncoder.encode("Error al crear la reunión", java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * Endpoint POST para cancelar una reunión
     */
    @PostMapping("/{id}/cancelar")
    public String cancelarReunion(@org.springframework.web.bind.annotation.PathVariable Long id) {
        try {
            reunionService.cancelarReunion(id);
            return "redirect:/admin/reuniones?success=cancelada";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/reuniones?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/reuniones?error=" + java.net.URLEncoder.encode("Error al cancelar la reunión", java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * Endpoint REST que devuelve todas las reuniones en formato JSON
     * Compatible con FullCalendar
     */
    @GetMapping("/api/all")
    public ResponseEntity<List<ReunionDTO>> obtenerReunionesJSON() {
        try {
            List<ReunionDTO> reuniones = reunionService.listarTodasDTO();
            return ResponseEntity.ok(reuniones);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}

