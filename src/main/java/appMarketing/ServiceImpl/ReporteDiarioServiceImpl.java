package appMarketing.ServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import appMarketing.Service.ReporteDIarioService;
import appMarketing.entity.ReporteDiario;
import appMarketing.repository.ReporteDiarioRepository;

@Service
public class ReporteDiarioServiceImpl implements ReporteDIarioService {

    @Autowired
    private ReporteDiarioRepository reporteRepository;

    @Override
    public ReporteDiario crearReporte(ReporteDiario reporte) {
        if (reporte.getFecha() == null) {
            reporte.setFecha(LocalDate.now());
        }

        // Calcular total acumulado
        BigDecimal totalAnterior = reporteRepository.sumGananciasByFecha(reporte.getFecha());
        if (totalAnterior == null) {
			totalAnterior = BigDecimal.ZERO;
		}

        reporte.setTotalGanancias(totalAnterior.add(reporte.getGananciaDia()));

        return reporteRepository.save(reporte);
    }

    @Override
    public List<ReporteDiario> listarPorFecha(LocalDate fecha) {
        return reporteRepository.findByFecha(fecha);
    }

    @Override
    public BigDecimal calcularTotalGanancias(LocalDate fecha) {
        BigDecimal total = reporteRepository.sumGananciasByFecha(fecha);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public List<ReporteDiario> listarTodos() {
        return reporteRepository.findAll();
    }

    @Override
    public List<ReporteDiario> obtenerDatosParaReporte(LocalDate fecha) {
        // Obtiene los datos de la fecha especificada, incluyendo todas las relaciones necesarias
        return reporteRepository.findByFecha(fecha);
    }
}