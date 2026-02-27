package appMarketing.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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

    private static final ZoneId PERU_ZONE = ZoneId.of("America/Lima");

    @Autowired
    private ReporteDIarioService reporteService;

    @Autowired
    private ServicioService servicioService;

    @GetMapping("/diario")
    public String reporteDiario(@RequestParam(required = false) LocalDate fecha,
                               Model model) {
        try {
            // Usar fecha de Perú si no se especifica una fecha
            LocalDate fechaFinal;
            if (fecha != null) {
                fechaFinal = fecha;
            } else {
                ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
                fechaFinal = ahoraEnPeru.toLocalDate();
            }

            model.addAttribute("active", "reportes"); 
            model.addAttribute("reporte", new ReporteDiario());
            
            // Obtener reportes diarios de la fecha
            var reportesDiarios = reporteService.listarPorFecha(fechaFinal);
            System.out.println("📊 Reportes diarios encontrados para " + fechaFinal + ": " + reportesDiarios.size());
            
            // Obtener servicios de la fecha
            var servicios = servicioService.listarTodos();
            var serviciosFecha = servicios.stream()
                .filter(s -> s.getFechaCreacion() != null 
                        && s.getFechaCreacion().toLocalDate().equals(fechaFinal))
                .toList();
            
            System.out.println("📋 Servicios encontrados para " + fechaFinal + ": " + serviciosFecha.size());
            
            // Crear lista combinada de reportes
            java.util.List<ReporteDiario> reportesCombinados = new java.util.ArrayList<>();
            
            // Agregar reportes diarios si existen
            if (reportesDiarios != null && !reportesDiarios.isEmpty()) {
                reportesCombinados.addAll(reportesDiarios);
            }
            
            // Agregar servicios como reportes si no están en reportes diarios
            if (!serviciosFecha.isEmpty()) {
                for (var servicio : serviciosFecha) {
                    // Verificar si este servicio ya tiene un reporte diario
                    boolean yaExiste = reportesDiarios.stream()
                        .anyMatch(r -> r.getServicio() != null && r.getServicio().getId().equals(servicio.getId()));
                    
                    if (!yaExiste) {
                        ReporteDiario r = new ReporteDiario();
                        r.setFecha(fechaFinal);
                        r.setServicio(servicio);
                        r.setDescripcionTrabajo(servicio.getDescripcion());
                        r.setHorasTrabajadas(0);
                        r.setGananciaDia(servicio.getPrecioBase());
                        r.setTotalGanancias(servicio.getPrecioBase());
                        reportesCombinados.add(r);
                    }
                }
            }
            
            System.out.println("✅ Total de reportes combinados: " + reportesCombinados.size());
            
            model.addAttribute("reportes", reportesCombinados);
            
            // Calcular total de ganancias
            var totalGanancias = reportesCombinados.stream()
                .map(r -> r.getGananciaDia() != null ? r.getGananciaDia() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            model.addAttribute("totalGanancias", totalGanancias);
            System.out.println("💰 Total de ganancias: " + totalGanancias);
            
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