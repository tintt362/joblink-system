package com.joblink.auth_service.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegisterRequestDto {

    @Email(message = "Email Not Valid")
    @NotBlank(message = "Email Not Blank")
    private String email;

    @NotBlank(message = "Password Not Blank")
    @Size(min = 8, max = 128 , message = "Length of Password must be between 8 and 128 character")
    private String password;


    @NotBlank(message = "Role Not Blank")
    // Đảm bảo chỉ nhận giá trị CANDIDATE hoặc RECRUITER
    @Enumerated(EnumType.STRING)
    private String role;



}
