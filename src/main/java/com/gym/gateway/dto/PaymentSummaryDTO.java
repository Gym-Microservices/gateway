package com.gym.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Date;

@Data
@Schema(description = "DTO que representa el resumen de un pago del miembro")
public class PaymentSummaryDTO {
    
    @Schema(description = "ID único del pago", example = "1")
    private Long id;
    
    @Schema(description = "ID del miembro que realizó el pago", example = "1")
    private Long memberId;
    
    @Schema(description = "Monto del pago", example = "50.00")
    private Double amount;
    
    @Schema(description = "Fecha del pago", example = "2024-01-15T10:00:00Z")
    private OffsetDateTime paymentDate;
}
