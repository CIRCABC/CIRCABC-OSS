/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * @author sprunma
 */
public class CommonUtils {

  public static final String LANGUAGES_FILE =
    "alfresco/extension/config/languages.xml";
  public static final String VISUAL_USER_DOMAIN_SEPARATOR = "@";
  private static final Log logger = LogFactory.getLog(CommonUtils.class);
  public static List langs;

  static {
    if (langs == null || langs.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);

      digester.addObjectCreate("languages", ArrayList.class);
      digester.addObjectCreate("languages/language", Language.class);
      digester.addBeanPropertySetter("languages/language/label", "label");
      digester.addBeanPropertySetter("languages/language/code", "code");
      digester.addCallMethod(
        "languages/language/available",
        "setAvailable",
        1,
        new Class[] { Boolean.class }
      );
      digester.addCallParam("languages/language/available", 0);
      digester.addBeanPropertySetter("languages/language/order", "order");
      digester.addSetNext("languages/language", "add");

      try {
        langs = (ArrayList<Language>) digester.parse(
          CommonUtils.class.getClassLoader().getResourceAsStream(LANGUAGES_FILE)
        );
        // Sort the list according to the standard order
        Collections.sort(langs);
      } catch (IOException ioe) {
        logger.error("Unable to read the file languages.xml", ioe);
        langs = Collections.EMPTY_LIST;
      } catch (SAXException e) {
        logger.error("The file languages.xml is malformed", e);
        langs = Collections.EMPTY_LIST;
      }
    }
  }

  /**
   * Gets a list of all {@link Language} of the European Commission in the right order.
   *
   * @return a list of language
   */
  @SuppressWarnings("unchecked")
  public static List<Language> getLanguages() {
    return langs;
  }

  /**
   * Read the object from Base64 string.
   */
  public static Object fromBase64String(String s)
    throws IOException, ClassNotFoundException {
    byte[] data = DatatypeConverter.parseBase64Binary(s);
    ObjectInputStream ois = new ObjectInputStream(
      new ByteArrayInputStream(data)
    );
    Object o = ois.readObject();
    ois.close();

    return o;
  }

  /**
   * Write the object to a Base64 string.
   */
  public static String toBase64String(Serializable o) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(o);
    oos.close();

    return DatatypeConverter.printBase64Binary(baos.toByteArray());
  }

  /**
   * according to the browser, generate a UTF-8 contentdisposition header
   */
  public static String generateFilenameContentDispositionHeader(
    Boolean attachment,
    String filename,
    HttpServletRequest req
  ) {
    String userAgent = req.getHeader("User-Agent");
    String dispositionType = attachment ? "attachment" : "inline";

    try {
      // Check if browser is Firefox
      if (userAgent != null && userAgent.toLowerCase().contains("firefox")) {
        // RFC 5987 encoding for Firefox
        String encodedFilename = URLEncoder.encode(filename, "UTF-8").replace(
          "+",
          "%20"
        );
        return String.format(
          "Content-Disposition: %s; filename*=UTF-8''%s",
          dispositionType,
          encodedFilename
        );
      } else {
        // Standard encoding for Chrome, Edge, and others
        return String.format(
          "Content-Disposition: %s; filename=\"%s\"",
          dispositionType,
          filename
        );
      }
    } catch (UnsupportedEncodingException e) {
      // Fallback to basic ASCII if encoding fails
      return String.format(
        "Content-Disposition: %s; filename=\"%s\"",
        dispositionType,
        filename
      );
    }
  }
}
