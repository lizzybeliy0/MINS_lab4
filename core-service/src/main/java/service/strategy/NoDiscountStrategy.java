package service.strategy;

import model.Medicine;

public class NoDiscountStrategy implements PricingStrategy {
    @Override
    public double[] calculatePrice(Medicine medicine, int quantity) {
        double unitPrice = medicine.getPrice();
        double total = unitPrice * quantity;
        return new double[] { unitPrice, total };
    }
}
