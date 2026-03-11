package br.com.actionlabs.carboncalc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualizar informações de consumo de um cálculo.
 * Contém dados necessários para calcular a emissão de carbono.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request com informações de consumo para cálculo de carbono")
public class CalculationInfoRequest {

    @NotBlank(message = "Calculation ID is required")
    @Schema(description = "ID do cálculo (retornado pelo endpoint /start-calc)",
            example = "63f1a2b3c4d5e6f7g8h9i0j1",
            required = true)
    private String id;

    @NotNull(message = "Energy consumption is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Energy consumption must be greater than 0")
    @Schema(description = "Consumo mensal de energia em kWh",
            example = "250.0",
            required = true)
    private Double energyConsumption;

    @NotBlank(message = "Transportation type is required")
    @Schema(description = "Tipo de transporte utilizado (ex: car, bus, bike, motorcycle)",
            example = "car",
            required = true)
    private String transportation;

    @NotNull(message = "Monthly distance is required")
    @DecimalMin(value = "0.0", message = "Monthly distance must be greater than or equal to 0")
    @Schema(description = "Distância percorrida mensalmente em km",
            example = "500.0",
            required = true)
    private Double monthlyDistance;

    @NotNull(message = "Solid waste production is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Solid waste production must be greater than 0")
    @Schema(description = "Produção mensal de resíduos sólidos em kg",
            example = "100.0",
            required = true)
    private Double solidWasteProduction;

    @NotNull(message = "Recycle percentage is required")
    @DecimalMin(value = "0.0", message = "Recycle percentage must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Recycle percentage must be between 0.0 and 1.0")
    @Schema(description = "Porcentagem de resíduos recicláveis (0.0 a 1.0, ex: 0.3 = 30%)",
            example = "0.3",
            required = true)
    private Double recyclePercentage;
}