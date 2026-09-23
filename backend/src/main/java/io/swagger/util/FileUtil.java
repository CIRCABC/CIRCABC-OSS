package io.swagger.util;

import java.text.MessageFormat;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Utility class for file-name related operations within the Alfresco repository.
 *
 * <p>Its main responsibility is to derive collision-free file names when adding
 * content to a folder, mimicking the common "filename(1).ext" style used by many
 * file systems. All members are static; the class is not meant to be
 * instantiated.
 */
public class FileUtil {

  private FileUtil() {
    // Private constructor to hide the implicit public one
  }

  /**
   * Checks if the filename has a number in parentheses before the extension.
   * This replaces the regular expression: ".*\\([0-9]*\\)\\..*"
   *
   * @param filename The filename to check
   * @return true if the filename has a pattern like "filename(123).ext"
   */
  private static boolean hasNumberInParenthesisBeforeExtension(
    String filename
  ) {
    if (!filename.contains(".") || !filename.contains("(")) {
      return false;
    }

    int openParenIndex = filename.lastIndexOf('(');
    int closeParenIndex = filename.indexOf(')', openParenIndex);
    int dotIndex = filename.lastIndexOf('.');

    // Check if we have (...).ext pattern
    if (
      openParenIndex == -1 ||
      closeParenIndex == -1 ||
      dotIndex == -1 ||
      closeParenIndex >= dotIndex
    ) {
      return false;
    }

    // Check if there are only digits between the parentheses
    String content = filename.substring(openParenIndex + 1, closeParenIndex);
    if (content.isEmpty()) {
      return false;
    }

    for (char c : content.toCharArray()) {
      if (!Character.isDigit(c)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Generates a file name that does not collide with an existing child of the
   * given parent node.
   *
   * <p>The parent folder is queried for a child with the requested name using
   * the {@link ContentModel#ASSOC_CONTAINS} association. While a match is found,
   * an incrementing counter is appended in parentheses before the file
   * extension (for example {@code report.pdf} becomes {@code report(1).pdf},
   * then {@code report(2).pdf}). If the name already contains a numeric suffix
   * in parentheses, that suffix is replaced rather than nested. Names without an
   * extension receive the counter at the end (for example {@code report} becomes
   * {@code report(1)}).
   *
   * @param nodeService the Alfresco node service used to look up existing
   *     children by name
   * @param parentRef the reference to the parent (folder) node under which the
   *     name must be unique
   * @param filename the desired file name
   * @return a file name unique among the children of {@code parentRef}; the
   *     original {@code filename} is returned unchanged when no collision exists
   */
  public static String generateUniqueFilename(
    NodeService nodeService,
    NodeRef parentRef,
    String filename
  ) {
    Integer counter = 1;
    while (
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        filename
      ) !=
      null
    ) {
      String cleanName = filename.trim();

      if (cleanName.contains(".")) {
        String leftSideName = cleanName.substring(
          0,
          cleanName.lastIndexOf('.')
        );
        String rightSideName = cleanName.substring(cleanName.lastIndexOf('.'));

        if (hasNumberInParenthesisBeforeExtension(cleanName)) {
          leftSideName = cleanName.substring(0, cleanName.lastIndexOf('('));
        }
        filename = MessageFormat.format(
          "{0}({1}){2}",
          leftSideName,
          counter,
          rightSideName
        );
      } else {
        filename = MessageFormat.format("{0}({1})", cleanName, counter);
      }

      counter += 1;
    }

    return filename;
  }
}
