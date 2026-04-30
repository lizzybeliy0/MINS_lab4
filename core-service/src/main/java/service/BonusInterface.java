package service;

import model.BonusInfo;
import model.Sale;

public interface BonusInterface {
    BonusInfo getBonusInfo(String clientPhone, double saleAmount);
    double processBonusesForSale(String clientPhone, double saleAmount, Sale sale, int requestedBonus);
    void showBonuses(String clientPhone);
    void printBonusReport();
    void resetAllData();
}