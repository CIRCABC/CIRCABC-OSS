package io.swagger.util.parsers;

import io.swagger.model.User;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author beaurpi
 */
public class FileParser {

    private static final String EMAIL = "email";
    private static final String USERID = "userid";

    private FileParser() {
        throw new IllegalStateException("Utility class");
    }

    public static List<User> parseExcelUserListPartial(WebScriptRequest req)
            throws IOException, InvalidFormatException {

        List<User> result = new ArrayList<>();

        Content reqContent = req.getContent();
        InputStream isReq = reqContent.getInputStream();
        final Workbook wb = WorkbookFactory.create(isReq);

        for (int iSheet = 0; iSheet < wb.getNumberOfSheets(); iSheet++) {
            Sheet sheet = wb.getSheetAt(iSheet);
            if (sheet != null) {
                processSheet(sheet, result);
            }
        }

        return result;
    }

    private static void processSheet(Sheet sheet, List<User> result) {
        if (result == null) {
            result = new ArrayList<>();
        }

        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        Row firstRow = sheet.getRow(firstRowNum);
        short firstCellNum = firstRow.getFirstCellNum();
        short lastCellNum = firstRow.getLastCellNum();

        if (firstCellNum < lastCellNum && firstRowNum < lastRowNum) {
            int iColUserId = getColumnId(firstRow, USERID);
            int iColEmail = getColumnId(firstRow, EMAIL);

            for (int i = firstRowNum + 1; i < lastRowNum + 1; i++) {
                processRow(sheet, result, iColUserId, iColEmail, i);
            }
        }
    }

    private static void processRow(Sheet sheet, List<User> result, int iColUserId, int iColEmail, Integer i) {
        Row row = sheet.getRow(i);
        String userId = "";
        String email = "";

        if (row.getCell(iColUserId) != null && iColUserId >= 0) {
            userId = row.getCell(iColUserId).getStringCellValue();
        }

        if (row.getCell(iColEmail) != null && iColEmail >= 0) {
            email = row.getCell(iColEmail).getStringCellValue();
        }

        User u = new User();
        u.setUserId(userId);
        u.setEmail(email);

        if (!hasUser(u, result)) {
            result.add(u);
        }
    }

    private static boolean hasUser(User u, List<User> result) {
        boolean found = false;
        for (User user : result) {
            if (user.getEmail().equals(u.getEmail()) || u.getUserId().equals(user.getUserId())) {
                found = true;
                break;
            }
        }
        return found;
    }

    private static int getColumnId(Row firstRow, String string) {
        short firstCellNum = firstRow.getFirstCellNum();
        short lastCellNum = firstRow.getLastCellNum();

        for (int i = firstCellNum; i < lastCellNum; i++) {
            if (firstRow.getCell(i).getStringCellValue() != null
                    && string.equalsIgnoreCase(firstRow.getCell(i).getStringCellValue())) {
                return i;
            }
        }

        return -1;
    }
}
