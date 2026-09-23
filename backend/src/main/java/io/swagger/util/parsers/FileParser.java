package io.swagger.util.parsers;

import io.swagger.model.User;
import io.swagger.util.EmailUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility class for parsing uploaded spreadsheet files into domain objects.
 *
 * <p>It reads an Excel workbook supplied in the body of a web script request and
 * extracts the list of users it contains. The parser locates the {@code userid}
 * and {@code email} columns from the header row of each sheet, validates and
 * sanitizes the email addresses, and de-duplicates the resulting users. This
 * class is not meant to be instantiated; all functionality is exposed through
 * static methods.
 *
 * @author beaurpi
 */
public class FileParser {

  /** Logger used to report rows that are skipped or contain invalid data. */
  private static final Log logger = LogFactory.getLog(FileParser.class);

  /** Header label identifying the column that holds user email addresses. */
  private static final String EMAIL = "email";

  /** Header label identifying the column that holds user identifiers. */
  private static final String USERID = "userid";

  /**
   * Private constructor to prevent instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class must not be instantiated
   */
  private FileParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the Excel workbook contained in the given web script request and
   * builds the list of users it describes.
   *
   * <p>Every sheet of the workbook is processed; for each data row a {@link User}
   * is created from the {@code userid} and {@code email} columns. Rows without a
   * valid user id or email are skipped, and duplicate users are not added more
   * than once.
   *
   * @param req the web script request whose content is the Excel workbook to parse
   * @return the list of distinct users extracted from the workbook; never {@code null}
   * @throws IOException if the request content cannot be read or the workbook
   *                     cannot be created from the input stream
   */
  public static List<User> parseExcelUserListPartial(WebScriptRequest req)
    throws IOException {
    List<User> result = new ArrayList<>();

    Content reqContent = req.getContent();
    InputStream isReq = reqContent.getInputStream();
    try (Workbook wb = WorkbookFactory.create(isReq)) {
      for (int iSheet = 0; iSheet < wb.getNumberOfSheets(); iSheet++) {
        Sheet sheet = wb.getSheetAt(iSheet);
        if (sheet != null) {
          processSheet(sheet, result);
        }
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

  private static void processRow(
    Sheet sheet,
    List<User> result,
    int iColUserId,
    int iColEmail,
    Integer i
  ) {
    Row row = sheet.getRow(i);
    String userId = extractCellValue(row, iColUserId);
    String email = sanitizeRowEmail(row, iColEmail, i);

    if (
      (email != null && !email.isEmpty()) ||
      (userId != null && !userId.isEmpty())
    ) {
      User u = new User();
      u.setUserId(userId);
      u.setEmail(email);
      if (!hasUser(u, result)) {
        result.add(u);
      }
    } else if (logger.isWarnEnabled()) {
      logger.warn("Skipping row " + i + " - no valid userId or email");
    }
  }

  private static String extractCellValue(Row row, int colIndex) {
    if (colIndex >= 0 && row.getCell(colIndex) != null) {
      return row.getCell(colIndex).getStringCellValue();
    }
    return "";
  }

  private static String sanitizeRowEmail(
    Row row,
    int iColEmail,
    Integer rowIndex
  ) {
    if (iColEmail < 0 || row.getCell(iColEmail) == null) {
      return "";
    }
    String email = row.getCell(iColEmail).getStringCellValue();
    String sanitizedEmail = EmailUtil.sanitizeEmailAddresses(email);
    if (sanitizedEmail != null && !sanitizedEmail.isEmpty()) {
      return sanitizedEmail;
    }
    if (logger.isWarnEnabled()) {
      logger.warn("Invalid email in Excel row " + rowIndex + ": " + email);
    }
    return null;
  }

  private static boolean hasUser(User u, List<User> result) {
    boolean found = false;
    for (User user : result) {
      // Check email only if both are valid
      boolean emailMatch = false;
      if (
        u.getEmail() != null &&
        user.getEmail() != null &&
        !u.getEmail().isEmpty() &&
        !user.getEmail().isEmpty()
      ) {
        emailMatch = user.getEmail().equalsIgnoreCase(u.getEmail());
      }

      // Check userId only if both are valid
      boolean userIdMatch = false;
      if (
        u.getUserId() != null &&
        user.getUserId() != null &&
        !u.getUserId().isEmpty() &&
        !user.getUserId().isEmpty()
      ) {
        userIdMatch = u.getUserId().equals(user.getUserId());
      }

      if (emailMatch || userIdMatch) {
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
      if (
        firstRow.getCell(i).getStringCellValue() != null &&
        string.equalsIgnoreCase(firstRow.getCell(i).getStringCellValue())
      ) {
        return i;
      }
    }

    return -1;
  }
}
