package service.strategy;

import model.Medicine;

public interface PricingStrategy {
    double[] calculatePrice(Medicine medicine, int quantity);
}
