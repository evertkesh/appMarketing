package appMarketing.entity;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "equipos")
@Data
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // Equipo 1, Equipo 2, etc.

    @OneToOne
    @JoinColumn(name = "jefe_equipo_id")
    private JefeEquipo jefeEquipo;

    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL)
    private List<Integrante> integrantes = new ArrayList<>();

    private String descripcion;

    private boolean activo = true;
}