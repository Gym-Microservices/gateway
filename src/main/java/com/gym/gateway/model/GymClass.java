package com.gym.gateway.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
public class GymClass {
    private Long id;
    private String name;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime schedule;
    
    private Integer maxCapacity;
    private Integer currentEnrollment = 0;
    private Long coachId;
    
    private List<Long> enrolledMembers = new ArrayList<>();
    private List<Long> reservedEquipment = new ArrayList<>();
    private List<Integer> equipmentQuantities = new ArrayList<>();
}
