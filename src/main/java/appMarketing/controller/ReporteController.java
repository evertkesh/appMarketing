package appMarketing.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import appMarketing.Service.ReporteDIarioService;
import appMarketing.Service.ServicioService;
import appMarketing.entity.ReporteDiario;

/**
 * Controlador de reportes - Solo maneja vistas HTML
 * Los PDFs se generan en ReportePDFController.java
 */
@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {

    @Autowired
    private ReporteDIarioService reporteService;

    @Autowired
    private ServicioService servicioService;

    @GetMapping("/diario")
    public String reporteDiario(@RequestParam(required = false) LocalDate fecha,
                               Model model) {
        try {
            LocalDate fechaFinal = fecha != null ? fecha : LocalDate.now();

            model.addAttribute("active", "reportes"); 
            model.addAttribute("reporte", new ReporteDiario());
            
            // Primero, intentar cargar reportes diarios
            var reportesDiarios = reporteService.listarPorFecha(fechaFinal);
            model.addAttribute("reportes", reportesDiarios != null ? reportesDiarios : java.util.Collections.emptyList());
            model.addAttribute("totalGanancias", reporteService.calcularTotalGanancias(fechaFinal));
            
            // Si no hay reportes diarios, cargar servicios completados de la fecha
            if (reportesDiarios == null || reportesDiarios.isEmpty()) {
                var servicios = servicioService.listarTodos();
                var serviciosFecha = servicios.stream()
                    .filter(s -> s.getFechaCreacion() != null 
                            && s.getFechaCreacion().toLocalDate().equals(fechaFinal)
                            && s.getEstado() != null)
                    .toList();
                
                System.out.println("Servicios encontrados para fecha " + fechaFinal + ": " + serviciosFecha.size());
                
                // Convertir servicios a ReporteDiario para mostrar en la tabla
                var reportesDesdeServicios = serviciosFecha.stream().map(s -> {
                    ReporteDiario r = new ReporteDiario();
                    r.setFecha(fechaFinal);
                    r.setServicio(s);
                    r.setDescripcionTrabajo(s.getDescripcion());
                    r.setHorasTrabajadas(0);
                    r.setGananciaDia(s.getPrecioBase());
                    r.setTotalGanancias(s.getPrecioBase());
                    return r;
                }).toList();
                
                model.addAttribute("reportes", reportesDesdeServicios);
                
                // Calcular total de servicios
                var totalServicios = serviciosFecha.stream()
                    .map(s -> s.getPrecioBase() != null ? s.getPrecioBase() : java.math.BigDecimal.ZERO)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                
                model.addAttribute("totalGanancias", totalServicios);
                System.out.println("Total de ganancias: " + totalServicios);
            }
            
            model.addAttribute("fecha", fechaFinal);
            model.addAttribute("servicios", servicioService.listarTodos() != null ? servicioService.listarTodos() : java.util.Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error al cargar reportes: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar reportes: " + e.getMessage());
            model.addAttribute("reportes", java.util.Collections.emptyList());
        }
        
        return "admin/reportes/diario"; 
    }

    @PostMapping("/diario/crear")
    public String crearReporte(@RequestParam Long servicioId,
                              @RequestParam Integer horasTrabajadas,
                              @RequestParam java.math.BigDecimal gananciaDia,
                              @RequestParam(required = false) String descripcionTrabajo,
                              @RequestParam(required = false) LocalDate fecha,
                              Model model) {
        try {
            ReporteDiario reporte = new ReporteDiario();
            reporte.setFecha(fecha != null ? fecha : LocalDate.now());
            reporte.setHorasTrabajadas(horasTrabajadas);
            reporte.setGananciaDia(gananciaDia);
            reporte.setDescripcionTrabajo(descripcionTrabajo);
            reporte.setServicio(servicioService.findById(servicioId).orElse(null));
            
            reporteService.crearReporte(reporte);
            return "redirect:/admin/reportes/diario" + (fecha != null ? "?fecha=" + fecha : "");
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al agregar reporte: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/reportes/diario";
        }
    }
}