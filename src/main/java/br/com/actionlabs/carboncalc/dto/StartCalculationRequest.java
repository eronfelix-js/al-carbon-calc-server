package br.com.actionlabs.carboncalc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para iniciar um novo cálculo de carbono.
 * Contém as informações básicas do usuário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para iniciar um novo cálculo de carbono")
public class StartCalculationRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Nome completo do usuário", example = "João Silva", required = true)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email do usuário", example = "joao@example.com", required = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10,11}$", message = "Phone number must contain 10 or 11 digits")
    @Schema(description = "Número de telefone (apenas dígitos)", example = "11999999999", required = true)
    private String phoneNumber;

    @NotBlank(message = "UF is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "UF must be a valid two-letter state code")
    @Schema(description = "Unidade Federativa (2 letras maiúsculas)", example = "SP", required = true)
    private String uf;
}