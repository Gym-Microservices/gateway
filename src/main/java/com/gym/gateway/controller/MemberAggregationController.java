package com.gym.gateway.controller;

import com.gym.gateway.dto.MemberSummaryDTO;
import com.gym.gateway.service.MemberAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Member Aggregation", description = "API para agregación de datos de miembros")
@SecurityRequirement(name = "bearer-key")
public class MemberAggregationController {
    
    @Autowired
    private MemberAggregationService memberAggregationService;
    
    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Endpoint de prueba para verificar que el controlador funciona")
    public Mono<ResponseEntity<String>> test() {
        return Mono.just(ResponseEntity.ok("Controlador del gateway funcionando correctamente"));
    }
    
    @GetMapping("/routes")
    @Operation(summary = "List routes", description = "Lista las rutas disponibles en el gateway")
    public Mono<ResponseEntity<String>> routes() {
        String routes = "Rutas disponibles:\n" +
                "- GET /api/test - Endpoint de prueba\n" +
                "- GET /api/routes - Lista de rutas\n" +
                "- GET /api/aggregation/members/{id}/summary - Resumen de miembro (requiere autenticación)\n" +
                "- GET /api/members - Lista de miembros\n" +
                "- GET /api/members/{id} - Miembro por ID\n" +
                "- GET /api/members/email/{email} - Miembro por email\n" +
                "- GET /api/classes/member/{id} - Clases por miembro\n" +
                "- GET /api/payment/member/{id} - Pagos por miembro";
        return Mono.just(ResponseEntity.ok(routes));
    }
    
    @GetMapping("/aggregation/members/{id}/summary")
    @Operation(
        summary = "Obtener resumen completo de miembro", 
        description = "Obtiene información agregada de un miembro incluyendo sus clases inscritas y pagos realizados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumen del miembro obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Miembro no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SuppressWarnings("unchecked")
    public Mono<ResponseEntity<MemberSummaryDTO>> getMemberSummary(
            @Parameter(description = "ID del miembro") @PathVariable Long id) {
        
        return memberAggregationService.getMemberSummary(id)
                .map(summary -> {
                    if (summary.getId() == null) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(summary);
                })
                .cast(ResponseEntity.class)
                .map(response -> (ResponseEntity<MemberSummaryDTO>) response)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
}
