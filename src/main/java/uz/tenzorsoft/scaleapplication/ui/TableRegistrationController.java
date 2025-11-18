package uz.tenzorsoft.scaleapplication.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.dto.CarInfoDto;
import uz.tenzorsoft.scaleapplication.sentDataNavoi.PageResponse;
import uz.tenzorsoft.scaleapplication.sentDataNavoi.RefreshToken;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class TableRegistrationController implements BaseController {

    private final TruckService truckService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final RefreshToken refreshToken;

    @Value("${spring.url}") private String baseUrl;


    private static final String API_PATH = "/navoiyazot-transfers/get-car-number";
    private static final int PAGE_SIZE = 33;
    private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @FXML private TableView<CarInfoDto> tableData;
    @FXML private TableColumn<CarInfoDto, String> carNumberCol;
    @FXML private TableColumn<CarInfoDto, String> ownerPinflCol;
    @FXML private TableColumn<CarInfoDto, Double> quantityCol;
    @FXML private TableColumn<CarInfoDto, String> driverNameCol;
    @FXML private TableColumn<CarInfoDto, String> productNameCol;

    @FXML private TextField searchField;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private Label totalLabel;

    // ASOSIY SOURCE LIST — bu yerda ma'lumotlar saqlanadi
    private final ObservableList<CarInfoDto> sourceList = FXCollections.observableArrayList();
    private FilteredList<CarInfoDto> filteredData;
    private int currentPage = 0;
    private int totalPages = 0;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupPlaceholder();
        setupSearch();
        loadAllDataFromApi();
    }

    private void setupTableColumns() {
        carNumberCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCarNumber()));
        ownerPinflCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getOwnerPinfl()));
        quantityCol.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getQuantity()).asObject());
        driverNameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDriverName()));
        productNameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getProductName()));
    }

    private void setupPlaceholder() {
        tableData.setPlaceholder(new Label("Ma'lumotlar yuklanmoqda..."));
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(sourceList, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(car -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase().trim();

                return (car.getCarNumber() != null && car.getCarNumber().toLowerCase().contains(lower)) ||
                        (car.getOwnerPinfl() != null && car.getOwnerPinfl().contains(newVal.trim()));
            });
            currentPage = 0;
            updatePage();
        });

        SortedList<CarInfoDto> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableData.comparatorProperty());
        tableData.setItems(sortedData);
    }

    @FXML private void clearSearch() {
        searchField.clear();
        currentPage = 0;
        updatePage();
    }

    @FXML private void goToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
    }

    @FXML private void goToNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePage();
        }
    }

    private void updatePage() {
        int totalItems = filteredData.size();
        totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / PAGE_SIZE);

        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, totalItems);

        ObservableList<CarInfoDto> pageData = totalItems == 0
                ? FXCollections.observableArrayList()
                : FXCollections.observableArrayList(filteredData.subList(from, to));

        tableData.setItems(pageData);
        tableData.setPlaceholder(totalItems == 0 ? new Label("Hech nima topilmadi") : new Label(""));

        pageLabel.setText("Sahifa " + (currentPage + 1) + " / " + totalPages);
        totalLabel.setText(totalItems + " ta");

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= totalPages - 1 || totalItems == 0);
    }

    private void loadAllDataFromApi() {
        executor.submit(() -> {
            List<CarInfoDto> cars = new ArrayList<>();
            int page = 0;
            int size = 50;

            try {
                while (true) {
                    String url = baseUrl + API_PATH + "?page=" + page + "&size=" + size;
                    System.out.println("Yuklanmoqda: " + url);
                    String json = fetchJson(url);
                    if (json == null || json.contains("Not Found")) break;

                    PageResponse response = mapper.readValue(json, PageResponse.class);
                    if (response.getContent() == null || response.getContent().isEmpty()) break;

                    cars.addAll(response.getContent());
                    saveToDatabase(response.getContent());

                    if (response.isLast()) break;
                    page++;
                }

                // YANGI MA'LUMOTLAR ENG YUQORIGA!
                cars.sort((c1, c2) -> {
                    LocalDateTime dt1 = parseEnterDate(c1.getEnterDate());
                    LocalDateTime dt2 = parseEnterDate(c2.getEnterDate());
                    if (dt1 == null && dt2 == null) return 0;
                    if (dt1 == null) return 1;
                    if (dt2 == null) return -1;
                    return dt2.compareTo(dt1);
                });

                Platform.runLater(() -> {
                    sourceList.clear();
                    sourceList.addAll(cars);

                    currentPage = 0;
                    updatePage();
                    showAlert(Alert.AlertType.INFORMATION, "Muvaffaqiyat",
                            cars.size() + " ta mashina yuklandi!\nYANGI ma’lumotlar ENG YUQORIDA");
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage()));
            }
        });
    }

    private LocalDateTime parseEnterDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            String cleaned = dateStr.replace("T", " ").substring(0, 23);
            return LocalDateTime.parse(cleaned, DATE_PARSER);
        } catch (Exception e) {
            return null;
        }
    }

    private String fetchJson(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + refreshToken.getNewToken());
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            int code = conn.getResponseCode();
            if (code != 200) return null;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                return br.lines().collect(java.util.stream.Collectors.joining());
            }
        } catch (Exception e) {
            System.err.println("Fetch xato: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private void saveToDatabase(List<CarInfoDto> list) {
        for (CarInfoDto dto : list) {
            try {
                truckService.createOrUpdateFromApi(
                        dto.getCarNumber(),
                        dto.getOwnerPinfl(),
                        dto.getDriverName(),
                        dto.getProductName(),
                        dto.getQuantity(),
                        dto.getModel()
                );
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void showAlert(Alert.AlertType type, String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}