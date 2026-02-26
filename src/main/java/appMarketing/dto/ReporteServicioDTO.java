package appMarketing.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * DTO para mapear datos del reporte de servicios diarios
 * Coincide con la estructura esperada por el archivo JRXML
 */
public class ReporteServicioDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Date fecha;
    private String servicio;
    private String descripcion_trabajo;
    private Integer horas_trabajadas;
    private BigDecimal ganancia_dia;
    private BigDecimal total_acumulado_servicio;
    
    // Constructor vacío
    public ReporteServicioDTO() {
    }
    
    // Constructor completo
    public ReporteServicioDTO(Date fecha, String servicio, String descripcion_trabajo,
                              Integer horas_trabajadas, BigDecimal ganancia_dia,
                              BigDecimal total_acumulado_servicio) {
        this.fecha = fecha;
        this.servicio = servicio;
        this.descripcion_trabajo = descripcion_trabajo;
        this.horas_trabajadas = horas_trabajadas;
        this.ganancia_dia = ganancia_dia;
        this.total_acumulado_servicio = total_acumulado_servicio;
    }
    
    // Getters y Setters
    public Date getFecha() {
        return fecha;
    }
    
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    
    public String getServicio() {
        return servicio;
    }
    
    public void setServicio(String servicio) {
        this.servicio = servicio;
    }
    
    public String getDescripcion_trabajo() {
        return descripcion_trabajo;
    }
    
    public void setDescripcion_trabajo(String descripcion_trabajo) {
        this.descripcion_trabajo = descripcion_trabajo;
    }
    
    public Integer getHoras_trabajadas() {
        return horas_trabajadas;
    }
    
    public void setHoras_trabajadas(Integer horas_trabajadas) {
        this.horas_trabajadas = horas_trabajadas;
    }
    
    public BigDecimal getGanancia_dia() {
        return ganancia_dia;
    }
    
    public void setGanancia_dia(BigDecimal ganancia_dia) {
        this.ganancia_dia = ganancia_dia;
    }
    
    public BigDecimal getTotal_acumulado_servicio() {
        return total_acumulado_servicio;
    }
    
    public void setTotal_acumulado_servicio(BigDecimal total_acumulado_servicio) {
        this.total_acumulado_servicio = total_acumulado_servicio;
    }
}
