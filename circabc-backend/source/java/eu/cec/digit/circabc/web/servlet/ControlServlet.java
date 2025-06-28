package eu.cec.digit.circabc.web.servlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

public class ControlServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        String authHeader = req.getHeader("Authorization");
        String remoteIp = getClientIpAddress(req);
        if (!AuthValidator.isAuthorized(authHeader)) {
            System.err.println("Unauthorized request: invalid API key from IP " + remoteIp);
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        if (!IpValidator.isAllowed(remoteIp)) {
            System.err.println("Unauthorized request: IP " + remoteIp + " is not allowed");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        

        String message, status;
        if ("/control/activate".equals(path)) {
            ServerState.setStatus("ACTIVATED");
            message = "Server activated successfully";
            status = "ACTIVATED";
        } else if ("/control/deactivate".equals(path)) {
            ServerState.setStatus("DEACTIVATED");
            message = "Server deactivated successfully";
            status = "DEACTIVATED";
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        ControlResponse response = new ControlResponse(message, status);

        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(new ObjectMapper().writeValueAsString(response));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim(); 
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        return request.getRemoteAddr(); 
    }

    public static class ControlResponse {
        private String message;
        private String status;

        public ControlResponse(String message, String status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class AuthValidator {

        private static final String CONFIG_PROPERTY = "circabc.control.api.key";
        private static final String PREFIX = "ApiKey ";
        private static final String GLOBAL_PROPS_PATH = "alfresco-global.properties";

        private static String apiKey;

        static {
            try {
                Properties props = new Properties();
                props.load(AuthValidator.class.getClassLoader().getResourceAsStream(GLOBAL_PROPS_PATH));
                apiKey = props.getProperty(CONFIG_PROPERTY);
            } catch (Exception e) {
                System.err.println("Failed to load API key from alfresco-global.properties: " + e.getMessage());
                apiKey = null;
            }
        }

        public static boolean isAuthorized(String authHeader) {
            if (authHeader == null || !authHeader.startsWith(PREFIX)) {
                return false;
            }
            String providedKey = authHeader.substring(PREFIX.length()).trim();
            return apiKey != null && apiKey.equals(providedKey);
        }
    }

    public static class IpValidator {

        private static final String CONFIG_PROPERTY = "circabc.control.allowed.ips";
        private static final String GLOBAL_PROPS_PATH = "alfresco-global.properties";

        private static Set<String> allowedIps = new HashSet<>();

        static {
            try {
                Properties props = new Properties();
                props.load(IpValidator.class.getClassLoader().getResourceAsStream(GLOBAL_PROPS_PATH));
                String ipsConfig = props.getProperty(CONFIG_PROPERTY);
                
                if (ipsConfig != null && !ipsConfig.trim().isEmpty()) {
                    
                    String[] ips = ipsConfig.split(",");
                    for (String ip : ips) {
                        String trimmedIp = ip.trim();
                        if (!trimmedIp.isEmpty()) {
                            allowedIps.add(trimmedIp);
                        }
                    }
                }
                
                
            } catch (Exception e) {
                System.err.println("Failed to load allowed IPs from alfresco-global.properties: " + e.getMessage());
            }
        }

        public static boolean isAllowed(String ip) {
            return !allowedIps.isEmpty() && allowedIps.contains(ip);
        }
        

        public static Set<String> getAllowedIps() {
            return new HashSet<>(allowedIps);
        }
    }

    public static class ServerState {
        private static String status = "ACTIVATED";

        public static String getStatus() {
            return status;
        }

        public static void setStatus(String newStatus) {
            status = newStatus;
        }

        public static boolean isActivated() {
            return "ACTIVATED".equalsIgnoreCase(status);
        }
    }
}