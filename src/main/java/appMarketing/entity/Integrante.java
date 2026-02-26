package appMarketing.entity;
import lombok.Data;
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
@Table(name = "integrantes")
@Data
public class Integrante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreCompleto;

    private String email;

    private String telefono;

    @Column(nullable = false)
    private String funcion; // Diseñador, Redactor, Community Manager, etc.

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    private LocalDate fechaIngreso;

    private LocalDate fechaSalida; // null si sigue activo

    private boolean activo = true;

    private String motivoSalida; // Renuncia, Despido, etc.
}