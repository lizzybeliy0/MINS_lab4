package service;

import java.util.List;

public interface ReportServiceInterface<T> {
    void printReport(List<T> items);
}