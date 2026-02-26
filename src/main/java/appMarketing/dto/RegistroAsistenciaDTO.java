package appMarketing.dto;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAsistenciaDTO {
    private Long id;
    private String integranteName;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private String estado;
    private String estadoFormatted; // PRESENTE, AUSENTE, TARDANZA
    private String tiempoActivo; // Ej: "2h 35min" o "45 min"
}
