package br.com.actionlabs.carboncalc.exception;

public class CalculationNotFoundException extends RuntimeException {

    public CalculationNotFoundException(String id) {
        super(String.format("Calculation not found with id: %s", id));
    }

    public CalculationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}