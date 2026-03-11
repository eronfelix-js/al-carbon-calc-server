package br.com.actionlabs.carboncalc.model;

import br.com.actionlabs.carboncalc.enums.TransportationType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("transportationEmissionFactor")
public class TransportationEmissionFactor {
  @Id private TransportationType type;
  private double factor;
}
