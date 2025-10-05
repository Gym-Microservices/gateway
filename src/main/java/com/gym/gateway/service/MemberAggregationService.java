package com.gym.gateway.service;

import com.gym.gateway.dto.ClassSummaryDTO;
import com.gym.gateway.dto.MemberSummaryDTO;
import com.gym.gateway.dto.PaymentSummaryDTO;
import com.gym.gateway.model.Member;
import com.gym.gateway.model.GymClass;
import com.gym.gateway.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberAggregationService {
    
    @Autowired
    private DiscoveryClient discoveryClient;
    
    
    private final WebClient webClient = WebClient.builder().build();
    
    public Mono<MemberSummaryDTO> getMemberSummary(Long memberId) {
        return getJwtToken()
                .flatMap(token -> Mono.zip(
                    getMemberData(memberId, token),
                    getMemberClasses(memberId, token),
                    getMemberPayments(memberId, token)
                ))
                .map(tuple -> {
                    Member member = tuple.getT1();
                    List<GymClass> classes = tuple.getT2();
                    List<Payment> payments = tuple.getT3();
                    
                    return buildMemberSummary(member, classes, payments);
                });
    }
    
    private Mono<String> getJwtToken() {
        return ReactiveSecurityContextHolder.getContext()
                .cast(org.springframework.security.core.context.SecurityContext.class)
                .map(securityContext -> securityContext.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> {
                    Jwt jwt = jwtAuth.getToken();
                    return "Bearer " + jwt.getTokenValue();
                })
                .onErrorReturn(""); // Retorna string vacío si no hay token
    }
    
    private Mono<Member> getMemberData(Long memberId, String token) {
        String memberServiceUrl = getServiceUrl("member-microservice");
        return webClient.get()
                .uri(memberServiceUrl + "/api/members/" + memberId)
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(Member.class)
                .onErrorReturn(new Member()); // Retorna miembro vacío si hay error
    }
    
    private Mono<List<GymClass>> getMemberClasses(Long memberId, String token) {
        String classServiceUrl = getServiceUrl("class-microservice");
        return webClient.get()
                .uri(classServiceUrl + "/api/classes/member/" + memberId)
                .header("Authorization", token)
                .retrieve()
                .bodyToFlux(GymClass.class)
                .collectList()
                .onErrorReturn(List.of()); // Retorna lista vacía si hay error
    }
    
    private Mono<List<Payment>> getMemberPayments(Long memberId, String token) {
        String paymentServiceUrl = getServiceUrl("payment-microservice");
        return webClient.get()
                .uri(paymentServiceUrl + "/api/payment/member/" + memberId)
                .header("Authorization", token)
                .retrieve()
                .bodyToFlux(Payment.class)
                .collectList()
                .onErrorReturn(List.of()); // Retorna lista vacía si hay error
    }
    
    private MemberSummaryDTO buildMemberSummary(Member member, List<GymClass> classes, List<Payment> payments) {
        MemberSummaryDTO summary = new MemberSummaryDTO();
        
        if (member.getId() != null) {
            summary.setId(member.getId());
            summary.setName(member.getName());
            summary.setEmail(member.getEmail());
            summary.setRegistrationDate(member.getRegistrationDate());
        }
        
        // Convertir clases a DTOs
        List<ClassSummaryDTO> classSummaries = classes.stream()
                .map(this::convertToClassSummaryDTO)
                .collect(Collectors.toList());
        summary.setEnrolledClasses(classSummaries);
        
        // Convertir pagos a DTOs
        List<PaymentSummaryDTO> paymentSummaries = payments.stream()
                .map(this::convertToPaymentSummaryDTO)
                .collect(Collectors.toList());
        summary.setPayments(paymentSummaries);
        
        // Calcular totales
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
        dto.setPaymentDate(payment.getPaymentDate());
        return dto;
    }
    
    private String getServiceUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances.isEmpty()) {
            throw new RuntimeException("Service " + serviceName + " not found");
        }
        ServiceInstance instance = instances.get(0);
        return "http://" + instance.getHost() + ":" + instance.getPort();
    }
}
