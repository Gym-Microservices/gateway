package com.gym.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "DTO que representa el resumen completo de un miembro con sus clases y pagos")
public class MemberSummaryDTO {
    
    @Schema(description = "ID único del miembro", example = "1")
    private Long id;
    
    @Schema(description = "Nombre completo del miembro", example = "Ana López")
    private String name;
    
    @Schema(description = "Email del miembro", example = "ana.lopez@email.com")
    private String email;
    
    @Schema(description = "Fecha de registro del miembro", example = "2024-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationDate;
    
    @Schema(description = "Lista de clases en las que está inscrito el miembro")
    private List<ClassSummaryDTO> enrolledClasses;
    
    @Schema(description = "Lista de pagos realizados por el miembro")
    private List<PaymentSummaryDTO> payments;
    
    @Schema(description = "Total de pagos realizados", example = "150.00")
    private Double totalPayments;
    
    @Schema(description = "Número total de clases inscritas", example = "5")
    private Integer totalClasses;
}
