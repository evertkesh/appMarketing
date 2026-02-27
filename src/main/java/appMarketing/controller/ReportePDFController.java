package appMarketing.controller;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import appMarketing.Service.ReporteDIarioService;
import appMarketing.Service.ServicioService;
import appMarketing.dto.ReportePDFDTO;
import appMarketing.entity.ReporteDiario;
import appMarketing.entity.Servicio;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@RestController
@RequestMapping("/api/reportes")
public class ReportePDFController {

    private static final ZoneId PERU_ZONE = ZoneId.of("America/Lima");

    @Autowired
    private ReporteDIarioService reporteDiarioService;
    
    @Autowired
    private ServicioService servicioService;

    @GetMapping("/ganancias-por-fecha")
    public ResponseEntity<byte[]> generarReportePorFecha(
            @RequestParam(required = false) String fecha) {

        try {
            // Usar fecha de Perú si no se especifica
            LocalDate fechaReporte;
            if (fecha != null && !fecha.isEmpty()) {
                fechaReporte = LocalDate.parse(fecha);
            } else {
                ZonedDateTime ahoraEnPeru = ZonedDateTime.now(PERU_ZONE);
                fechaReporte = ahoraEnPeru.toLocalDate();
            }

            System.out.println("=== GENERANDO REPORTE PARA FECHA: " + fechaReporte + " (Zona horaria: Peru) ===");
            
            // Primero intentar obtener reportes diarios de la fecha
            var reportesDiarios = reporteDiarioService.listarPorFecha(fechaReporte);
            System.out.println("Reportes diarios encontrados para " + fechaReporte + ": " + reportesDiarios.size());
            
            if (!reportesDiarios.isEmpty()) {
                // Usar reportes diarios si existen
                List<ReportePDFDTO> datosDTO = mapearDatosDesdeReportes(reportesDiarios);
                System.out.println("DTOs creados desde reportes diarios: " + datosDTO.size());
                
                byte[] pdf = generarPDF(datosDTO);
                System.out.println("PDF generado exitosamente. Tamaño: " + pdf.length + " bytes");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData(
                        "attachment",
                        "Reporte_Ganancias_" + fechaReporte + ".pdf"
                );

                return ResponseEntity.ok().headers(headers).body(pdf);
            }
            
            // Si no hay reportes diarios, buscar en servicios
            var servicios = servicioService.listarTodos();
            System.out.println("Servicios totales encontrados: " + servicios.size());
            
            // Filtrar servicios por fecha (sin filtrar por estado - mostrar todos)
            var serviciosFiltrados = servicios.stream()
                    .filter(s -> s.getFechaCreacion() != null 
                            && s.getFechaCreacion().toLocalDate().equals(fechaReporte))
                    .toList();
            
            System.out.println("Servicios encontrados para la fecha " + fechaReporte + ": " + serviciosFiltrados.size());

            if (serviciosFiltrados.isEmpty()) {
                System.out.println("Sin datos para la fecha: " + fechaReporte);
                // Generar PDF vacío
                List<ReportePDFDTO> datosDTO = new java.util.ArrayList<>();
                byte[] pdf = generarPDF(datosDTO);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData(
                        "attachment",
                        "Reporte_Ganancias_" + fechaReporte + ".pdf"
                );
                return ResponseEntity.ok().headers(headers).body(pdf);
            }

            // Mapear servicios a DTO
            List<ReportePDFDTO> datosDTO = mapearDatosDesdeServicios(serviciosFiltrados);
            System.out.println("DTOs creados desde servicios: " + datosDTO.size());
            
            byte[] pdf = generarPDF(datosDTO);
            System.out.println("PDF generado exitosamente. Tamaño: " + pdf.length + " bytes");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                    "attachment",
                    "Reporte_Ganancias_" + fechaReporte + ".pdf"
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);

        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/ganancias-pdf")
    public ResponseEntity<byte[]> generarReportePDF() {
        try {
            System.out.println("=== GENERANDO REPORTE TOTAL (TODOS LOS REGISTROS) ===");
            
            // Obtener TODOS los reportes diarios sin filtrar por fecha
            var todosLosReportes = reporteDiarioService.listarTodos();
            System.out.println("Reportes diarios totales en BD: " + todosLosReportes.size());
            
            // Obtener TODOS los servicios (sin filtrar por estado ni fecha)
            var todosLosServicios = servicioService.listarTodos();
            System.out.println("Servicios totales en BD: " + todosLosServicios.size());
            
            List<ReportePDFDTO> datosDTO = new java.util.ArrayList<>();
            
            // Agregar todos los reportes diarios
            if (!todosLosReportes.isEmpty()) {
                var dtosReportes = mapearDatosDesdeReportes(todosLosReportes);
                datosDTO.addAll(dtosReportes);
                System.out.println("DTOs agregados desde reportes diarios: " + dtosReportes.size());
            }
            
            // Agregar todos los servicios que no estén ya en reportes diarios
            if (!todosLosServicios.isEmpty()) {
                var dtosServicios = mapearDatosDesdeServicios(todosLosServicios);
                datosDTO.addAll(dtosServicios);
                System.out.println("DTOs agregados desde servicios: " + dtosServicios.size());
            }
            
            System.out.println("Total de DTOs para el PDF: " + datosDTO.size());
            
            if (datosDTO.isEmpty()) {
                System.out.println("⚠️ ADVERTENCIA: No hay datos para generar reporte total");
            }
            
            byte[] pdf = generarPDF(datosDTO);
            System.out.println("✅ PDF generado exitosamente. Tamaño: " + pdf.length + " bytes");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Reporte_Total_Ganancias.pdf");

            return ResponseEntity.ok().headers(headers).body(pdf);

        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte total: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private List<ReportePDFDTO> mapearDatos(List<ReporteDiario> reportes) {
        return reportes.stream().map(r -> {
            ReportePDFDTO dto = new ReportePDFDTO();
            dto.setFecha(r.getFecha());
            dto.setDescripcion_trabajo(r.getDescripcionTrabajo());
            dto.setHoras_trabajadas(r.getHorasTrabajadas());
            dto.setGanancia_dia(r.getGananciaDia());
            dto.setTotal_ganancias(r.getTotalGanancias());
            dto.setServicio_nombre(
                    r.getServicio() != null
                            ? r.getServicio().getNombre()
                            : "Sin Servicio"
            );
            return dto;
        }).toList();
    }

    private List<ReportePDFDTO> mapearDatosDesdeReportes(List<ReporteDiario> reportes) {
        return mapearDatos(reportes);
    }

    private List<ReportePDFDTO> mapearDatosDesdeServicios(List<Servicio> servicios) {
        return servicios.stream().map(s -> {
            ReportePDFDTO dto = new ReportePDFDTO();
            dto.setFecha(s.getFechaCreacion() != null ? s.getFechaCreacion().toLocalDate() : LocalDate.now());
            dto.setDescripcion_trabajo(s.getDescripcion() != null ? s.getDescripcion() : "");
            dto.setHoras_trabajadas(0); // No hay horas en servicios
            dto.setGanancia_dia(s.getPrecioBase() != null ? s.getPrecioBase() : java.math.BigDecimal.ZERO);
            dto.setTotal_ganancias(s.getPrecioBase() != null ? s.getPrecioBase() : java.math.BigDecimal.ZERO);
            dto.setServicio_nombre(s.getNombre());
            return dto;
        }).toList();
    }

    private byte[] generarPDF(List<ReportePDFDTO> datos) throws Exception {
        InputStream inputStream = null;
        try {
            // Intentar con el reporte simplificado primero
            inputStream = this.getClass().getResourceAsStream("/Reportes/Reporte_Basico.jrxml");
            
            if (inputStream == null) {
                System.out.println("Reporte_Basico.jrxml no encontrado, intentando Reporte_Simple.jrxml");
                inputStream = this.getClass().getResourceAsStream("/Reportes/Reporte_Simple.jrxml");
            }
            
            if (inputStream == null) {
                throw new Exception("No se encontró ningún archivo de reporte");
            }

            System.out.println("✓ Archivo JRXML cargado correctamente");

            // Compilar el reporte
            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
            System.out.println("✓ Reporte compilado");

            // Crear la fuente de datos desde la colección de DTOs
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datos, false);
            System.out.println("✓ DataSource creado con " + datos.size() + " registros");

            // Llenar el reporte con los datos
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    new HashMap<>(),
                    dataSource
            );
            System.out.println("✓ Reporte llenado");

            // Exportar a PDF
            byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);
            System.out.println("✓ PDF exportado: " + pdf.length + " bytes");
            
            return pdf;
            
        } catch (Exception e) {
            System.err.println("❌ Error en generarPDF: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    System.err.println("Error al cerrar InputStream: " + e.getMessage());
                }
            }
        }
    }
}