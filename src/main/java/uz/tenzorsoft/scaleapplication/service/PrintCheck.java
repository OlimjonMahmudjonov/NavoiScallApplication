package uz.tenzorsoft.scaleapplication.service;

import javafx.scene.control.Alert;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.ui.MainController;

import javax.print.*;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isRaspberryUsing;
import static uz.tenzorsoft.scaleapplication.domain.Settings.PRINTER_NAME;

@Service
public class PrintCheck {


    private final LogService logService;
    private final MainController mainController;

    public PrintCheck(LogService logService, MainController mainController) {
        this.logService = logService;
        this.mainController = mainController;
    }

    public void printReceipt(TruckEntity response) {
        PrintService printer = findPrinter(PRINTER_NAME); // Specify your printer name here
        if (printer == null && !isRaspberryUsing) {
            mainController.showAlert(Alert.AlertType.ERROR, "Printer topilmadi", "Mavjud bo'lgan printer topilmadi");
            System.out.println(PRINTER_NAME + " printer not found.");
            return;
        }

        String receipt = buildReceiptContent(response);

        try {
            if (isRaspberryUsing) {
                // Prepare printing on Linux/Raspberry Pi
                printToLinuxDevice(receipt);
            } else {
                try (InputStream inputStream = new ByteArrayInputStream(receipt.getBytes(StandardCharsets.ISO_8859_1))) {
                    DocPrintJob printJob = printer.createPrintJob();
                    Doc doc = new SimpleDoc(inputStream, DocFlavor.INPUT_STREAM.AUTOSENSE, null);

                    // Print the receipt
                    printJob.print(doc, null);

                    // Send the cut command after printing
                    sendCutCommand(printer);
                    System.out.println("Receipt printed and paper cut successfully.");
                }
            }
        } catch (Exception e) {
            mainController.showAlert(Alert.AlertType.ERROR, "Kvitansiya chop etilishi va qog'oz kesilishi amalga oshmadi", e.getMessage());
            logService.save(new LogEntity(5L, Instances.truckNumber, "00013: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void printToLinuxDevice(String content) {
        try {
            // Set permissions for /dev/usb/lp0
            Process chmodProcess = Runtime.getRuntime().exec("sudo chmod 666 /dev/usb/lp0");
            chmodProcess.waitFor(); // Wait for the command to complete

            if (chmodProcess.exitValue() != 0) {
                throw new Exception("Failed to set permissions for /dev/usb/lp0. Please check sudo permissions.");
            }

            try (FileOutputStream fos = new FileOutputStream("/dev/usb/lp0")) {
                // Send receipt content
                fos.write(content.getBytes(StandardCharsets.ISO_8859_1));

                // Flush to ensure all data is written
                fos.flush();

                // Send the cut command
                byte[] cutCommand = new byte[]{0x1D, 0x56, 0x42, 0x00};
                fos.write(cutCommand);
                fos.flush();

                System.out.println("Receipt printed and paper cut successfully on Linux.");
            }
        } catch (Exception e) {
            mainController.showAlert(Alert.AlertType.ERROR, "Printer ishlamayapti", e.getMessage());
            logService.save(new LogEntity(5L, Instances.truckNumber, "00015: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private PrintService findPrinter(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().equalsIgnoreCase(printerName)) {
                return printService;
            }
        }
        return null;
    }

    private String buildReceiptContent(TruckEntity response) {
        String truckNumber = response.getTruckNumber();
        String operatorNumber = "N/A";
        LocalDateTime enteredAt = null;
        LocalDateTime exitedAt = null;
        Double exitedWeight = 0.0;
        Double enteredWeight = 0.0;

        for (TruckActionEntity truckAction : response.getTruckActions()) {
            if (truckAction.getAction() == TruckAction.ENTRANCE || truckAction.getAction() == TruckAction.MANUAL_ENTRANCE)
            {
                enteredAt = truckAction.getCreatedAt();
                enteredWeight = truckAction.getWeight();
            }
            if(truckAction.getAction() == TruckAction.EXIT || truckAction.getAction() == TruckAction.MANUAL_EXIT){
                exitedAt = truckAction.getCreatedAt();
                exitedWeight = truckAction.getWeight();
                operatorNumber = truckAction.getOnDuty().getPhoneNumber();
            }
        }
        double tara = Math.min(enteredWeight, exitedWeight);
        double brutto = Math.abs(enteredWeight - exitedWeight);
        double netto = Math.abs(enteredWeight - exitedWeight);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String formattedEnteredAt = enteredAt != null ? enteredAt.format(formatter) : "N/A";
        String formattedExitedAt = exitedAt != null ? exitedAt.format(formatter) : "N/A";

        StringBuilder receiptContent = new StringBuilder();
        //skladni nomini kiritish kerak
        receiptContent.append("       Sklad     \n")
                .append("--------------------------\n")
                .append("Moshina raqami: ").append(truckNumber).append("\n")
                .append("Kirish vaqti: ").append(formattedEnteredAt).append("\n")
                .append("Kirish vazni (kg): ").append(String.format("%.0f", enteredWeight)).append("\n")
                .append("Chiqish vaqti: ").append(formattedExitedAt).append("\n")
                .append("Chiqish vazni (kg): ").append(String.format("%.0f", exitedWeight)).append("\n")
                .append("Tara (kg): ").append(String.format("%.0f", tara)).append("\n")
                .append("Sof og'irlik (kg): ").append(String.format("%.0f", brutto)).append("\n")
                .append("Operator: ").append(operatorNumber).append("\n")
                .append("--------------------------\n")
                .append("   Tashrifingiz uchun raxmat!   \n\n\n\n\n\n\n\n\n\n\n");

        return receiptContent.toString();
    }


    private void sendCutCommand(PrintService printer) {
        try {
            // ESC/POS command to cut paper
            byte[] cutCommand = new byte[]{0x1D, 0x56, 0x42, 0x00};
            DocPrintJob printJob = printer.createPrintJob();
            Doc doc = new SimpleDoc(cutCommand, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            printJob.print(doc, null);
        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00014: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public void listAvailablePrinters() {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        if (printServices.length == 0) {
            System.out.println("No printers found.");
        } else {
            System.out.println("Available Printers:");
            for (PrintService printer : printServices) {
                System.out.println(printer.getName());
            }
        }
    }

    public void testCheckRecipient() {

    }
}

