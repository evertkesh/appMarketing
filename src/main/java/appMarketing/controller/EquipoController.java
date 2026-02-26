package appMarketing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import appMarketing.Service.EquipoService;
import appMarketing.Service.IntegranteService;
import appMarketing.Service.JefeEquipoService;
import appMarketing.Service.UsuarioService;
import appMarketing.entity.Equipo;
import appMarketing.entity.Integrante;
import appMarketing.entity.JefeEquipo;
import appMarketing.entity.Usuario;

import java.util.List;

@Controller
@RequestMapping("/admin/equipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private IntegranteService integranteService;

    @Autowired
    private JefeEquipoService jefeEquipoService;
    
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listarEquipos(Model model) {
        try {
            model.addAttribute("active", "equipos"); 
            model.addAttribute("equipos", equipoService.listarTodos() != null ? equipoService.listarTodos() : java.util.Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar equipos: " + e.getMessage());
            model.addAttribute("equipos", java.util.Collections.emptyList());
        }
        return "admin/equipos/lista"; 
    }

    @GetMapping("/{id}")
    public String verEquipo(@PathVariable Long id, Model model) {
        model.addAttribute("active", "equipos");
        Equipo equipo = equipoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        
        model.addAttribute("equipo", equipo);
        model.addAttribute("integrantes", integranteService.listarPorEquipo(id));
        model.addAttribute("nuevoIntegrante", new Integrante()); 
        return "admin/equipos/detalle";
    }

    @PostMapping("/integrantes/agregar")
    public String agregarIntegrante(@ModelAttribute Integrante integrante,
                                   @RequestParam Long equipoId) {
        Equipo equipo = equipoService.findById(equipoId).orElse(null);
        if (equipo != null) {
            integrante.setEquipo(equipo);
            integranteService.guardar(integrante);
        }
        return "redirect:/admin/equipos/" + equipoId;
    }

    @PostMapping("/integrantes/eliminar")
    public String eliminarIntegrante(@RequestParam Long id,
                                     @RequestParam String motivo,
                                     @RequestParam Long equipoId) {
        integranteService.eliminar(id, motivo);
        return "redirect:/admin/equipos/" + equipoId;
    }
    
    @GetMapping("/jefe/{id}")
    public String verJefe(@PathVariable Long id, Model model) {
        model.addAttribute("active", "equipos");
        
        JefeEquipo jefe = jefeEquipoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado"));
        
        List<Equipo> equipos = equipoService.findByJefeEquipoId(id);
        Equipo equipo = equipos.isEmpty() ? null : equipos.get(0);
        
        model.addAttribute("jefe", jefe);
        model.addAttribute("equipoId", equipo != null ? equipo.getId() : null);
        model.addAttribute("equiposDelJefe", equipos);
        
        return "admin/equipos/jefe-detalle";
    }

    
    @GetMapping("/{id}/asignar-jefe")
    public String mostrarAsignarJefe(@PathVariable Long id, Model model) {
        model.addAttribute("active", "equipos");

        Equipo equipo = equipoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        model.addAttribute("equipo", equipo);
        model.addAttribute("jefes", jefeEquipoService.listarTodos());

        return "admin/equipos/asignar-jefe";
    }

    @PostMapping("/{id}/asignar-jefe")
    public String asignarJefe(@PathVariable Long id,
                              @RequestParam Long jefeId) {

        Equipo equipo = equipoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        JefeEquipo jefe = jefeEquipoService.findById(jefeId)
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado"));

        equipo.setJefeEquipo(jefe);
        equipoService.guardar(equipo);

        return "redirect:/admin/equipos/" + id;
    }

    
    @PostMapping("/jefe/{id}/correo")
    public String enviarCorreoJefe(@PathVariable Long id,
                                  @RequestParam String asunto,
                                  @RequestParam String mensaje) {
        jefeEquipoService.enviarCorreo(id, asunto, mensaje);
        return "redirect:/admin/equipos/jefe/" + id + "?enviado=true";
    }

    @PostMapping("/jefe/{id}/aviso")
    public String enviarAvisoJefe(@PathVariable Long id,
                                 @RequestParam String aviso) {
        jefeEquipoService.enviarAviso(id, aviso);
        return "redirect:/admin/equipos/jefe/" + id + "?avisoOk=true";
    }

    @PostMapping("/guardar")
    public String guardarEquipo(@ModelAttribute Equipo equipo) {
        equipoService.guardar(equipo);
        return "redirect:/admin/equipos";
    }
    
    @PostMapping("/{id}/crear-jefe")
    public String crearJefeYAsignar(@PathVariable Long id,
                                    @RequestParam String nombreCompleto,
                                    @RequestParam String username,
                                    @RequestParam String email,
                                    @RequestParam String telefono,
                                    @RequestParam String especialidad,
                                    @RequestParam String password,
                                    RedirectAttributes redirectAttributes) {
        
        try {
      
            if (usuarioService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe");
                return "redirect:/admin/equipos/" + id + "/asignar-jefe";
            }
            
          
            if (usuarioService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
                return "redirect:/admin/equipos/" + id + "/asignar-jefe";
            }
            
            
            Usuario usuario = new Usuario();
            usuario.setNombreCompleto(nombreCompleto);
            usuario.setUsername(username);
            usuario.setEmail(email);
            usuario.setPassword(password);
            usuario.setRol(Usuario.Rol.JEFE_EQUIPO);
            usuario.setActivo(true);
            usuarioService.guardar(usuario);
            
      
            JefeEquipo jefe = new JefeEquipo();
            jefe.setUsuario(usuario);
            jefe.setTelefono(telefono);
            jefe.setEspecialidad(especialidad);
            jefe.setActivo(true);
            jefeEquipoService.guardar(jefe);
            
        
            Equipo equipo = equipoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
            equipo.setJefeEquipo(jefe);
            equipoService.guardar(equipo);
            
            redirectAttributes.addFlashAttribute("success", 
                "Jefe creado y asignado exitosamente. Contraseña: " + password);
            
            return "redirect:/admin/equipos/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/admin/equipos/" + id + "/asignar-jefe";
        }
    }
        
        @PostMapping("/editar")
        public String editarEquipo(@RequestParam Long id,
                                  @RequestParam String nombre,
                                  @RequestParam String descripcion,
                                  RedirectAttributes redirectAttributes) {
            try {
                Equipo equipo = equipoService.findById(id)
                        .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
                
                equipo.setNombre(nombre);
                equipo.setDescripcion(descripcion);
                equipoService.guardar(equipo);
                
                redirectAttributes.addFlashAttribute("success", "Equipo actualizado exitosamente");
                
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            }
            
            return "redirect:/admin/equipos";
        }

        
        @PostMapping("/eliminar")
        public String eliminarEquipo(@RequestParam Long id,
                                    RedirectAttributes redirectAttributes) {
            try {
                Equipo equipo = equipoService.findById(id)
                        .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
                
                if (equipo.getIntegrantes() != null && !equipo.getIntegrantes().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "No se puede eliminar el equipo porque tiene integrantes. Elimine los integrantes primero.");
                    return "redirect:/admin/equipos";
                }
                
                equipoService.eliminar(id);
                redirectAttributes.addFlashAttribute("success", "Equipo eliminado exitosamente");
                
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
            }
            
            return "redirect:/admin/equipos";
        
    }
}