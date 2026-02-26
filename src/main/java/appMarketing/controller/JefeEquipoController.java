package appMarketing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import appMarketing.Service.JefeEquipoService;
import appMarketing.Service.UsuarioService;
import appMarketing.entity.JefeEquipo;
import appMarketing.entity.Usuario;

@Controller
@RequestMapping("/admin/jefes")
public class JefeEquipoController {
    
    @Autowired
    private JefeEquipoService jefeEquipoService;
    
    @Autowired
    private UsuarioService usuarioService;
    

    @GetMapping
    public String listarJefes(Model model) {
        try {
            model.addAttribute("active", "jefes");
            model.addAttribute("jefes", jefeEquipoService.listarTodos() != null ? jefeEquipoService.listarTodos() : java.util.Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar jefes: " + e.getMessage());
            model.addAttribute("jefes", java.util.Collections.emptyList());
        }
        return "admin/jefes/lista";
    }
    
  
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("active", "jefes");
        return "admin/jefes/formulario";
    }
    
  
    @PostMapping("/guardar")
    public String guardarJefe(@RequestParam String nombreCompleto,
                             @RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String telefono,
                             @RequestParam String especialidad,
                             @RequestParam(defaultValue = "temporal123") String password,
                             RedirectAttributes redirectAttributes) {
        
        try {
            
            if (usuarioService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe");
                return "redirect:/admin/jefes/nuevo";
            }
            
            
            if (usuarioService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
                return "redirect:/admin/jefes/nuevo";
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
            
            redirectAttributes.addFlashAttribute("success", "Jefe creado exitosamente. Contraseña temporal: " + password);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear jefe: " + e.getMessage());
        }
        
        return "redirect:/admin/jefes";
    }
  
    @GetMapping("/{id}")
    public String verJefe(@PathVariable Long id, Model model) {
        model.addAttribute("active", "jefes");
        JefeEquipo jefe = jefeEquipoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado"));
        model.addAttribute("jefe", jefe);
        return "admin/jefes/detalle";
    }
    
   
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("active", "jefes");
        JefeEquipo jefe = jefeEquipoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado"));
        model.addAttribute("jefe", jefe);
        return "admin/jefes/editar";
    }
    
  
    @PostMapping("/{id}/editar")
    public String editarJefe(@PathVariable Long id,
                            @RequestParam String nombreCompleto,
                            @RequestParam String username,
                            @RequestParam String email,
                            @RequestParam String telefono,
                            @RequestParam String especialidad,
                            RedirectAttributes redirectAttributes) {
        
        try {
            JefeEquipo jefe = jefeEquipoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Jefe no encontrado"));

           
            Usuario usuario = jefe.getUsuario();
            usuario.setNombreCompleto(nombreCompleto);
            usuario.setUsername(username);
            usuario.setEmail(email);
            usuarioService.guardar(usuario);

           
            jefe.setTelefono(telefono);
            jefe.setEspecialidad(especialidad);
            jefeEquipoService.guardar(jefe);
            
            redirectAttributes.addFlashAttribute("success", "Jefe actualizado exitosamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }

        return "redirect:/admin/jefes/" + id;
    }
}