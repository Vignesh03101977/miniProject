package com.onetap.app.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.onetap.app.models.Attendance;

public class ExcelManager {

    private static final String TAG = "ExcelManager";

    public interface ExportCallback {
        void onSuccess(String savedLocation);
        void onError(String errorMessage);
    }

    /**
     * Generate one XLSX per subject
     * Rows = students
     * Columns = session dates
     */
    public static void exportSubjectAttendanceToDownloads(
            Context context,
            String subjectName,
            List<Attendance> allAttendance,
            ExportCallback callback) {

        new Thread(() -> {
            try {
                if (allAttendance == null || allAttendance.isEmpty()) {
                    callback.onError("No attendance data available.");
                    return;
                }

                Log.d(TAG, "=== Starting Export ===");
                Log.d(TAG, "Subject: " + subjectName);
                Log.d(TAG, "Records: " + allAttendance.size());

                // Group by session code
                Map<String, List<Attendance>> sessionMap = new LinkedHashMap<>();
                Map<String, String> sessionDates = new LinkedHashMap<>();

                SimpleDateFormat dateFmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                for (Attendance att : allAttendance) {
                    String code = (att.getSessionCode() != null &&
                            !att.getSessionCode().isEmpty()) ?
                            att.getSessionCode() : "unknown";

                    if (!sessionMap.containsKey(code)) {
                        sessionMap.put(code, new ArrayList<>());
                        sessionDates.put(code, att.getMarkedAt() > 0
                                ? dateFmt.format(new Date(att.getMarkedAt()))
                                : dateFmt.format(new Date()));
                    }
                    sessionMap.get(code).add(att);
                }

                // Unique students
                Map<String, String[]> studentMap = new LinkedHashMap<>();
                for (List<Attendance> sessionList : sessionMap.values()) {
                    for (Attendance att : sessionList) {
                        String key = (att.getStudentId() != null)
                                ? att.getStudentId().toLowerCase().trim()
                                : "unknown";

                        if (!studentMap.containsKey(key)) {
                            studentMap.put(key, new String[]{
                                    att.getStudentName() != null ? att.getStudentName() : "Unknown",
                                    att.getStudentId() != null ? att.getStudentId() : "N/A",
                                    att.getDepartment() != null ? att.getDepartment() : "N/A"
                            });
                        }
                    }
                }

                XSSFWorkbook workbook = new XSSFWorkbook();
                buildSheet(workbook, subjectName, sessionMap, sessionDates, studentMap);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                workbook.write(baos);
                workbook.close();

                byte[] excelBytes = baos.toByteArray();
                baos.close();

                Log.d(TAG, "Excel bytes size: " + excelBytes.length);

                if (excelBytes.length == 0) {
                    callback.onError("Excel file is empty.");
                    return;
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = sanitize(subjectName.toLowerCase()) + "_attendance_" + timeStamp + ".xlsx";

                String savedPath = saveBytesToDownloads(context, excelBytes, fileName);

                if (savedPath != null) {
                    Log.d(TAG, "✅ Saved successfully: " + savedPath);
                    callback.onSuccess(savedPath);
                } else {
                    callback.onError("Failed to save Excel file.");
                }

            } catch (Throwable e) {
                Log.e(TAG, "Export error: " + e.getMessage(), e);
                callback.onError("Export error: " + e.getMessage());
            }
        }).start();
    }

    private static void buildSheet(
            XSSFWorkbook workbook,
            String subjectName,
            Map<String, List<Attendance>> sessionMap,
            Map<String, String> sessionDates,
            Map<String, String[]> studentMap) {

        String sheetName = subjectName.length() > 31
                ? subjectName.substring(0, 31)
                : subjectName;

        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle titleStyle = buildTitleStyle(workbook);
        CellStyle headerStyle = buildHeaderStyle(workbook);
        CellStyle presentStyle = buildPresentStyle(workbook);
        CellStyle absentStyle = buildAbsentStyle(workbook);
        CellStyle normalStyle = buildNormalStyle(workbook);
        CellStyle summaryStyle = buildSummaryStyle(workbook);
        CellStyle goodPctStyle = buildGoodPercentStyle(workbook);
        CellStyle badPctStyle = buildBadPercentStyle(workbook);

        // Title row
        Row r0 = sheet.createRow(0);
        org.apache.poi.ss.usermodel.Cell titleCell = r0.createCell(0);
        titleCell.setCellValue("ATTENDANCE REGISTER - " + subjectName.toUpperCase());
        titleCell.setCellStyle(titleStyle);

        // Info row
        Row r1 = sheet.createRow(1);
        r1.createCell(0).setCellValue(
                "Generated: " + new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(new Date()) +
                        " | Sessions: " + sessionMap.size() +
                        " | Students: " + studentMap.size()
        );

        // Blank row
        sheet.createRow(2);

        // Header row
        Row headerRow = sheet.createRow(3);
        setHeader(headerRow, 0, "S.No", headerStyle);
        setHeader(headerRow, 1, "Student Name", headerStyle);
        setHeader(headerRow, 2, "Roll Number", headerStyle);
        setHeader(headerRow, 3, "Department", headerStyle);

        List<String> sessionCodes = new ArrayList<>(sessionMap.keySet());
        int startCol = 4;

        for (int i = 0; i < sessionCodes.size(); i++) {
            String code = sessionCodes.get(i);
            String date = sessionDates.getOrDefault(code, "");
            setHeader(headerRow, startCol + i, date, headerStyle);
        }

        int totalPCol = startCol + sessionCodes.size();
        int totalACol = totalPCol + 1;
        int pctCol = totalACol + 1;

        setHeader(headerRow, totalPCol, "Total P", headerStyle);
        setHeader(headerRow, totalACol, "Total A", headerStyle);
        setHeader(headerRow, pctCol, "Attendance %", headerStyle);

        // Student rows
        int rowNum = 4;
        int sno = 1;

        for (String studentKey : studentMap.keySet()) {
            String[] info = studentMap.get(studentKey);
            Row row = sheet.createRow(rowNum++);

            setNormalCell(row, 0, String.valueOf(sno++), normalStyle);
            setNormalCell(row, 1, info[0], normalStyle);
            setNormalCell(row, 2, info[1], normalStyle);
            setNormalCell(row, 3, info[2], normalStyle);

            int presentCount = 0;
            int absentCount = 0;

            for (int i = 0; i < sessionCodes.size(); i++) {
                String code = sessionCodes.get(i);
                List<Attendance> sessionList = sessionMap.get(code);

                String status = "A";
                if (sessionList != null) {
                    for (Attendance att : sessionList) {
                        String key = att.getStudentId() != null
                                ? att.getStudentId().toLowerCase().trim()
                                : "";
                        if (key.equals(studentKey)) {
                            status = "present".equalsIgnoreCase(att.getStatus()) ? "P" : "A";
                            break;
                        }
                    }
                }

                org.apache.poi.ss.usermodel.Cell sc = row.createCell(startCol + i);
                sc.setCellValue(status);
                sc.setCellStyle("P".equals(status) ? presentStyle : absentStyle);

                if ("P".equals(status)) presentCount++;
                else absentCount++;
            }

            org.apache.poi.ss.usermodel.Cell tp = row.createCell(totalPCol);
            tp.setCellValue(presentCount);
            tp.setCellStyle(summaryStyle);

            org.apache.poi.ss.usermodel.Cell ta = row.createCell(totalACol);
            ta.setCellValue(absentCount);
            ta.setCellStyle(summaryStyle);

            double perc = sessionCodes.size() > 0
                    ? ((double) presentCount / sessionCodes.size()) * 100
                    : 0;

            org.apache.poi.ss.usermodel.Cell pc = row.createCell(pctCol);
            pc.setCellValue(String.format(Locale.getDefault(), "%.1f%%", perc));
            pc.setCellStyle(perc >= 75 ? goodPctStyle : badPctStyle);
        }

        // ✅ DO NOT USE autoSizeColumn on Android
        // Set fixed widths manually
        sheet.setColumnWidth(0, 2500); // S.No
        sheet.setColumnWidth(1, 7000); // Student Name
        sheet.setColumnWidth(2, 4500); // Roll No
        sheet.setColumnWidth(3, 4500); // Department

        for (int i = 0; i < sessionCodes.size(); i++) {
            sheet.setColumnWidth(startCol + i, 4000); // Each date column
        }

        sheet.setColumnWidth(totalPCol, 3500);
        sheet.setColumnWidth(totalACol, 3500);
        sheet.setColumnWidth(pctCol, 4500);
    }

    private static String saveBytesToDownloads(Context context, byte[] bytes, String fileName) {
        try {
            ContentResolver resolver = context.getContentResolver();
            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            values.put(MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/OneTap/Attendance");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri fileUri = resolver.insert(collection, values);
            if (fileUri == null) {
                return null;
            }

            try (OutputStream os = resolver.openOutputStream(fileUri)) {
                if (os == null) return null;
                os.write(bytes);
                os.flush();
            }

            ContentValues doneValues = new ContentValues();
            doneValues.put(MediaStore.Downloads.IS_PENDING, 0);
            resolver.update(fileUri, doneValues, null, null);

            return "Downloads/OneTap/Attendance/" + fileName;

        } catch (Throwable e) {
            Log.e(TAG, "Save error: " + e.getMessage(), e);
            return null;
        }
    }

    private static void setHeader(Row row, int col, String text, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
        cell.setCellValue(text);
        cell.setCellStyle(style);
    }

    private static void setNormalCell(Row row, int col, String text, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
        cell.setCellValue(text);
        cell.setCellStyle(style);
    }

    public static String sanitize(String name) {
        if (name == null) return "attendance";
        return name.trim().replaceAll("[^a-zA-Z0-9]", "_");
    }

    public static CellStyle buildTitleStyle(org.apache.poi.ss.usermodel.Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        s.setFont(f);
        return s;
    }

    public static CellStyle buildHeaderStyle(org.apache.poi.ss.usermodel.Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setWrapText(true);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    public static CellStyle buildPresentStyle(org.apache.poi.ss.usermodel.Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.DARK_GREEN.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    public static CellStyle buildAbsentStyle(org.apache.poi.ss.usermodel.Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.DARK_RED.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    public static CellStyle buildNormalStyle(org.apache.poi.ss.usermodel.Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    public static CellStyle buildSummaryStyle(org.apache.poi.ss.usermodel.Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private static CellStyle buildGoodPercentStyle(org.apache.poi.ss.usermodel.Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.DARK_GREEN.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private static CellStyle buildBadPercentStyle(org.apache.poi.ss.usermodel.Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.DARK_RED.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }
}