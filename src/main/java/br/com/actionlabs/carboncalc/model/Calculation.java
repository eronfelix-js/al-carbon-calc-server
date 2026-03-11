package br.com.actionlabs.carboncalc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "calculations")
public class Calculation {
    @Id
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String uf;
    private Double energyConsumption;
    private String transportation;
    private Double monthlyDistance;
    private Double solidWasteProduction;
    private Double recyclePercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private Boolean infoCompleted = false;
}