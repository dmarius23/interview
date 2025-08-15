package com.interview.company.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RentalCompanyCreateDto {
    @NotBlank
    @Size(max = 120)
    private String name;
}
