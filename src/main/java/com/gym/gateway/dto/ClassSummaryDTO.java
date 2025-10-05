package com.gym.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "DTO que representa el resumen de una clase para el miembro")
public class ClassSummaryDTO {
    
    @Schema(description = "ID único de la clase", example = "1")
    private Long id;
    
    @Schema(description = "Nombre de la clase", example = "Yoga Matutino")
    private String name;
    
    @Schema(description = "Fecha y hora programada de la clase", example = "2024-01-15T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime schedule;
    
    @Schema(description = "Capacidad máxima de la clase", example = "20")
    private Integer maxCapacity;
    
    @Schema(description = "ID del coach asignado", example = "1")
    private Long coachId;
    
    @Schema(description = "Indica si el miembro está inscrito en esta clase", example = "true")
    private Boolean enrolled;
}
