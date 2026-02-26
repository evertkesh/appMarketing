package appMarketing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para mapear datos de ReporteDiario al formato esperado por Jasper Reports
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportePDFDTO {
    private LocalDate fecha;
    private String descripcion_trabajo;
    private Integer horas_trabajadas;
    private BigDecimal ganancia_dia;
    private BigDecimal total_ganancias;
    private String servicio_nombre;
}