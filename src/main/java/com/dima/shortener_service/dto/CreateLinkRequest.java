package com.dima.shortener_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class CreateLinkRequest {
    @NotBlank(message = "Original URL cannot be blank")
    @URL(message = "Invalid URL format")
    private String originalUrl;
    private Integer daysToExpire;

}
