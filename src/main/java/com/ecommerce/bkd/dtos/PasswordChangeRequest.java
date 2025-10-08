package com.ecommerce.bkd.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordChangeRequest {
    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}
