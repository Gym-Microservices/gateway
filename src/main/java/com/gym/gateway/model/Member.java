package com.gym.gateway.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Member {
    private Long id;
    private String name;
    private String email;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationDate;
}
