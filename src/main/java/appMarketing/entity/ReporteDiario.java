package appMarketing.entity;

import lombok.Data;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reportes_diarios")
@Data
public class ReporteDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    private String descripcionTrabajo;

    private Integer horasTrabajadas;

    @Column(precision = 10, scale = 2)
    private BigDecimal gananciaDia;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalGanancias; // Acumulado
}