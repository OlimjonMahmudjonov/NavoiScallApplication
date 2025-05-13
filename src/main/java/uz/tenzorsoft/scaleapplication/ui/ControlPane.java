package uz.tenzorsoft.scaleapplication.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.ProductsEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.repository.TruckActionRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.ProductService;
import uz.tenzorsoft.scaleapplication.service.TruckActionService;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.ui.components.TruckScalingController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.truckPosition;

@Component
@RequiredArgsConstructor
public class ControlPane implements BaseController {
    private final ExecutorService executors;
    private final LogService logService;
    private final TableController tableController;
    private final ButtonController buttonController;
    private final ProductService productService;
    @Autowired
    @Lazy
    private TruckScalingController truckScalingController;
    @FXML
    private Button connectButton;

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;

    @FXML
    private ComboBox<String> productCombobox;

    private ObservableList<String> products;

    @FXML
    @Getter
    @Setter
    private Button issueCheckButton, deleteButton, editButton, button3;

/*

    @FXML
    @Getter
    @Setter
    private AnchorPane hisobotlarPane;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private TextArea reportTextArea;


    @FXML
    private void openHisobotlarWindow() {
        hisobotlarPane.setVisible(true);
    }

    @FXML
    private void closeHisobotlarPane() {
        hisobotlarPane.setVisible(false);
    }

    @FXML
    private void clearFromDate() {
        fromDatePicker.setValue(null);
        generateReport();
    }

    @FXML
    private void clearToDate() {
        toDatePicker.setValue(null);
        generateReport();
    }

    @FXML
    private void generateReport() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        try {
            String report = generateReport(fromDate, toDate);
            reportTextArea.setText(report); // Assuming `reportTextArea` is a TextArea in your UI
        } catch (IllegalArgumentException e) {
            reportTextArea.setText("Xatolik: " + e.getMessage()); // Display error message in UI
        }
    }

    // Overloaded version
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
            throw new IllegalArgumentException("Tugash sanasi boshlanish sanasidan katta bo'lmasligi kerak.");
        }


        // Fetch data
        List<Object[]> truckCounts = truckRepository.findTruckCountsByDate(fromDate, toDate);
        List<Object[]> truckWeights = truckActionRepository.findTruckWeightsByDate(fromDate, toDate);

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
*/

    @FXML
    public void initialize() {
        StringConverter<LocalDate> dateStringConverter = new StringConverter<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, formatter) : null;
            }
        };

        startDate.setConverter(dateStringConverter);
        endDate.setConverter(dateStringConverter);
        startDate.setValue(LocalDate.now());
        endDate.setValue(LocalDate.now());
        products = FXCollections.observableArrayList(productService.getAllProducts());

/*
        // Check if there are any products available
        if (!products.isEmpty()) {
            // Set the selected product if one exists and is not deleted
            ProductsEntity selectedProduct = productService.getSelectedProduct();
            if (selectedProduct != null && selectedProduct.getIsSelected() && !selectedProduct.getIsDeleted()) {
                productCombobox.setValue(selectedProduct.getName());  // Set selected product
            } else {
                showNoProductsPopup(); // If no product is selected, show the pop-up
            }
        } else {
            showNoProductsPopup(); // If there are no products, show the pop-up
        }
*/

        // Initialize product list
        products.add("Mahsulot qo'shish");
        productCombobox.setItems(products);
        if (!productService.getAllProducts().isEmpty()) {
            productCombobox.setValue(productService.getSelectedProduct().getName());
            isAvailableToConnect = true;
        }

// Apply a custom cell factory for styling
        productCombobox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Apply special style for "Mahsulot qo'shish"
                    if ("Mahsulot qo'shish".equals(item)) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-background-color: #f0f8ff;");
                    } else {
                        setStyle(""); // Default style for other items
                    }
                }
            }
        });

// Ensure the selected item in the ComboBox also uses the custom style
        productCombobox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Mahsulot qo'shish".equals(item)) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-background-color: #f0f8ff;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Handle selection
        productCombobox.setOnAction(event -> handleSelection(productCombobox.getValue()));

        editButton.setDisable(true);
        editButton.setVisible(false);
    }

    private void handleSelection(String selected) {
        if ("Mahsulot qo'shish".equals(selected)) {
            addNewProduct();
        } else if (selected != null && selected.trim().isEmpty()) {
            showNoProductsPopup();
        } else {
            // Update the selected product
            productService.updateSelectedProduct(selected);
        }
    }

    private void showNoProductsPopup() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Mahsulotlar mavjud emas");
        alert.setHeaderText("Hech qanday mahsulot qo'shilmagan.");
        alert.setContentText("Davom etish uchun mahsulot qo‘shing.");

        // Add custom buttons
        ButtonType addProductButton = new ButtonType("Mahsulot qo'shish");
        ButtonType cancelButton = ButtonType.CLOSE; // Default close button

        alert.getButtonTypes().setAll(addProductButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == addProductButton) {
                addNewProduct();
            }
        }
    }

    private void addNewProduct() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mahsulot qo'shish");
        dialog.setHeaderText("Yangi mahsulot qo'shing");
        dialog.setContentText("Mahsulot nomini kiriting:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            if (productService.getAllProducts().isEmpty()) {
                productCombobox.setValue(null);
                isAvailableToConnect = false;
                return;
            }
            productCombobox.setValue(productService.getSelectedProduct().getName());
            return;
        }
        result.ifPresent(productName -> {
            if (!productName.trim().isEmpty()) {
                // Save the new product and mark it as selected
                ProductsEntity newProduct = productService.saveProduct(productName);
                productService.updateSelectedProduct(newProduct.getName()); // Mark as selected

                // Refresh the product list
                products.add(products.size() - 1, newProduct.getName()); // Add before "Mahsulot qo'shish"
                productCombobox.setValue(newProduct.getName()); // Automatically select the new product

                // First alert for product added
                showAlert(Alert.AlertType.INFORMATION, "Mahsulot qo'shildi", "Mahsulot qo'shildi: " + productName);
                isAvailableToConnect = true;
            } else {
                showAlert(Alert.AlertType.ERROR, "Xatolik", "Mahsulot nomi bo'sh bo'lishi mumkin emas!");
                productCombobox.setValue(null);
            }
        });
    }


    public void controlConnectButton() {
        executors.execute(() -> {
            while (true) {
                try {
                    Platform.runLater(() -> {
                        if (!isConnected) {
                            connectButton.setText("Connect");
                            connectButton.setStyle(
                                    "-fx-background-color: linear-gradient(#24ff03, #21a301);" +
                                            "-fx-background-radius: 30;" +
                                            "-fx-background-insets: 0;" +
                                            "-fx-text-fill: white;"
                            );

                            connectButton.setOnMouseEntered(event -> connectButton.setStyle(
                                    "-fx-background-color: linear-gradient(#18b800, #156c00);" +
                                            "-fx-background-radius: 30;" +
                                            "-fx-background-insets: 0;" +
                                            "-fx-text-fill: white;"
                            ));
                            connectButton.setOnMouseExited(event -> connectButton.setStyle(
                                    "-fx-background-color: linear-gradient(#24ff03, #21a301);" +
                                            "-fx-background-radius: 30;" +
                                            "-fx-background-insets: 0;" +
                                            "-fx-text-fill: white;"
                            ));
                        } else {
                            connectButton.setText("Disconnect");
                            connectButton.setStyle(
                                    "-fx-background-color: linear-gradient(#ff0000, #a30000);" +
                                            "-fx-background-radius: 30;" +
                                            "-fx-background-insets: 0;" +
                                            "-fx-text-fill: white;"
                            );

                            connectButton.setOnMouseEntered(event -> connectButton.setStyle(
                                    "-fx-background-color: linear-gradient(#d00000, #7a0000);" +
                                            "-fx-background-radius: 30;" +
                                            "-fx-background-insets: 0;" +
                                            "-fx-text-fill: white;"
                            ));
                            connectButton.setOnMouseExited(event -> connectButton.setStyle(
                                    "-fx-background-color: linear-gradient(#ff0000, #a30000);" +
                                            "-fx-background-radius: 30;" +
                                            "-fx-background-insets: 0;" +
                                            "-fx-text-fill: white;"
                            ));
                        }
                    });

                    Thread.sleep(500);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });

    }


    @FXML
    private void getFilteredData() {
        LocalDate startDateValue = startDate.getValue();
        LocalDate endDateValue = endDate.getValue();

        if (startDateValue != null && startDateValue.isAfter(LocalDate.now())) {
            startDateValue = LocalDate.now();
            startDate.setValue(startDateValue);
        }
        if (endDateValue != null && endDateValue.isAfter(LocalDate.now())) {
            endDateValue = LocalDate.now();
            endDate.setValue(endDateValue);
        }
        tableController.loadFilter(startDateValue, endDateValue);

    }

    @FXML
    private void connectToController() {
        if (!isAvailableToConnect) {
            showNoProductsPopup();
            return;
        }
        if (isConnected) {
            System.out.println("Disconnecting...");
            buttonController.disconnect();
            truckPosition = -1;
            currentTruck = new TruckResponse();
            truckScalingController.reinitialize();
            cargoConfirmationStatus = -1;
            isWaiting = false;
            truckNumber = "";
        } else {
            System.out.println("Connecting...");
            buttonController.connect();
        }
    }

    public void rescaleTruckWeigh() {
        truckScalingController.setRescaleAttributes();
    }

}
