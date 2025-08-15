package com.interview.company.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Data
public class RentalLocationUpdateDto {
    @NotBlank
    @Size(max = 120)
    private String name;
    @NotBlank
    @Size(max = 120)
    private String city;
    @Size(max = 120)
    private String country;
    @PositiveOrZero
    private Long version;
}
