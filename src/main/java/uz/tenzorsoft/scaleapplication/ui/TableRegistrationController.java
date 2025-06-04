package uz.tenzorsoft.scaleapplication.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Arrays;
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
            try {
                URL url = new URL("https://api-kimyosanoat.tenzorsoft.uz/be/api/v1/navoiyazot-transfers/get-car-number?number=200");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int code = connection.getResponseCode();
                if (code == 200) {
                    try (InputStream inputStream = connection.getInputStream();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                        String json = reader.lines().collect(Collectors.joining());
                        ObjectMapper mapper = new ObjectMapper();

                        List<CarInfoDto> cars = Arrays.asList(mapper.readValue(json, CarInfoDto[].class));

                        // Ma'lumotlarni bazaga saqlash
                        saveCarInfoToDatabase(cars);

                        ObservableList<CarInfoDto> data = FXCollections.observableArrayList(cars);
                        Platform.runLater(() -> tableData.setItems(data));
                    }
                } else {
                    System.err.println("HTTP Error Code: " + code);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void saveCarInfoToDatabase(List<CarInfoDto> carInfoList) {
        try {
            for (CarInfoDto carInfo : carInfoList) {
                // TruckService dan foydalanib ma'lumotlarni saqlash
                TruckEntity savedTruck = truckService.createOrUpdateFromApi(
                        carInfo.getCarNumber(),
                        carInfo.getOwnerPinfl(),
                        carInfo.getDriverName(),
                        carInfo.getProductName(),
                        carInfo.getQuantity(),
                        carInfo.getModel()
                );

                if (savedTruck != null) {
                    System.out.println("Truck saqlandi/yangilandi: " + carInfo.getCarNumber() +
                            " - Driver: " + carInfo.getDriverName() +
                            " - Product: " + carInfo.getProductName());
                } else {
                    System.err.println("Truck saqlashda xatolik: " + carInfo.getCarNumber());
                }
            }
            System.out.println("Jami " + carInfoList.size() + " ta truck ma'lumoti API dan yuklandi va avtorizatsiya uchun tayyor!");
        } catch (Exception e) {
            System.err.println("Ma'lumotlarni saqlashda xatolik: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        carNumberCol.setCellValueFactory(new PropertyValueFactory<>("carNumber"));
        ownerPinflCol.setCellValueFactory(new PropertyValueFactory<>("ownerPinfl"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        driverNameCol.setCellValueFactory(new PropertyValueFactory<>("driverName"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        loadDataFromApi();
    }
}