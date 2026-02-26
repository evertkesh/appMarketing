package appMarketing.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import appMarketing.entity.ReporteDiario;

public interface ReporteDIarioService {
    ReporteDiario crearReporte(ReporteDiario reporte);
    List<ReporteDiario> listarPorFecha(LocalDate fecha);
    BigDecimal calcularTotalGanancias(LocalDate fecha);
    List<ReporteDiario> listarTodos();
    List<ReporteDiario> obtenerDatosParaReporte(LocalDate fecha);
}