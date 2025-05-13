package uz.tenzorsoft.scaleapplication.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelService {

    public static void export(List<TableViewData> data, File file, String reportTitle, String dateRange) {
        if (file == null) return; // Handle case where user cancels file selection

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exported Data");

            // Create title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reportTitle); // Use only the report title
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);

            // Merge cells for the title
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 15)); // Adjust column span as per your table

            // Create header row
            Row headerRow = sheet.createRow(1);
            String[] headers = {"Id", "Kirgan moshina raqami", "Kirish sanasi", "Kirish vaqti", "Kirgan vazni",
                    "Kirgan vaqtdagi operator", "Chiqqan moshina raqami", "Chiqish sanasi", "Chiqish vaqti",
                    "Chiqish massasi", "Chiqqan vaqtidagi operator", "Tara", "Brutto", "Kirim",
                    "Chiqim", "Mahsulot"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
            }

            // Write data rows
            int rowIndex = 2; // Start after title and header rows
            for (TableViewData row : data) {
                Row excelRow = sheet.createRow(rowIndex++);

                writeCellValue(excelRow, 0, row.getId());
                writeCellValue(excelRow, 1, row.getEnteredTruckNumber());
                writeCellValue(excelRow, 2, row.getEnteredDate());
                writeCellValue(excelRow, 3, row.getEnteredTime());
                writeCellValue(excelRow, 4, row.getEnteredWeight());
                writeCellValue(excelRow, 5, row.getEnteredOnDuty());
                writeCellValue(excelRow, 6, row.getExitedTruckNumber());
                writeCellValue(excelRow, 7, row.getExitedDate());
                writeCellValue(excelRow, 8, row.getExitedTime());
                writeCellValue(excelRow, 9, row.getExitedWeight());
                writeCellValue(excelRow, 10, row.getExitedOnDuty());
                writeCellValue(excelRow, 11, row.getMinWeight());
                writeCellValue(excelRow, 12, row.getMaxWeight());
                writeCellValue(excelRow, 13, row.getPickupWeight());
                writeCellValue(excelRow, 14, row.getDropWeight());
                writeCellValue(excelRow, 15, row.getProductType());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save file
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeCellValue(Row row, int columnIndex, Object value) {
        Cell cell = row.createCell(columnIndex);

        if (value == null) {
            cell.setCellValue(""); // Write empty string for null values
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue()); // Store numbers as numbers
        } else {
            cell.setCellValue(value.toString()); // Store text as text
        }
    }
}

