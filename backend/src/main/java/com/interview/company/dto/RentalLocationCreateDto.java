package com.interview.company.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RentalLocationCreateDto {
    @NotNull
    private Long companyId;
    @NotBlank
    @Size(max = 120)
    private String name;
    @NotBlank
    @Size(max = 120)
    private String city;
    @Size(max = 120)
    private String country;
}
