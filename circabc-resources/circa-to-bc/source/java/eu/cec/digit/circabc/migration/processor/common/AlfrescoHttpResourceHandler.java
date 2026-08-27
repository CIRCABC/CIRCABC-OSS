package eu.cec.digit.circabc.migration.processor.common;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import eu.cec.digit.circabc.migration.processor.ResourceManager;
import eu.cec.digit.circabc.migration.processor.ResourceManager.ResourceHandler;
import eu.cec.digit.circabc.service.migration.ImportationException;

/**
 * ResourceHandler that fetches content from a remote Alfresco instance via HTTP.
 * Handles URIs starting with http:// or https://.
 * Uses alf_ticket authentication for accessing Alfresco webscript content URLs.
 * Falls back to Basic Auth header if ticket acquisition fails.
 */
public class AlfrescoHttpResourceHandler implements ResourceHandler {

    private static final Log logger = LogFactory.getLog(AlfrescoHttpResourceHandler.class);

    private String username;
    private String password;
    private int connectTimeout = 30000;
    private int readTimeout = 120000;
    private ResourceManager resourceManager;

    /** Cached alf_ticket for the current session */
    private String alfTicket;
    /** Base URL extracted from the first resource URL for login */
    private String cachedBaseUrl;

    public void register() {
        if (resourceManager != null) {
            resourceManager.registerResource(this);
        }
    }

    @Override
    public boolean handle(final String resourceUrl) {
        return resourceUrl != null &&
               (resourceUrl.startsWith("http://") || resourceUrl.startsWith("https://"));
    }

    @Override
    public Resource getResource(final String resourceUrl) throws ImportationException {
        try {
            // Ensure we have a valid alf_ticket
            final String ticket = getOrAcquireTicket(resourceUrl);

            // Append alf_ticket to the URL
            String authenticatedUrl = resourceUrl;
            if (ticket != null && !ticket.isEmpty()) {
                authenticatedUrl = resourceUrl + (resourceUrl.contains("?") ? "&" : "?")
                        + "alf_ticket=" + ticket;
            }

            final URL url = new URL(authenticatedUrl);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);

            // Also set Basic Auth as fallback
            if (username != null && !username.isEmpty()) {
                final String auth = Base64.getEncoder().encodeToString(
                        (username + ":" + password).getBytes("UTF-8"));
                conn.setRequestProperty("Authorization", "Basic " + auth);
            }

            final int status = conn.getResponseCode();
            if (status == 302 || status == 301) {
                // Redirect means auth failed - invalidate ticket and retry once
                logger.warn("Got redirect for " + resourceUrl + ", ticket may be expired. Retrying...");
                alfTicket = null;
                return getResource(resourceUrl);
            }
            if (status != 200) {
                throw new ImportationException(
                        "HTTP " + status + " fetching resource: " + authenticatedUrl);
            }

            // Verify we didn't get an HTML login page
            // Only flag as login page if content-type is HTML AND the URL doesn't
            // request an HTML file (legitimate .html content would have text/html type)
            final String contentType = conn.getContentType();
            if (contentType != null && contentType.contains("text/html")) {
                // Check if the requested resource is itself an HTML file
                final String lowerUrl = resourceUrl.toLowerCase();
                final boolean isHtmlResource = lowerUrl.endsWith(".html") || lowerUrl.endsWith(".htm")
                        || lowerUrl.contains(".html?") || lowerUrl.contains(".htm?");
                if (!isHtmlResource) {
                    logger.warn("Got HTML response (likely login page) for non-HTML resource: " + resourceUrl);
                    alfTicket = null;
                    throw new ImportationException(
                            "Authentication failed - received login page instead of content for: " + resourceUrl);
                }
            }

            final InputStream is = conn.getInputStream();
            if (logger.isDebugEnabled()) {
                logger.debug("Fetching content from: " + authenticatedUrl);
            }
            return new InputStreamResource(is);

        } catch (ImportationException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportationException(
                    "Error fetching resource from " + resourceUrl + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get or acquire an alf_ticket by calling /s/api/login on the source instance.
     */
    private synchronized String getOrAcquireTicket(final String resourceUrl) {
        if (alfTicket != null) {
            return alfTicket;
        }
        if (username == null || username.isEmpty()) {
            return null;
        }

        try {
            // Extract base URL from the resource URL
            final String baseUrl = extractBaseUrl(resourceUrl);
            if (baseUrl == null) {
                return null;
            }

            final String loginUrl = baseUrl + "/s/api/login";
            final String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

            final URL url = new URL(loginUrl);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes("UTF-8"));

            final int status = conn.getResponseCode();
            if (status == 200) {
                final InputStream is = conn.getInputStream();
                final byte[] bytes = new byte[4096];
                final int len = is.read(bytes);
                is.close();
                final String response = new String(bytes, 0, len, "UTF-8");

                // Parse ticket from JSON response: {"data":{"ticket":"TICKET_xxx"}}
                final int ticketStart = response.indexOf("TICKET_");
                if (ticketStart >= 0) {
                    final int ticketEnd = response.indexOf("\"", ticketStart);
                    alfTicket = response.substring(ticketStart, ticketEnd);
                    cachedBaseUrl = baseUrl;
                    logger.info("Acquired alf_ticket for import from: " + baseUrl);
                    return alfTicket;
                }
            }
            logger.warn("Failed to acquire alf_ticket from " + loginUrl + " (HTTP " + status + ")");
        } catch (Exception e) {
            logger.warn("Error acquiring alf_ticket: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Extract the base URL (protocol + host + port) from a full resource URL.
     */
    private String extractBaseUrl(final String resourceUrl) {
        try {
            final URL url = new URL(resourceUrl);
            final int port = url.getPort();
            if (port > 0) {
                return url.getProtocol() + "://" + url.getHost() + ":" + port;
            } else {
                return url.getProtocol() + "://" + url.getHost();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
    public void setResourceManager(ResourceManager resourceManager) { this.resourceManager = resourceManager; }
}
