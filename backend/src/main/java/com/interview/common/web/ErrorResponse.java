package com.interview.common.web;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ErrorResponse {
    private Instant timestamp;
    private String path;
    private String correlationId;

    private String code;       //  NOT_FOUND, VALIDATION_ERROR, CONFLICT
    private String message;

    private List<FieldError> errors; // optional field-level errors

    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}

