package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.service.ReportService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;

@Component
public class ReportDialogController {

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private TextArea reportTextArea;

    @Autowired
    private ReportService reportService;

    @FXML
    private void generateReport() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        try {
            String report = reportService.generateReport(fromDate, toDate);
            reportTextArea.setText(report);
        } catch (IllegalArgumentException e) {
            reportTextArea.setText("Xatolik: " + e.getMessage());
        }
    }

    @FXML
    private void clearStartDate() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        reportTextArea.setText("Xatolik: Hisobotni ko'rish uchun kunni tanlang.");
    }

    @FXML
    private void clearEndDate() {
        toDatePicker.setValue(null);
        LocalDate fromDate = fromDatePicker.getValue();

        if (fromDate != null) {
            try {
                String report = reportService.generateReport(fromDate, null);
                reportTextArea.setText(report);
            } catch (IllegalArgumentException e) {
                reportTextArea.setText("Xatolik: " + e.getMessage());
            }
        } else {
            reportTextArea.setText("Xatolik: Hisobotni ko'rish uchun kunni tanlang.");
        }
    }

    @FXML
    private void printOptions() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Print Options");

        VBox dialogVBox = new VBox(20);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setPadding(new Insets(30));
        dialogVBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-background-radius: 10; -fx-border-radius: 10;");

        Label promptLabel = new Label("Hisobotlarni qaysi formatda chop etishni afzal ko'rasiz?");
        promptLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #333333; -fx-font-weight: bold;");

        Button printToPdfButton = new Button("PDF formatida chop etish");
        Button printToPrinterButton = new Button("Printerga chop etish");

        String buttonStyle = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-pref-width: 220; -fx-padding: 10;";
        printToPdfButton.setStyle(buttonStyle);
        printToPrinterButton.setStyle(buttonStyle);

        printToPdfButton.setOnAction(e -> {
            printToPDF();
            dialog.close();
        });

        printToPrinterButton.setOnAction(e -> {
            printReportToPrinter();
            dialog.close();
        });

        dialogVBox.getChildren().addAll(promptLabel, printToPdfButton, printToPrinterButton);
        Scene dialogScene = new Scene(dialogVBox, 500, 350);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    @FXML
    private Label statusLabel;

    private void printToPDF() {
        String reportContent = reportTextArea.getText();

        if (reportContent == null || reportContent.isBlank() || reportContent.contains("Xatolik:")) {
            statusLabel.setText("Xatolik: Hisobot chop etilishi uchun to'g'ri bo'lishi kerak.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(reportTextArea.getScene().getWindow());

        if (file != null) {
            try (OutputStream outputStream = new FileOutputStream(file)) {
                PDDocument document = new PDDocument();
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                for (String line : reportContent.split("\n")) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                }
                contentStream.endText();
                contentStream.close();

                document.save(outputStream);
                document.close();
                statusLabel.setText("PDF muvaffaqiyatli yaratildi!");
                statusLabel.setStyle("-fx-text-fill: green;");
            } catch (IOException e) {
                statusLabel.setText("Xatolik: PDF yaratishda muammo yuz berdi.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void printReportToPrinter() {
        String reportContent = reportTextArea.getText();

        if (reportContent == null || reportContent.isBlank() || reportContent.contains("Xatolik:")) {
            statusLabel.setText("Xatolik: Hisobot chop etilishi uchun to'g'ri bo'lishi kerak.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Printer printer = Printer.getDefaultPrinter();
        if (printer == null) {
            statusLabel.setText("Xatolik: Printer mavjud emas.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(null)) {
            boolean success = printerJob.printPage(new Text(reportContent));
            if (success) {
                printerJob.endJob();
                statusLabel.setText("Hisobot chop etildi.");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Xatolik: Hisobot chop etishda muammo yuz berdi.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            statusLabel.setText("Xatolik: Printer tanlanmadi.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }





//    @FXML
//    private void printReport() {
//        String reportContent = reportTextArea.getText();
//
//        if (reportContent == null || reportContent.isBlank()) {
//            reportTextArea.setText("Xatolik: Hisobot chop etilishi uchun bo'sh bo'lmasligi kerak.");
//            return;
//        }
//
//        // Create a simple print task
//        Printer printer = Printer.getDefaultPrinter();
//        if (printer == null) {
//            reportTextArea.setText("Xatolik: Printer mavjud emas.");
//            return;
//        }
//
//        PrinterJob printerJob = PrinterJob.createPrinterJob();
//        if (printerJob != null && printerJob.showPrintDialog(null)) {
//            boolean success = printerJob.printPage(new Text(reportContent));
//            if (success) {
//                printerJob.endJob();
//                reportTextArea.setText("Hisobot chop etildi.");
//            } else {
//                reportTextArea.setText("Xatolik: Hisobot chop etishda muammo yuz berdi.");
//            }
//        } else {
//            reportTextArea.setText("Xatolik: Printer tanlanmadi.");
//        }
//    }


    public void closePopup() {
        Stage stage = (Stage) reportTextArea.getScene().getWindow();
        stage.close();
    }
}
