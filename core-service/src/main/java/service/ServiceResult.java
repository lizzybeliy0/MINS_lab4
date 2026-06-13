package service;

public class ServiceResult<T> {
    private final T value;
    private final boolean available;
    private final String errorMessage;

    private ServiceResult(T value, boolean available, String errorMessage) {
        this.value = value;
        this.available = available;
        this.errorMessage = errorMessage;
    }

    public static <T> ServiceResult<T> success(T value) {
        return new ServiceResult<>(value, true, null);
    }

    public static <T> ServiceResult<T> unavailable(String errorMessage) {
        return new ServiceResult<>(null, false, errorMessage);
    }

    public T getValue() { return value; }
    public boolean isAvailable() { return available; }
    public String getErrorMessage() { return errorMessage; }
}