package br.com.actionlabs.carboncalc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta contendo o resultado do cálculo de carbono.
 * Apresenta as emissões por categoria e o total.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resultado do cálculo de pegada de carbono")
public class CalculationResultResponse {

    @Schema(description = "ID do cálculo", example = "63f1a2b3c4d5e6f7g8h9i0j1")
    private String id;

    @Schema(description = "Emissão de carbono por consumo de energia em kg CO2", example = "20.425")
    private Double energy;

    @Schema(description = "Emissão de carbono por transporte em kg CO2", example = "60.0")
    private Double transportation;

    @Schema(description = "Emissão de carbono por resíduos sólidos em kg CO2", example = "12.0")
    private Double solidWaste;

    @Schema(description = "Emissão total de carbono em kg CO2", example = "92.425")
    private Double total;
}