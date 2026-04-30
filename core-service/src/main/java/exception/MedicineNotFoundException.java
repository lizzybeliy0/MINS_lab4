package exception;

public class MedicineNotFoundException extends PharmacyException {

    public MedicineNotFoundException(String message) {
        super(message);
    }
}