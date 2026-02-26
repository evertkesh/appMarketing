package appMarketing.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO para representar una reunión en formato JSON
 * Compatible con calendarios como FullCalendar
 */
public class ReunionDTO {
    
    private Long id;
    private String title; // Para FullCalendar
    private String start; // Fecha inicio en formato ISO 8601
    private String end;   // Fecha fin en formato ISO 8601
    private String description;
    private String location;
    private String equipoNombre;
    private String organizadorNombre;
    private boolean notificarJefe;
    private String color; // Para distinguir reuniones
    
    // Constructor vacío
    public ReunionDTO() {
    }
    
    // Constructor completo
    public ReunionDTO(Long id, String titulo, LocalDateTime fechaHora, String descripcion,
                     String lugar, String equipoNombre, String organizadorNombre,
                     boolean notificarJefe) {
        this.id = id;
        this.title = titulo;
        // Convertir LocalDateTime a ISO 8601 para FullCalendar
        if (fechaHora != null) {
            this.start = fechaHora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            // Asumimos que la reunión dura 1 hora
            this.end = fechaHora.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        this.description = descripcion;
        this.location = lugar;
        this.equipoNombre = equipoNombre;
        this.organizadorNombre = organizadorNombre;
        this.notificarJefe = notificarJefe;
        this.color = "#3788d8"; // Color azul por defecto
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getStart() {
        return start;
    }
    
    public void setStart(String start) {
        this.start = start;
    }
    
    public String getEnd() {
        return end;
    }
    
    public void setEnd(String end) {
        this.end = end;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getEquipoNombre() {
        return equipoNombre;
    }
    
    public void setEquipoNombre(String equipoNombre) {
        this.equipoNombre = equipoNombre;
    }
    
    public String getOrganizadorNombre() {
        return organizadorNombre;
    }
    
    public void setOrganizadorNombre(String organizadorNombre) {
        this.organizadorNombre = organizadorNombre;
    }
    
    public boolean isNotificarJefe() {
        return notificarJefe;
    }
    
    public void setNotificarJefe(boolean notificarJefe) {
        this.notificarJefe = notificarJefe;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
}
