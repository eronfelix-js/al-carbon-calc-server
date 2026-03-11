package br.com.actionlabs.carboncalc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta contendo o ID do cálculo criado")
public class StartCalculationResponse {

    @Schema(description = "ID único do cálculo criado", example = "63f1a2b3c4d5e6f7g8h9i0j1")
    private String id;
}