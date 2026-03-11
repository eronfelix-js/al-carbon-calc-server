package br.com.actionlabs.carboncalc.repository;

import br.com.actionlabs.carboncalc.model.Calculation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalculationRepository extends MongoRepository<Calculation, String> {
    Optional<Calculation> findById(String id);
    boolean existsById(String id);
}