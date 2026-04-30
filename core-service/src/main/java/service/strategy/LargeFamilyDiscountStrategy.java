package service.strategy;

import model.Medicine;

public class LargeFamilyDiscountStrategy implements PricingStrategy {
    @Override
    public double[] calculatePrice(Medicine medicine, int quantity) {
        double discountedUnitPrice = medicine.getPrice() * 0.9; //10
        double total = discountedUnitPrice * quantity;
        return new double[] { discountedUnitPrice, total };
    }
}
