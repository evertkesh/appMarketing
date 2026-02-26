package appMarketing.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "servicios")
@Data
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // SEO, SEM, Redes Sociales, Diseño Web, etc.

    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Enumerated(EnumType.STRING)
    private EstadoServicio estado = EstadoServicio.PENDIENTE; // Default

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipoAsignado;
    
    public Servicio() {
        this.equipoAsignado = new Equipo(); // Inicializar para evitar NullPointerException
    }

    public enum EstadoServicio {
        PENDIENTE, EN_PROCESO, COMPLETADO, CANCELADO
        
        
    }
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
