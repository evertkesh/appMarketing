package appMarketing.ServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import appMarketing.Service.ServicioService;
import appMarketing.entity.Servicio;
import appMarketing.repository.ServicioRepository;

@Service
public class ServicioServiceImpl implements ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Override
    public List<Servicio> listarTodos() {
        return servicioRepository.findAll();
    }

    @Override
    public Optional<Servicio> findById(Long id) {
        return servicioRepository.findById(id);
    }

    @Override
    public Servicio guardar(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    @Override
    public void actualizarEstado(Long id, Servicio.EstadoServicio estado) {
        servicioRepository.findById(id).ifPresent(s -> {
            s.setEstado(estado);
            servicioRepository.save(s);
        });
    }

    @Override
    public BigDecimal calcularGananciasTotales() {
        return servicioRepository.sumarGananciasCompletadas();
    }

    @Override
    public void eliminar(Long id) {
        servicioRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void eliminarTodos() {
        servicioRepository.deleteAllInBatch();
    }
    
    @Override
    public BigDecimal calcularPrecioAutomatico(Long servicioId) {
        return servicioRepository.findById(servicioId)
                .map(Servicio::getPrecioBase)
                .orElse(BigDecimal.ZERO);
    }
}