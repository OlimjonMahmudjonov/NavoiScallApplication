package uz.tenzorsoft.scaleapplication.service;

import javafx.fxml.FXML;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.enumerators.ActionStatus;
import uz.tenzorsoft.scaleapplication.repository.TruckActionRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReportService {

    // Assume these are repositories with necessary query methods
    private final TruckRepository truckRepository;
    private final TruckActionRepository truckActionRepository;

    public ReportService(TruckRepository truckRepository, TruckActionRepository truckActionRepository) {
        this.truckRepository = truckRepository;
        this.truckActionRepository = truckActionRepository;
    }

    public String generateReport(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate != null) {
            fromDate = LocalDate.MIN; // Consider all records up to 'toDate'
        } else if (fromDate != null && toDate == null) {
            toDate = fromDate; // Restrict to the single day specified by 'fromDate'
        } else if (fromDate == null && toDate == null) {
            throw new IllegalArgumentException("Kamida bitta sana kiritilishi kerak.");
        }

        if (fromDate.isAfter(LocalDate.now()) || toDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Hozirgacha bo'lgan muddatni tanlang.");
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Boshlanish sanasi tugash sanasidan katta bo'lmasligi kerak.");
        }

        if (fromDate == null && toDate != null) {
            throw new IllegalArgumentException("Enter the start date as well!");
        }

        // Fetch data with filtering for COMPLETE status
        LocalDateTime startOfDay = fromDate.atStartOfDay();
        LocalDateTime endOfDay = toDate.atTime(LocalTime.MAX);

        List<Object[]> truckCounts = truckRepository.findTruckCountsByDateAndStatus(startOfDay, endOfDay, ActionStatus.COMPLETE);
        List<Object[]> truckWeights = truckActionRepository.findTruckWeightsByDateAndStatus(startOfDay, endOfDay, ActionStatus.COMPLETE);

        long totalEntranceCount = 0;
        double totalEntranceWeight = 0.0;
        long totalExitCount = 0;
        double totalExitWeight = 0.0;

        // Process truck counts
        if (truckCounts != null) {
            for (Object[] record : truckCounts) {
                String action = record[0].toString();
                Long count = ((Number) record[1]).longValue();

                if ("ENTRANCE".equalsIgnoreCase(action)) {
                    totalEntranceCount += count;
                } else if ("EXIT".equalsIgnoreCase(action)) {
                    totalExitCount += count;
                }
            }
        }

        // Process truck weights
        if (truckWeights != null) {
            for (Object[] record : truckWeights) {
                String action = record[0].toString();
                Double weight = ((Number) record[1]).doubleValue();

                if ("ENTRANCE".equalsIgnoreCase(action)) {
                    totalEntranceWeight += weight;
                } else if ("EXIT".equalsIgnoreCase(action)) {
                    totalExitWeight += weight;
                }
            }
        }

        // Determine the date range description
        String dateRange;
        if (fromDate.equals(toDate)) {
            dateRange = fromDate.toString(); // Single-day report
        } else if (fromDate.equals(LocalDate.MIN)) {
            dateRange = "Up to " + toDate; // Report for all records up to 'toDate'
        } else {
            dateRange = fromDate + " - " + toDate; // Standard range report
        }

        // Generate summary report
        return String.format("""
                    Hisobot [%s]

                            Kirim:
                    Kirishlar soni: %d
                    Yuk hajmi: %.2f kg
                               \s
                            Chiqim:
                    Chiqishlar soni: %d
                    Yuk hajmi: %.2f kg
                   \s""",
                dateRange,
                totalEntranceCount, totalEntranceWeight,
                totalExitCount, totalExitWeight
        );
    }

}
