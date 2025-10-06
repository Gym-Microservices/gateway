package com.gym.gateway.service;

import com.gym.gateway.dto.ClassSummaryDTO;
import com.gym.gateway.dto.MemberSummaryDTO;
import com.gym.gateway.dto.PaymentSummaryDTO;
import com.gym.gateway.model.Member;
import com.gym.gateway.model.GymClass;
import com.gym.gateway.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberAggregationService {

    private static final Logger log = LoggerFactory.getLogger(MemberAggregationService.class);

    @Autowired
    private DiscoveryClient discoveryClient;

    private final WebClient webClient = WebClient.builder().build();

    public Mono<MemberSummaryDTO> getMemberSummary(Long memberId) {
        return getJwtInfo()
                .flatMap(jwtInfo -> Mono.zip(
                        getMemberData(memberId, jwtInfo),
                        getMemberClasses(memberId, jwtInfo),
                        getMemberPayments(memberId, jwtInfo)
                ))
                .map(tuple -> {
                    Member member = tuple.getT1();
                    List<GymClass> classes = tuple.getT2();
                    List<Payment> payments = tuple.getT3();
                    return buildMemberSummary(member, classes, payments);
                })
                .doOnError(err -> log.error("❌ Error agregando información de miembro {}", memberId, err))
                .onErrorReturn(new MemberSummaryDTO()); // evita que propague el 500
    }

    private Mono<JwtInfo> getJwtInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> {
                    Jwt jwt = jwtAuth.getToken();
                    String userId = jwt.getClaimAsString("sub");
                    String username = jwt.getClaimAsString("preferred_username");
                    String email = jwt.getClaimAsString("email");
                    
                    List<String> roles = jwt.getClaimAsStringList("roles");
                    if (roles == null || roles.isEmpty()) {
                        roles = List.of("ROLE_MEMBER");
                    }    

                    return new JwtInfo("Bearer " + jwt.getTokenValue(), userId, username, email, roles);
                })
                .onErrorResume(e -> {
                    log.warn("⚠️ No se encontró JWT en contexto, usando modo interno del Gateway");
                    return Mono.just(new JwtInfo("", "system", "gateway", "system@gym.internal", List.of("ROLE_SYSTEM")));
                });
    }

    private Mono<Member> getMemberData(Long memberId, JwtInfo jwtInfo) {
        String memberServiceUrl = getServiceUrl("member-microservice");
        return webClient.get()
                .uri(memberServiceUrl + "/api/members/" + memberId)
                .headers(h -> addGatewayHeaders(h, jwtInfo))
                .retrieve()
                .bodyToMono(Member.class)
                .doOnError(e -> log.warn("Error obteniendo datos del miembro desde member-microservice: {}", e.getMessage()))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.warn("❌ Member service devolvió {} {}", e.getRawStatusCode(), e.getStatusText());
                    return Mono.just(new Member());
                })
                .onErrorReturn(new Member());
    }

    private Mono<List<GymClass>> getMemberClasses(Long memberId, JwtInfo jwtInfo) {
        String classServiceUrl = getServiceUrl("class-microservice");
        return webClient.get()
                .uri(classServiceUrl + "/api/classes/member/" + memberId)
                .headers(h -> addGatewayHeaders(h, jwtInfo))
                .retrieve()
                .bodyToFlux(GymClass.class)
                .collectList()
                .doOnError(e -> log.warn("Error obteniendo clases desde class-microservice: {}", e.getMessage()))
                .onErrorReturn(List.of());
    }

    private Mono<List<Payment>> getMemberPayments(Long memberId, JwtInfo jwtInfo) {
        String paymentServiceUrl = getServiceUrl("payment-microservice");
        return webClient.get()
                .uri(paymentServiceUrl + "/api/payment/member/" + memberId)
                .headers(h -> addGatewayHeaders(h, jwtInfo))
                .retrieve()
                .bodyToFlux(Payment.class)
                .collectList()
                .doOnError(e -> log.warn("Error obteniendo pagos desde payment-microservice: {}", e.getMessage()))
                .onErrorReturn(List.of());
    }

    private void addGatewayHeaders(org.springframework.http.HttpHeaders headers, JwtInfo jwtInfo) {
        headers.add("Authorization", jwtInfo.token());
        headers.add("X-Auth-Source", "gateway");
        headers.add("X-User-Id", jwtInfo.userId());
        headers.add("X-User-Name", jwtInfo.username());
        headers.add("X-User-Email", jwtInfo.email());

        List<String> roles = jwtInfo.roles();
        if (roles == null || roles.isEmpty()) {
            roles = List.of("ROLE_SYSTEM");
        }

        headers.add("X-User-Roles", String.join(",", roles));
    }

    private MemberSummaryDTO buildMemberSummary(Member member, List<GymClass> classes, List<Payment> payments) {
        MemberSummaryDTO summary = new MemberSummaryDTO();

        if (member.getId() != null) {
            summary.setId(member.getId());
            summary.setName(member.getName());
            summary.setEmail(member.getEmail());
            summary.setRegistrationDate(member.getRegistrationDate());
        }

        summary.setEnrolledClasses(classes.stream()
                .map(this::convertToClassSummaryDTO)
                .collect(Collectors.toList()));

        summary.setPayments(payments.stream()
                .map(this::convertToPaymentSummaryDTO)
                .collect(Collectors.toList()));

        summary.setTotalPayments(payments.stream()
                .mapToDouble(Payment::getAmount)
                .sum());
        summary.setTotalClasses(classes.size());

        return summary;
    }

    private ClassSummaryDTO convertToClassSummaryDTO(GymClass gymClass) {
        ClassSummaryDTO dto = new ClassSummaryDTO();
        dto.setId(gymClass.getId());
        dto.setName(gymClass.getName());
        dto.setSchedule(gymClass.getSchedule());
        dto.setMaxCapacity(gymClass.getMaxCapacity());
        dto.setCoachId(gymClass.getCoachId());
        dto.setEnrolled(true);
        return dto;
    }

    private PaymentSummaryDTO convertToPaymentSummaryDTO(Payment payment) {
        PaymentSummaryDTO dto = new PaymentSummaryDTO();
        dto.setId(payment.getId());
        dto.setMemberId(payment.getMemberId());
        dto.setAmount(payment.getAmount());

        // Conversión segura de Date -> OffsetDateTime
        if (payment.getPaymentDate() != null) {
            dto.setPaymentDate(payment.getPaymentDate().toInstant().atOffset(java.time.ZoneOffset.UTC));
        }

        return dto;
    }
    
    private String getServiceUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances.isEmpty()) {
            log.error("❌ Service {} no encontrado en Eureka", serviceName);
            throw new RuntimeException("Service " + serviceName + " not found");
        }
        ServiceInstance instance = instances.get(0);
        return "http://" + instance.getHost() + ":" + instance.getPort();
    }

    private record JwtInfo(String token, String userId, String username, String email, List<String> roles) {}
}
