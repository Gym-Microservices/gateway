package com.gym.gateway.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class Payment {
    private Long id;
    private Long memberId;
    private Double amount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date paymentDate;
}
