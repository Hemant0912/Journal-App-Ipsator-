package net.hemjournalApp.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthRequest {

    private String email;
    private String mobile;

    @NotBlank(message = "Password is required")
    private String password;
}
