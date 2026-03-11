package br.com.actionlabs.carboncalc.rest;

import br.com.actionlabs.carboncalc.dto.CalculationInfoRequest;
import br.com.actionlabs.carboncalc.dto.CalculationResultResponse;
import br.com.actionlabs.carboncalc.dto.StartCalculationRequest;
import br.com.actionlabs.carboncalc.dto.StartCalculationResponse;
import br.com.actionlabs.carboncalc.service.CarbonCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
@Validated
@Tag(name = "Carbon Calculator", description = "Endpoints for carbon footprint calculation")
public class OpenRestController {

  private final CarbonCalculatorService carbonCalculatorService;

  @PostMapping("/start-calc")
  @Operation(
          summary = "Start a new carbon calculation",
          description = "Initializes a new calculation with user's basic information and returns the calculation ID"
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Calculation created successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid request data")
  })
  public ResponseEntity<StartCalculationResponse> startCalculation(
          @Valid @RequestBody StartCalculationRequest request) {

    log.info("POST /open/start-calc - Starting calculation for user: {}", request.getEmail());

    String calculationId = carbonCalculatorService.startCalculation(request);

    StartCalculationResponse response = new StartCalculationResponse();
    response.setId(calculationId);

    log.info("Calculation started successfully with id: {}", calculationId);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Atualiza as informações de consumo do cálculo.
   * Permite calcular a emissão de carbono baseada em energia, transporte e resíduos.
   * Se chamado múltiplas vezes para o mesmo ID, sobrescreve os valores anteriores.
   *
   * @param request informações de consumo (energia, transporte, resíduos sólidos)
   * @return resposta vazia com status 200 OK
   */
  @PutMapping("/info")
  @Operation(
          summary = "Update calculation information",
          description = "Updates consumption data (energy, transportation, solid waste) for carbon emission calculation. Overwrites previous values if called multiple times."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Information updated successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid request data"),
          @ApiResponse(responseCode = "404", description = "Calculation not found")
  })
  public ResponseEntity<Void> updateCalculationInfo(
          @Valid @RequestBody CalculationInfoRequest request) {

    log.info("PUT /open/info - Updating calculation id: {}", request.getId());

    carbonCalculatorService.updateCalculationInfo(request.getId(), request);

    log.info("Calculation info updated successfully for id: {}", request.getId());

    return ResponseEntity.ok().build();
  }

  /**
   * Retorna o resultado do cálculo de pegada de carbono.
   * Calcula as emissões de energia, transporte e resíduos sólidos separadamente
   * e retorna o total.
   *
   * Se as informações de consumo não foram preenchidas (/info não foi chamado),
   * retorna valores zerados.
   *
   * @param id identificador do cálculo
   * @return resultado contendo emissões por categoria e total em kg CO2
   */
  @GetMapping("/result/{id}")
  @Operation(
          summary = "Get calculation result",
          description = "Returns the carbon footprint calculation result with emissions breakdown by category (energy, transportation, solid waste) and total"
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Result retrieved successfully"),
          @ApiResponse(responseCode = "404", description = "Calculation not found")
  })
  public ResponseEntity<CalculationResultResponse> getCalculationResult(
          @PathVariable String id) {

    log.info("GET /open/result/{} - Retrieving calculation result", id);

    CalculationResultResponse result = carbonCalculatorService.getCalculationResult(id);

    log.info("Result retrieved successfully for id: {} - Total: {} kg CO2", id, result.getTotal());

    return ResponseEntity.ok(result);
  }
}