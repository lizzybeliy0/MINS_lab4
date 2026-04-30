package service.strategy;

import model.Medicine;

public class PensionerDiscountStrategy implements PricingStrategy{
    @Override
    public double[] calculatePrice(Medicine medicine, int quantity) {
        double discountedUnitPrice = medicine.getPrice() * 0.75; //25
        double total = discountedUnitPrice * quantity;
        return new double[] { discountedUnitPrice, total };
    }
}
