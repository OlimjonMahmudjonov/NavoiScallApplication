package uz.tenzorsoft.scaleapplication.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.PrintCheck;
import uz.tenzorsoft.scaleapplication.service.TableService;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TableController implements BaseController {

    private final TruckService truckService;
    private final TableService tableService;
    private final ExecutorService executors;
    private final PrintCheck printCheck;
    private final LogService logService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Getter
    @Setter
    private LocalDate startDate;

    @Getter
    @Setter
    private LocalDate endDate;

    @Autowired
    @Lazy
    private ControlPane controlPane;
    @Autowired
    @Lazy
    private ImageController imageController;

    @FXML
    private TableView<TableViewData> tableData;

    @FXML
    private TableColumn<TableViewData, Long> id;

    @FXML
    private TableColumn<TableViewData, String> enteredTruckNumber;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> enteredDate;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> enteredTime;

    @FXML
    private TableColumn<TableViewData, Double> enteredWeight;

    @FXML
    private TableColumn<TableViewData, String> enteredOnDuty;

    @FXML
    private TableColumn<TableViewData, String> enteredActionStatus;

    @FXML
    private TableColumn<TableViewData, String> exitedTruckNumber;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> exitedDate;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> exitedTime;

    @FXML
    private TableColumn<TableViewData, Double> exitedWeight;

//    @FXML
//    private TableColumn<TableViewData, String> exitedOnDuty;

    @FXML
    private TableColumn<TableViewData, String> exitedActionStatus;

    @FXML
    private TableColumn<TableViewData, String> minWeight;

    @FXML
    private TableColumn<TableViewData, String> maxWeight;

    @FXML
    private TableColumn<TableViewData, String> pickupWeight;

    @FXML
    private TableColumn<TableViewData, String> dropWeight;

    @FXML
    private TableColumn<TableViewData, String> productType;


    @FXML
    public void initialize() {
        id.setCellValueFactory(new PropertyValueFactory<>("Id"));
        enteredTruckNumber.setCellValueFactory(new PropertyValueFactory<>("EnteredTruckNumber"));
        enteredDate.setCellValueFactory(new PropertyValueFactory<>("EnteredDate"));
        enteredTime.setCellValueFactory(new PropertyValueFactory<>("EnteredTime"));
        enteredWeight.setCellValueFactory(new PropertyValueFactory<>("EnteredWeight"));
        enteredOnDuty.setCellValueFactory(new PropertyValueFactory<>("EnteredOnDuty"));
        enteredActionStatus.setCellValueFactory(new PropertyValueFactory<>("EnteredActionStatus"));

        exitedTruckNumber.setCellValueFactory(new PropertyValueFactory<>("ExitedTruckNumber"));
        exitedDate.setCellValueFactory(new PropertyValueFactory<>("ExitedDate"));
        exitedTime.setCellValueFactory(new PropertyValueFactory<>("ExitedTime"));
        exitedWeight.setCellValueFactory(new PropertyValueFactory<>("ExitedWeight"));
//        exitedOnDuty.setCellValueFactory(new PropertyValueFactory<>("ExitedOnDuty"));
        exitedActionStatus.setCellValueFactory(new PropertyValueFactory<>("ExitedActionStatus"));

        minWeight.setCellValueFactory(new PropertyValueFactory<>("MinWeight"));
        maxWeight.setCellValueFactory(new PropertyValueFactory<>("MaxWeight"));
        pickupWeight.setCellValueFactory(new PropertyValueFactory<>("PickupWeight"));
        dropWeight.setCellValueFactory(new PropertyValueFactory<>("DropWeight"));

        productType.setCellValueFactory(new PropertyValueFactory<>("ProductType"));

        tableData.setRowFactory(tv -> {
            TableRow<TableViewData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    printCheckAndSetDeleteButton(row);
                    editTruckNumberButtonVisibility(row);
                }
            });
            return row;
        });
    }

    private void editTruckNumberButtonVisibility(TableRow<TableViewData> row) {
        TruckEntity truck = truckService.findById(row.getItem().getId());
        if (truck == null) {
            showAlert(Alert.AlertType.ERROR, "Xatolik", "Moshina topilmadi");
            return;
        }
        Button editButton = controlPane.getEditButton();

        if (!truck.getIsFinished()) {
            editButton.setDisable(false);
            editButton.setVisible(true);
            editButton.setOnAction(e -> {
                truckService.save(truck);
                loadDataNow();
            });
            controlPane.setEditButton(editButton);
        } else {
            editButton.setDisable(true);
            editButton.setVisible(false);
            controlPane.setEditButton(editButton);
        }
        editButton.setOnMouseClicked(e -> {
            openEditPopup(truck);
            row.setItem(tableService.entityToTableData(truck));
            loadDataNow();
        });
    }

    private void openEditPopup(TruckEntity truck) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Moshina raqamini o'zgartirish");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField truckNumberField = new TextField(truck.getTruckNumber());
        truckNumberField.setPromptText("Moshina raqami");

        grid.add(new Label("Moshina raqami:"), 0, 0);
        grid.add(truckNumberField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("O'zgartirish", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Bekor qilish", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                truck.setOriginalTruckNumber(truck.getTruckNumber());
                truck.setTruckNumber(truckNumberField.getText());
                truckService.saveTruck(truck);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void printCheckAndSetDeleteButton(TableRow<TableViewData> row) {
        controlPane.getIssueCheckButton().setDisable(false);
        TableViewData rowItem = row.getItem();
        TruckEntity truck = truckService.findById(rowItem.getId());
        controlPane.getIssueCheckButton().setOnMouseClicked(e -> {
            printCheck.printReceipt(truck);
            controlPane.getIssueCheckButton().setDisable(true);
        });
        Button deleteButton = controlPane.getDeleteButton();
        if (!truck.getIsFinished()) {
            deleteButton.setDisable(false);
            deleteButton.setVisible(true);
            deleteButton.setOnAction(e -> {
                truck.setIsDeleted(true);
                truckService.save(truck);
                loadDataNow();
            });
            controlPane.setDeleteButton(deleteButton);
        } else {
            deleteButton.setDisable(true);
            deleteButton.setVisible(false);
            controlPane.setDeleteButton(deleteButton);
        }
        imageController.showImages(rowItem); // Show images based on the selected row data
    }

    public void loadDataNow() {
        try {
            List<TableViewData> data = new ArrayList<>();
            List<TruckEntity> all = truckService.findAll();
            for (TruckEntity truck : all) {
                TableViewData tableData = tableService.entityToTableData(truck);
                if (tableData != null) { // Only add non-null results
                    data.add(tableData);
                }
            }
            tableData.setItems(FXCollections.observableArrayList(data));

        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00034: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void loadData() {
//        executors.execute(() -> {
//            while (true) {
//                loadDataNow();
//                try {
//                    Thread.sleep(30000);
//                } catch (Exception e) {
//                    logService.save(new LogEntity(5L, Instances.truckNumber, "00034: (" + getClass().getName() + ") " + e.getMessage()));
//                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
//                }
//            }
//        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Загружаем обновлённые данные
                List<TableViewData> updatedData = truckService.findAll()
                        .stream()
                        .map(tableService::entityToTableData)
                        .filter(Objects::nonNull)
                        .toList();

                // Обновляем таблицу
                updateTableSmoothly(tableData, updatedData);

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("60 sekunda table loading data ...");
        }, 0, 60, TimeUnit.SECONDS);
    }

    public void updateTableSmoothly(TableView<TableViewData> tableView, List<TableViewData> newData) {
        Platform.runLater(() -> {
            ObservableList<TableViewData> currentItems = tableView.getItems();

            // Сохраняем текущий выделенный элемент и индекс
            int selectedItem = tableView.getSelectionModel().getSelectedIndex();
            int focusedIndex = tableView.getFocusModel().getFocusedIndex();

            // Сравниваем и обновляем только изменённые строки
            for (int i = 0; i < newData.size(); i++) {
                TableViewData newItem = newData.get(i);
                if (i < currentItems.size()) {
                    TableViewData currentItem = currentItems.get(i);
                    if (!newItem.equals(currentItem)) { // Сравнение объектов
                        currentItems.set(i, newItem);
                    }
                } else {
                    currentItems.add(newItem); // Добавляем новые элементы
                }
            }

            // Удаляем лишние элементы
            if (currentItems.size() > newData.size()) {
                currentItems.remove(newData.size(), currentItems.size());
            }

            // Восстанавливаем выделение и фокус
            if (selectedItem != 0) {
                tableView.getSelectionModel().selectIndices(selectedItem);
            }
            if (focusedIndex >= 0 && focusedIndex < tableView.getItems().size()) {
                tableView.getFocusModel().focus(focusedIndex);
            }
        });
    }


    public void addLastRecord() {
        TableViewData record = truckService.findLastRecord();
        if (record == null) return;
        tableData.getItems().add(0, record);
    }

    public void updateTableRow(TruckEntity truckEntity) {
        if (truckEntity == null) return;
        ObservableList<TableViewData> items = tableData.getItems();
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(truckEntity.getId())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            TableViewData record = truckService.entityToTableViewData(truckEntity);
            items.set(index, record);
        }
    }

    public void loadFilter(LocalDate startDateValue, LocalDate endDateValue) {

        this.startDate = startDateValue;
        this.endDate = endDateValue;

        try {
            List<TableViewData> data = new ArrayList<>();
            List<TruckEntity> filteredData = truckService.filterWithDate(startDateValue, endDateValue);
            for (TruckEntity truck : filteredData) {
                TableViewData tableViewData = tableService.entityToTableData(truck);
                if (tableViewData != null) data.add(tableViewData);
            }
            tableData.setItems(FXCollections.observableArrayList(data));
        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00049: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public List<TableViewData> getFilteredData() {
        return new ArrayList<>(tableData.getItems());
    }

}
