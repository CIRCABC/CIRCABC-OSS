package eu.cec.digit.circabc.repo.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.validator.html.*;
import org.springframework.web.util.HtmlUtils;

import java.io.File;
import java.util.regex.Pattern;

public class SecurityService {

    private static final Log logger = LogFactory.getLog(SecurityService.class);
    private static final String lineSeparator = System.getProperty("line.separator");
    private static String POLICY_FILE_LOCATION =
            System.getProperty("user.home")
                    + File.separator
                    + "esapi"
                    + File.separator
                    + "antisamy-esapi.xml";
    private Policy policy;
    private AntiSamy antiSamy;

    private void init() {
        if (policy == null) {
            try {
                policy = Policy.getInstance(POLICY_FILE_LOCATION);
            } catch (PolicyException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error creating policy " + POLICY_FILE_LOCATION, e);
                }
            }
            antiSamy = new AntiSamy();
        }
    }

    private String stripXSS(String value) {
        if (value != null) {
            // value = ESAPI.encoder().canonicalize(value);
            // Avoid anything between script tags
            Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid anything in a src='...' type of expression
            // ----------------------------
            // disable for compatibility with  content message in the forum
            // scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE
            // | Pattern.MULTILINE | Pattern.DOTALL);
            // value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome </script> tag
            scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome <script ...> tag
            scriptPattern =
                    Pattern.compile(
                            "<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid eval(...) expressions
            scriptPattern =
                    Pattern.compile(
                            "eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid expression(...) expressions
            scriptPattern =
                    Pattern.compile(
                            "expression\\((.*?)\\)",
                            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid javascript:... expressions
            scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid vbscript:... expressions
            scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid onload= expressions
            scriptPattern =
                    Pattern.compile(
                            "onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");

            scriptPattern =
                    Pattern.compile(
                            "data:text/html;base64",
                            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
        }
        return value;
    }

    private String runAntiSamy(String value) {
        String result = value;
        CleanResults cleanValue = null;

        if (value != null) {
            init();

            try {
                cleanValue = antiSamy.scan(value, policy, AntiSamy.SAX);
            } catch (ScanException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Scan error" + POLICY_FILE_LOCATION, e);
                }
            } catch (PolicyException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Policy error " + POLICY_FILE_LOCATION, e);
                }
            }
        }

        if (cleanValue != null) {
            final String cleanHTML = cleanValue.getCleanHTML();
            final String htmlUnescape = HtmlUtils.htmlUnescape(cleanHTML);
            // check if antisamy just escaped characters
            if (!htmlUnescape.equals(result)) {
                result = cleanHTML;
            }
        } else {
            result = "";
        }
        return result;
    }

    /**
     * @param value          The string input.
     * @param permissiveHtml Set to true and it will ignore the antisamyRun
     */
    public String getCleanHTML(String value, Boolean permissiveHtml) {

        Boolean checkPermissiveHtml = (permissiveHtml == null ? false : permissiveHtml);

        String result = stripXSS(value);

        if (!checkPermissiveHtml) {
            result = runAntiSamy(result);
        }

        return result;
    }

    /**
     * Verify if the provided text corresponds to the cleaned text by antisamy & stripXss. If yes, the
     * text is valid.
     *
     * @param value          value The string input.
     * @param permissiveHtml permissiveHtml Set to true and it will ignore the antisamyRun
     */
    public boolean isCleanHTML(String value, Boolean permissiveHtml) {
        if (value == null) {
            return true;
        }
        value = value.replaceAll("\\r\\n", lineSeparator);
        return value.equalsIgnoreCase(getCleanHTML(value, permissiveHtml));
    }
}
