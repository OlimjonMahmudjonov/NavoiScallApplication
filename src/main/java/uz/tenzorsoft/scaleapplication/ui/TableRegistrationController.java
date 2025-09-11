package uz.tenzorsoft.scaleapplication.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.dto.CarInfoDto;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TableRegistrationController implements BaseController {

    @Autowired
    private TruckService truckService;

    private final ExecutorService executors = Executors.newFixedThreadPool(2);
    private static final ObjectMapper mapper = new ObjectMapper(); // Reuse ObjectMapper
    private static final int MAX_RESPONSE_SIZE = 10 * 1024 * 1024; // 10MB limit

    @FXML
    private TableView<CarInfoDto> tableData;

    @FXML
    private TableColumn<CarInfoDto, String> carNumberCol;

    @FXML
    private TableColumn<CarInfoDto, String> ownerPinflCol;

    @FXML
    private TableColumn<CarInfoDto, Double> quantityCol;

    @FXML
    private TableColumn<CarInfoDto, String> driverNameCol;

    @FXML
    private TableColumn<CarInfoDto, String> productNameCol;

    private void loadDataFromApi() {
        executors.submit(() -> {
            HttpURLConnection connection = null;
            try {
                System.out.println("🔄 API dan ma'lumot yuklash boshlandi...");

                URL url = new URL("https://api-kimyosanoat.tenzorsoft.uz/be/api/v1/navoiyazot-transfers/get-car-number?number=200");
                connection = (HttpURLConnection) url.openConnection();

                // Set timeouts to prevent hanging
                connection.setConnectTimeout(10000); // 10 seconds
                connection.setReadTimeout(30000);    // 30 seconds

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJrcHAxX3VzZXIiLCJpYXQiOjE3NTA2NjAxMjYsImV4cCI6MjM4MTM4MDEyNn0.yVLoZFIJDkAxNRFZiIY1vn50WcDwrl9JTF8q8ph0ieg");
                connection.setRequestProperty("Accept", "application/json");

                connection.connect();

                int code = connection.getResponseCode();
                System.out.println("📡 HTTP Response Code: " + code);

                if (code == 200) {
                    // Check content length to avoid large responses
                    int contentLength = connection.getContentLength();
                    if (contentLength > MAX_RESPONSE_SIZE) {
                        throw new RuntimeException("Response too large: " + contentLength + " bytes");
                    }

                    String json = null;
                    try (InputStream inputStream = connection.getInputStream();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {

                        // Read with size limit
                        StringBuilder jsonBuilder = new StringBuilder();
                        String line;
                        int totalSize = 0;

                        while ((line = reader.readLine()) != null) {
                            totalSize += line.length();
                            if (totalSize > MAX_RESPONSE_SIZE) {
                                throw new RuntimeException("Response too large during reading");
                            }
                            jsonBuilder.append(line);
                        }

                        json = jsonBuilder.toString();
                        System.out.println("📄 JSON response size: " + json.length() + " characters");
                    }

                    // Parse JSON with better error handling
                    List<CarInfoDto> cars = parseJsonSafely(json);

                    if (cars != null && !cars.isEmpty()) {
                        System.out.println("✅ " + cars.size() + " ta avtomobil ma'lumoti parse qilindi");

                        // Save to database
                        saveCarInfoToDatabase(cars);

                        // Update UI
                        ObservableList<CarInfoDto> data = FXCollections.observableArrayList(cars);
                        Platform.runLater(() -> {
                            tableData.setItems(data);
                            System.out.println("📊 Table yangilandi: " + cars.size() + " ta yozuv");
                        });
                    } else {
                        System.err.println("❌ Parse qilingan ma'lumot bo'sh yoki null");
                    }
                } else {
                    System.err.println("❌ HTTP Error Code: " + code);
                    // Read error response
                    try (InputStream errorStream = connection.getErrorStream();
                         BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream))) {
                        String errorResponse = errorReader.lines().collect(Collectors.joining());
                        System.err.println("Error response: " + errorResponse);
                    } catch (Exception e) {
                        System.err.println("Could not read error response: " + e.getMessage());
                    }
                }

            } catch (OutOfMemoryError e) {
                System.err.println("💥 MEMORY ERROR in loadDataFromApi: " + e.getMessage());
                // Force garbage collection
                System.gc();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Xotira yetishmovchiligi");
                    alert.setHeaderText("API ma'lumotlari juda katta");
                    alert.setContentText("Xotira yetishmadi. Dasturni qayta ishga tushiring.");
                    alert.showAndWait();
                });
            } catch (Exception e) {
                System.err.println("❌ API dan ma'lumot yuklashda xatolik: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("API Xatolik");
                    alert.setHeaderText("Ma'lumot yuklashda muammo");
                    alert.setContentText("API dan ma'lumot yuklab bo'lmadi: " + e.getMessage());
                    alert.showAndWait();
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private List<CarInfoDto> parseJsonSafely(String json) {
        if (json == null || json.trim().isEmpty()) {
            System.err.println("❌ JSON bo'sh yoki null");
            return new ArrayList<>();
        }

        try {
            // Force garbage collection before parsing
            System.gc();

            System.out.println("🔄 JSON parse qilish boshlandi...");

            // Use TypeReference instead of array deserialization (more memory efficient)
            TypeReference<List<CarInfoDto>> typeRef = new TypeReference<List<CarInfoDto>>() {};
            List<CarInfoDto> cars = mapper.readValue(json, typeRef);

            System.out.println("✅ JSON muvaffaqiyatli parse qilindi: " + cars.size() + " ta element");
            return cars;

        } catch (OutOfMemoryError e) {
            System.err.println("💥 JSON parse qilishda xotira tugadi: " + e.getMessage());
            System.gc(); // Force cleanup
            throw e;
        } catch (Exception e) {
            System.err.println("❌ JSON parse qilishda xatolik: " + e.getMessage());
            System.err.println("JSON snippet: " + json.substring(0, Math.min(200, json.length())) + "...");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveCarInfoToDatabase(List<CarInfoDto> carInfoList) {
        try {
            System.out.println("💾 Bazaga saqlash boshlandi: " + carInfoList.size() + " ta yozuv");

            int saved = 0;
            int failed = 0;

            for (CarInfoDto carInfo : carInfoList) {
                try {
                    TruckEntity savedTruck = truckService.createOrUpdateFromApi(
                            carInfo.getCarNumber(),
                            carInfo.getOwnerPinfl(),
                            carInfo.getDriverName(),
                            carInfo.getProductName(),
                            carInfo.getQuantity(),
                            carInfo.getModel()
                    );

                    if (savedTruck != null) {
                        saved++;
                        if (saved % 10 == 0) { // Log every 10th save
                            System.out.println("📝 Saqlandi: " + saved + "/" + carInfoList.size());
                        }
                    } else {
                        failed++;
                        System.err.println("❌ Saqlashda xatolik: " + carInfo.getCarNumber());
                    }
                } catch (Exception e) {
                    failed++;
                    System.err.println("❌ " + carInfo.getCarNumber() + " saqlashda xatolik: " + e.getMessage());
                }
            }

            System.out.println("✅ Bazaga saqlash tugadi: " + saved + " muvaffaqiyatli, " + failed + " xatolik");

        } catch (Exception e) {
            System.err.println("💥 Bazaga saqlashda umumiy xatolik: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        System.out.println("🚀 TableRegistrationController initialize qilindi");

        carNumberCol.setCellValueFactory(new PropertyValueFactory<>("carNumber"));
        ownerPinflCol.setCellValueFactory(new PropertyValueFactory<>("ownerPinfl"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        driverNameCol.setCellValueFactory(new PropertyValueFactory<>("driverName"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        loadDataFromApi();
    }

    // Cleanup method
    public void shutdown() {
        if (executors != null && !executors.isShutdown()) {
            executors.shutdownNow();
            System.out.println("🔄 ExecutorService to'xtatildi");
        }
    }
}