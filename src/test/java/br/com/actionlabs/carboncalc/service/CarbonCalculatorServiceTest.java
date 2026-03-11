package br.com.actionlabs.carboncalc.service;

import br.com.actionlabs.carboncalc.dto.CalculationInfoRequest;
import br.com.actionlabs.carboncalc.dto.CalculationResultResponse;
import br.com.actionlabs.carboncalc.dto.StartCalculationRequest;
import br.com.actionlabs.carboncalc.exception.CalculationNotFoundException;
import br.com.actionlabs.carboncalc.model.Calculation;
import br.com.actionlabs.carboncalc.model.EnergyEmissionFactor;
import br.com.actionlabs.carboncalc.model.SolidWasteEmissionFactor;
import br.com.actionlabs.carboncalc.model.TransportationEmissionFactor;
import br.com.actionlabs.carboncalc.enums.TransportationType;
import br.com.actionlabs.carboncalc.repository.CalculationRepository;
import br.com.actionlabs.carboncalc.repository.EnergyEmissionFactorRepository;
import br.com.actionlabs.carboncalc.repository.SolidWasteEmissionFactorRepository;
import br.com.actionlabs.carboncalc.repository.TransportationEmissionFactorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarbonCalculatorService Tests")
class CarbonCalculatorServiceTest {

    @Mock
    private CalculationRepository calculationRepository;

    @Mock
    private EnergyEmissionFactorRepository energyEmissionFactorRepository;

    @Mock
    private TransportationEmissionFactorRepository transportationEmissionFactorRepository;

    @Mock
    private SolidWasteEmissionFactorRepository solidWasteEmissionFactorRepository;

    @InjectMocks
    private CarbonCalculatorService carbonCalculatorService;

    private StartCalculationRequest startRequest;
    private CalculationInfoRequest infoRequest;
    private Calculation calculation;

    @BeforeEach
    void setUp() {
        // Preparar dados de teste
        startRequest = new StartCalculationRequest();
        startRequest.setName("João Silva");
        startRequest.setEmail("joao@email.com");
        startRequest.setPhoneNumber("11999999999");
        startRequest.setUf("SP");

        infoRequest = new CalculationInfoRequest();
        infoRequest.setId("calc123");
        infoRequest.setEnergyConsumption(250.0);
        infoRequest.setTransportation("car");
        infoRequest.setMonthlyDistance(500.0);
        infoRequest.setSolidWasteProduction(100.0);
        infoRequest.setRecyclePercentage(0.3);

        calculation = Calculation.builder()
                .id("calc123")
                .name("João Silva")
                .email("joao@email.com")
                .phoneNumber("11999999999")
                .uf("SP")
                .energyConsumption(250.0)
                .transportation("car")
                .monthlyDistance(500.0)
                .solidWasteProduction(100.0)
                .recyclePercentage(0.3)
                .infoCompleted(true)
                .build();
    }

    @Test
    @DisplayName("Should create a new calculation successfully")
    void shouldCreateNewCalculation() {
        // Given
        Calculation savedCalculation = Calculation.builder()
                .id("calc123")
                .name(startRequest.getName())
                .email(startRequest.getEmail())
                .phoneNumber(startRequest.getPhoneNumber())
                .uf(startRequest.getUf())
                .infoCompleted(false)
                .build();

        when(calculationRepository.save(any(Calculation.class))).thenReturn(savedCalculation);

        // When
        String calculationId = carbonCalculatorService.startCalculation(startRequest);

        // Then
        assertNotNull(calculationId);
        assertEquals("calc123", calculationId);

        ArgumentCaptor<Calculation> calculationCaptor = ArgumentCaptor.forClass(Calculation.class);
        verify(calculationRepository).save(calculationCaptor.capture());

        Calculation captured = calculationCaptor.getValue();
        assertEquals("João Silva", captured.getName());
        assertEquals("joao@email.com", captured.getEmail());
        assertEquals("SP", captured.getUf());
        assertFalse(captured.getInfoCompleted());
    }

    @Test
    @DisplayName("Should update calculation info successfully")
    void shouldUpdateCalculationInfo() {
        // Given
        Calculation existingCalculation = Calculation.builder()
                .id("calc123")
                .name("João Silva")
                .uf("SP")
                .infoCompleted(false)
                .build();

        when(calculationRepository.findById("calc123")).thenReturn(Optional.of(existingCalculation));
        when(calculationRepository.save(any(Calculation.class))).thenReturn(existingCalculation);

        // When
        carbonCalculatorService.updateCalculationInfo("calc123", infoRequest);

        // Then
        ArgumentCaptor<Calculation> calculationCaptor = ArgumentCaptor.forClass(Calculation.class);
        verify(calculationRepository).save(calculationCaptor.capture());

        Calculation updated = calculationCaptor.getValue();
        assertEquals(250.0, updated.getEnergyConsumption());
        assertEquals("car", updated.getTransportation());
        assertEquals(500.0, updated.getMonthlyDistance());
        assertEquals(100.0, updated.getSolidWasteProduction());
        assertEquals(0.3, updated.getRecyclePercentage());
        assertTrue(updated.getInfoCompleted());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent calculation")
    void shouldThrowExceptionWhenCalculationNotFound() {
        // Given
        when(calculationRepository.findById("invalid")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CalculationNotFoundException.class, () -> {
            carbonCalculatorService.updateCalculationInfo("invalid", infoRequest);
        });
    }

    @Test
    @DisplayName("Should return zero emissions when info not completed")
    void shouldReturnZeroWhenInfoNotCompleted() {
        // Given
        Calculation incompleteCalculation = Calculation.builder()
                .id("calc123")
                .name("João Silva")
                .infoCompleted(false)
                .build();

        when(calculationRepository.findById("calc123")).thenReturn(Optional.of(incompleteCalculation));

        // When
        CalculationResultResponse result = carbonCalculatorService.getCalculationResult("calc123");

        // Then
        assertEquals(0.0, result.getEnergy());
        assertEquals(0.0, result.getTransportation());
        assertEquals(0.0, result.getSolidWaste());
        assertEquals(0.0, result.getTotal());
    }

    @Test
    @DisplayName("Should calculate energy emission correctly")
    void shouldCalculateEnergyEmissionCorrectly() {
        // Given
        EnergyEmissionFactor energyFactor = new EnergyEmissionFactor();
        energyFactor.setUf("SP");
        energyFactor.setFactor(0.0817);

        when(calculationRepository.findById("calc123")).thenReturn(Optional.of(calculation));
        when(energyEmissionFactorRepository.findById("SP")).thenReturn(Optional.of(energyFactor));
        when(transportationEmissionFactorRepository.findById(any())).thenReturn(Optional.empty());
        when(solidWasteEmissionFactorRepository.findById(any())).thenReturn(Optional.empty());

        // When
        CalculationResultResponse result = carbonCalculatorService.getCalculationResult("calc123");

        // Then
        double expectedEnergy = 250.0 * 0.0817;
        assertEquals(expectedEnergy, result.getEnergy(), 0.001);
    }

    @Test
    @DisplayName("Should calculate transportation emission correctly")
    void shouldCalculateTransportationEmissionCorrectly() {
        // Given
        TransportationEmissionFactor transportFactor = new TransportationEmissionFactor();
        transportFactor.setType(TransportationType.CAR);
        transportFactor.setFactor(0.12);

        when(calculationRepository.findById("calc123")).thenReturn(Optional.of(calculation));
        when(energyEmissionFactorRepository.findById(any())).thenReturn(Optional.empty());
        when(transportationEmissionFactorRepository.findById(TransportationType.CAR))
                .thenReturn(Optional.of(transportFactor));
        when(solidWasteEmissionFactorRepository.findById(any())).thenReturn(Optional.empty());

        // When
        CalculationResultResponse result = carbonCalculatorService.getCalculationResult("calc123");

        // Then
        double expectedTransport = 500.0 * 0.12;
        assertEquals(expectedTransport, result.getTransportation(), 0.001);
    }

    @Test
    @DisplayName("Should calculate solid waste emission correctly")
    void shouldCalculateSolidWasteEmissionCorrectly() {
        // Given
        SolidWasteEmissionFactor wasteFactor = new SolidWasteEmissionFactor();
        wasteFactor.setUf("SP");
        wasteFactor.setRecyclableFactor(0.05);
        wasteFactor.setNonRecyclableFactor(0.15);

        when(calculationRepository.findById("calc123")).thenReturn(Optional.of(calculation));
        when(energyEmissionFactorRepository.findById(any())).thenReturn(Optional.empty());
        when(transportationEmissionFactorRepository.findById(any())).thenReturn(Optional.empty());
        when(solidWasteEmissionFactorRepository.findById("SP")).thenReturn(Optional.of(wasteFactor));

        // When
        CalculationResultResponse result = carbonCalculatorService.getCalculationResult("calc123");

        // Then
        // Recyclable: 100 * 0.3 * 0.05 = 1.5
        // Non-recyclable: 100 * 0.7 * 0.15 = 10.5
        // Total: 1.5 + 10.5 = 12.0
        double expectedWaste = (100.0 * 0.3 * 0.05) + (100.0 * 0.7 * 0.15);
        assertEquals(expectedWaste, result.getSolidWaste(), 0.001);
    }

    @Test
    @DisplayName("Should calculate total emission correctly")
    void shouldCalculateTotalEmissionCorrectly() {
        // Given
        EnergyEmissionFactor energyFactor = new EnergyEmissionFactor();
        energyFactor.setUf("SP");
        energyFactor.setFactor(0.0817);

        TransportationEmissionFactor transportFactor = new TransportationEmissionFactor();
        transportFactor.setType(TransportationType.CAR);
        transportFactor.setFactor(0.12);

        SolidWasteEmissionFactor wasteFactor = new SolidWasteEmissionFactor();
        wasteFactor.setUf("SP");
        wasteFactor.setRecyclableFactor(0.05);
        wasteFactor.setNonRecyclableFactor(0.15);

        when(calculationRepository.findById("calc123")).thenReturn(Optional.of(calculation));
        when(energyEmissionFactorRepository.findById("SP")).thenReturn(Optional.of(energyFactor));
        when(transportationEmissionFactorRepository.findById(TransportationType.CAR))
                .thenReturn(Optional.of(transportFactor));
        when(solidWasteEmissionFactorRepository.findById("SP")).thenReturn(Optional.of(wasteFactor));

        // When
        CalculationResultResponse result = carbonCalculatorService.getCalculationResult("calc123");

        // Then
        double expectedEnergy = 250.0 * 0.0817; // 20.425
        double expectedTransport = 500.0 * 0.12; // 60.0
        double expectedWaste = (100.0 * 0.3 * 0.05) + (100.0 * 0.7 * 0.15); // 12.0
        double expectedTotal = expectedEnergy + expectedTransport + expectedWaste;

        assertEquals(expectedTotal, result.getTotal(), 0.001);
        assertEquals(expectedEnergy, result.getEnergy(), 0.001);
        assertEquals(expectedTransport, result.getTransportation(), 0.001);
        assertEquals(expectedWaste, result.getSolidWaste(), 0.001);
    }

    @Test
    @DisplayName("Should handle invalid transportation type gracefully")
    void shouldHandleInvalidTransportationType() {
        // Given
        Calculation calcWithInvalidTransport = Calculation.builder()
                .id("calc123")
                .uf("SP")
                .transportation("invalid_type")
                .monthlyDistance(500.0)
                .infoCompleted(true)
                .build();

        when(calculationRepository.findById("calc123")).thenReturn(Optional.of(calcWithInvalidTransport));
        //when(energyEmissionFactorRepository.findById(any())).thenReturn(Optional.empty());
       // when(solidWasteEmissionFactorRepository.findById(any())).thenReturn(Optional.empty());

        // When
        CalculationResultResponse result = carbonCalculatorService.getCalculationResult("calc123");

        // Then - deve retornar 0 para transporte
        assertEquals(0.0, result.getTransportation());
    }
}