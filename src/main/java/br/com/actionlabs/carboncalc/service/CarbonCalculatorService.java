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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class CarbonCalculatorService {

    private final CalculationRepository calculationRepository;
    private final EnergyEmissionFactorRepository energyEmissionFactorRepository;
    private final TransportationEmissionFactorRepository transportationEmissionFactorRepository;
    private final SolidWasteEmissionFactorRepository solidWasteEmissionFactorRepository;


    public String startCalculation(StartCalculationRequest request) {
        log.info("Starting new calculation for user: {} ({})", request.getName(), request.getEmail());

        Calculation calculation = Calculation.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .uf(request.getUf())
                .createdAt(LocalDateTime.now())
                .infoCompleted(false)
                .build();

        Calculation saved = calculationRepository.save(calculation);

        log.info("Calculation created successfully with id: {}", saved.getId());
        return saved.getId();
    }


    public void updateCalculationInfo(String id, CalculationInfoRequest request) {
        log.info("Updating calculation info for id: {}", id);

        Calculation calculation = calculationRepository.findById(id)
                .orElseThrow(() -> new CalculationNotFoundException(id));

        // Sobrescreve todos os parâmetros de consumo
        calculation.setEnergyConsumption(request.getEnergyConsumption());
        calculation.setTransportation(request.getTransportation());
        calculation.setMonthlyDistance(request.getMonthlyDistance());
        calculation.setSolidWasteProduction(request.getSolidWasteProduction());
        calculation.setRecyclePercentage(request.getRecyclePercentage());
        calculation.setUpdatedAt(LocalDateTime.now());
        calculation.setInfoCompleted(true);

        calculationRepository.save(calculation);

        log.info("Calculation info updated successfully for id: {}", id);
        log.debug("Energy: {} kWh, Transport: {} ({}km), Waste: {}kg ({}% recyclable)",
                request.getEnergyConsumption(),
                request.getTransportation(),
                request.getMonthlyDistance(),
                request.getSolidWasteProduction(),
                request.getRecyclePercentage() * 100);
    }


    public CalculationResultResponse getCalculationResult(String id) {
        log.info("Calculating carbon footprint for id: {}", id);

        Calculation calculation = calculationRepository.findById(id)
                .orElseThrow(() -> new CalculationNotFoundException(id));

        // Se as informações não foram completadas, retorna zeros
        if (!Boolean.TRUE.equals(calculation.getInfoCompleted())) {
            log.warn("Calculation info not completed for id: {}. Returning zero emissions.", id);
            return buildZeroResult(id);
        }

        // Calcula emissões por categoria
        double energyEmission = calculateEnergyEmission(calculation);
        double transportationEmission = calculateTransportationEmission(calculation);
        double solidWasteEmission = calculateSolidWasteEmission(calculation);

        // Calcula total
        double total = energyEmission + transportationEmission + solidWasteEmission;

        log.info("Carbon footprint calculated for id: {} - Energy: {}, Transport: {}, Waste: {}, Total: {}",
                id, energyEmission, transportationEmission, solidWasteEmission, total);

        return CalculationResultResponse.builder()
                .id(id)
                .energy(energyEmission)
                .transportation(transportationEmission)
                .solidWaste(solidWasteEmission)
                .total(total)
                .build();
    }


    private double calculateEnergyEmission(Calculation calculation) {
        if (calculation.getEnergyConsumption() == null) {
            log.warn("Energy consumption is null for calculation: {}", calculation.getId());
            return 0.0;
        }

        Optional<EnergyEmissionFactor> factorOpt =
                energyEmissionFactorRepository.findById(calculation.getUf());

        if (factorOpt.isEmpty()) {
            log.warn("Energy emission factor not found for UF: {}", calculation.getUf());
            return 0.0;
        }

        double emissionFactor = factorOpt.get().getFactor();
        double emission = calculation.getEnergyConsumption() * emissionFactor;

        log.debug("Energy emission: {} kWh × {} = {} kg CO2",
                calculation.getEnergyConsumption(), emissionFactor, emission);

        return emission;
    }

    private double calculateTransportationEmission(Calculation calculation) {
        if (calculation.getTransportation() == null || calculation.getMonthlyDistance() == null) {
            log.warn("Transportation or distance is null for calculation: {}", calculation.getId());
            return 0.0;
        }

        // Converte String para enum TransportationType
        TransportationType transportType;
        try {
            transportType = TransportationType.valueOf(calculation.getTransportation().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid transportation type: {}", calculation.getTransportation());
            return 0.0;
        }

        Optional<TransportationEmissionFactor> factorOpt =
                transportationEmissionFactorRepository.findById(transportType);

        if (factorOpt.isEmpty()) {
            log.warn("Transportation emission factor not found for type: {}", transportType);
            return 0.0;
        }

        double emissionFactor = factorOpt.get().getFactor();
        double emission = calculation.getMonthlyDistance() * emissionFactor;

        log.debug("Transportation emission: {} km × {} = {} kg CO2",
                calculation.getMonthlyDistance(), emissionFactor, emission);

        return emission;
    }

    private double calculateSolidWasteEmission(Calculation calculation) {
        if (calculation.getSolidWasteProduction() == null || calculation.getRecyclePercentage() == null) {
            log.warn("Solid waste production or recycle percentage is null for calculation: {}",
                    calculation.getId());
            return 0.0;
        }

        // Busca fatores de emissão para o UF do usuário
        Optional<SolidWasteEmissionFactor> factorOpt =
                solidWasteEmissionFactorRepository.findById(calculation.getUf());

        if (factorOpt.isEmpty()) {
            log.warn("Solid waste emission factors not found for UF: {}", calculation.getUf());
            return 0.0;
        }

        SolidWasteEmissionFactor factors = factorOpt.get();
        double recyclableFactor = factors.getRecyclableFactor();
        double nonRecyclableFactor = factors.getNonRecyclableFactor();

        double wasteProduction = calculation.getSolidWasteProduction();
        double recyclePercentage = calculation.getRecyclePercentage();
        double nonRecyclePercentage = 1.0 - recyclePercentage;

        // Calcula emissão separadamente para cada tipo
        double recyclableEmission = wasteProduction * recyclePercentage * recyclableFactor;
        double nonRecyclableEmission = wasteProduction * nonRecyclePercentage * nonRecyclableFactor;
        double totalEmission = recyclableEmission + nonRecyclableEmission;

        log.debug("Solid waste emission: {}kg waste ({}% recyclable) = {} kg CO2 (recyclable: {}, non-recyclable: {})",
                wasteProduction, recyclePercentage * 100, totalEmission, recyclableEmission, nonRecyclableEmission);

        return totalEmission;
    }


    private CalculationResultResponse buildZeroResult(String id) {
        return CalculationResultResponse.builder()
                .id(id)
                .energy(0.0)
                .transportation(0.0)
                .solidWaste(0.0)
                .total(0.0)
                .build();
    }
}