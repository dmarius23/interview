package com.interview.company.dto;

import lombok.Data;

@Data
public class RentalLocationResponseDto {
    private Long id;
    private RentalCompanyResponseDto company;
    private String name;
    private String city;
    private String country;
}
