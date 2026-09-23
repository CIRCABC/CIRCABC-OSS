package com.sii.eucaptcha.captcha.util;

import com.sii.eucaptcha.captcha.exception.ResourceNotFoundException;
import java.io.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FileUtil {

  private FileUtil() {
    // Utility class - prevent instantiation
  }

  /**
   * Get a file resource and return it as an InputStream. Intended primarily
   * to read in binary files which are contained in a jar.
   *
   * @param filename the file
   * @return An @{link InputStream} to the file
   */
  public static InputStream readResource(String filename) {
    InputStream jarIs = FileUtil.class.getResourceAsStream(filename);
    if (jarIs == null) {
      throw new ResourceNotFoundException(
        "File '" + filename + "' not found.",
        new FileNotFoundException("File '" + filename + "' not found.")
      );
    }

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    byte[] data = new byte[16384];
    int nRead;

    try {
      while ((nRead = jarIs.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
      jarIs.close();
    } catch (IOException e) {
      log.error("Failed to read resource: {}", filename, e);
    }

    return new ByteArrayInputStream(buffer.toByteArray());
  }
}
