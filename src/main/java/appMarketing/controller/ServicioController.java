package appMarketing.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import appMarketing.Service.ServicioService;
import appMarketing.entity.Equipo;
import appMarketing.entity.Servicio;
import appMarketing.Service.EquipoService;

@Controller
@RequestMapping("/admin/servicios")
public class ServicioController {

	@Autowired
	private EquipoService equipoService;
	
    @Autowired
    private ServicioService servicioService;

    @GetMapping
    public String listarServicios(Model model) {
        try {
            model.addAttribute("active", "servicios"); 
            model.addAttribute("servicios", servicioService.listarTodos() != null ? servicioService.listarTodos() : java.util.Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar servicios: " + e.getMessage());
            model.addAttribute("servicios", java.util.Collections.emptyList());
        }
        return "admin/servicios/lista"; 
    }

    @GetMapping("/nuevo")
    public String nuevoServicioForm(Model model) {
        try {
            Servicio servicio = new Servicio();
            model.addAttribute("servicio", servicio);
            
            List<Equipo> equipos = equipoService.listarTodos();
            model.addAttribute("equipos", equipos != null ? equipos : java.util.Collections.emptyList());
            
            System.out.println("DEBUG - Formulario nuevo servicio cargado exitosamente");
            System.out.println("DEBUG - Cantidad de equipos: " + (equipos != null ? equipos.size() : 0));
        } catch (Exception e) {
            System.err.println("ERROR en nuevoServicioForm: " + e.getMessage());
            e.printStackTrace();
            
            // Asegurar que siempre haya atributos necesarios
            if (!model.containsAttribute("servicio")) {
                model.addAttribute("servicio", new Servicio());
            }
            if (!model.containsAttribute("equipos")) {
                model.addAttribute("equipos", java.util.Collections.emptyList());
            }
            model.addAttribute("error", "Error al cargar formulario: " + e.getMessage());
        }
        return "admin/servicios/form";
    }
    
    @PostMapping("/guardar")
    public String guardarServicio(@ModelAttribute Servicio servicio) {

        if (servicio.getEquipoAsignado() != null &&
            servicio.getEquipoAsignado().getId() != null) {

            Equipo equipo = equipoService
                    .findById(servicio.getEquipoAsignado().getId())
                    .orElse(null);

            servicio.setEquipoAsignado(equipo);
        }

        servicioService.guardar(servicio);

        return "redirect:/admin/servicios";
    }

    @PostMapping("/{id}/estado")
    @ResponseBody
    public BigDecimal cambiarEstado(@PathVariable Long id,
                                   @RequestParam Servicio.EstadoServicio estado) {
        servicioService.actualizarEstado(id, estado);
        return servicioService.calcularPrecioAutomatico(id);
    }

    @GetMapping("/{id}/precio")
    @ResponseBody
    public BigDecimal obtenerPrecio(@PathVariable Long id) {
        return servicioService.calcularPrecioAutomatico(id);
    }
    
    @ModelAttribute("equipos")
    public List<Equipo> cargarEquipos() {
        try {
            List<Equipo> equipos = equipoService.listarTodos();
            return equipos != null ? equipos : java.util.Collections.emptyList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
    @GetMapping("/eliminar/{id}")
    public String eliminarServicio(@PathVariable Long id) {
        servicioService.eliminar(id); // Asegúrate que este método exista en tu Service
        return "redirect:/admin/servicios";
    }

    @GetMapping("/editar/{id}")
    public String editarServicioForm(@PathVariable Long id, Model model) {
        Servicio servicio = servicioService.findById(id).orElseThrow();
        model.addAttribute("servicio", servicio);
        model.addAttribute("equipos", equipoService.listarTodos());
        return "admin/servicios/form";
    }
}