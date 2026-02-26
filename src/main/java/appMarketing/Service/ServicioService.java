package appMarketing.Service;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import appMarketing.entity.Servicio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import appMarketing.entity.Servicio;

public interface ServicioService {

    List<Servicio> listarTodos();

    Optional<Servicio> findById(Long id);

    Servicio guardar(Servicio servicio);

    void actualizarEstado(Long id, Servicio.EstadoServicio estado);

    BigDecimal calcularGananciasTotales();

    void eliminar(Long id);

    BigDecimal calcularPrecioAutomatico(Long servicioId);

    void eliminarTodos();
}